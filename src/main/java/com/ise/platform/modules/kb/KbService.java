package com.ise.platform.modules.kb;

import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.modules.kb.rag.KbRagService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class KbService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;
    private final KbRagService kbRagService;

    public KbService(JdbcTemplate jdbcTemplate, KbRagService kbRagService) {
        this.jdbcTemplate = jdbcTemplate;
        this.kbRagService = kbRagService;
    }

    public PagedData<KbDto.ArticleView> listArticles(CurrentUser user,
                                                     int pageNo,
                                                     int pageSize,
                                                     String keyword,
                                                     Long categoryId,
                                                     String publishStatus,
                                                     String tag) {
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        String keywordLike = StringUtils.hasText(keyword) ? "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%" : null;
        String statusFilter = normalizeFilter(publishStatus);
        String tagFilter = normalizeFilter(tag);
        boolean managerLike = isManagerLike(user);

        Long total = jdbcTemplate.queryForObject(
            """
                select count(*)
                  from kb_article
                 where is_deleted = 0
                   and (? = true or publish_status = 'published')
                   and (? is null or lower(title) like ? or lower(summary) like ? or lower(coalesce(source_file_name, '')) like ?)
                   and (? is null or lower(publish_status) = ?)
                   and (? is null or lower(category_label) = ?)
                """,
            Long.class,
            managerLike,
            keywordLike, keywordLike, keywordLike, keywordLike,
            statusFilter, statusFilter,
            tagFilter, tagFilter
        );

        List<KbDto.ArticleView> records = jdbcTemplate.query(
            """
                select id, title, summary, category_label, publish_status, version_no,
                       source_file_name, source_file_id, keywords_text
                  from kb_article
                 where is_deleted = 0
                   and (? = true or publish_status = 'published')
                   and (? is null or lower(title) like ? or lower(summary) like ? or lower(coalesce(source_file_name, '')) like ?)
                   and (? is null or lower(publish_status) = ?)
                   and (? is null or lower(category_label) = ?)
                 order by updated_at desc, id desc
                 limit ? offset ?
                """,
            this::mapArticle,
            managerLike,
            keywordLike, keywordLike, keywordLike, keywordLike,
            statusFilter, statusFilter,
            tagFilter, tagFilter,
            safePageSize,
            (safePageNo - 1) * safePageSize
        );

        return new PagedData<>(records, safePageNo, safePageSize, total == null ? 0 : total);
    }

    public List<KbDto.TemplateView> templates(CurrentUser user) {
        return jdbcTemplate.query(
            """
                select id, template_name, category_label, file_type, description, file_id, updated_at
                  from kb_template
                 where is_deleted = 0
                 order by updated_at desc, id desc
                """,
            (rs, rowNum) -> {
                Long fileId = rs.getObject("file_id", Long.class);
                return new KbDto.TemplateView(
                    rs.getLong("id"),
                    rs.getString("template_name"),
                    rs.getString("category_label"),
                    rs.getString("file_type"),
                    format(rs.getTimestamp("updated_at")),
                    rs.getString("description"),
                    fileId,
                    buildSourceUrl(fileId)
                );
            }
        );
    }

    public KbDto.ArticleDetailView articleDetail(CurrentUser user, Long articleId) {
        List<KbDto.ArticleDetailView> rows = jdbcTemplate.query(
            """
                select id, title, summary, category_label, publish_status, version_no,
                       standard_answer, source_file_name, source_file_id, keywords_text, view_count
                  from kb_article
                 where id = ?
                   and is_deleted = 0
                """,
            (rs, rowNum) -> {
                Long sourceFileId = rs.getObject("source_file_id", Long.class);
                return new KbDto.ArticleDetailView(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("summary"),
                    rs.getString("category_label"),
                    rs.getString("publish_status"),
                    rs.getString("version_no"),
                    rs.getString("standard_answer"),
                    rs.getString("source_file_name"),
                    sourceFileId,
                    buildSourceUrl(sourceFileId),
                    parseKeywords(rs.getString("keywords_text")),
                    rs.getInt("view_count")
                );
            },
            articleId
        );

        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "article not found");
        }

        KbDto.ArticleDetailView detail = rows.get(0);
        if (!isManagerLike(user) && !"published".equalsIgnoreCase(detail.getPublishStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "article not found");
        }

        jdbcTemplate.update("update kb_article set view_count = coalesce(view_count, 0) + 1 where id = ?", articleId);

        return new KbDto.ArticleDetailView(
            detail.getArticleId(),
            detail.getTitle(),
            detail.getSummary(),
            detail.getCategoryLabel(),
            detail.getPublishStatus(),
            detail.getVersion(),
            detail.getContent(),
            detail.getSource(),
            detail.getSourceFileId(),
            detail.getSourceUrl(),
            detail.getKeywords(),
            detail.getViewCount() == null ? 1 : detail.getViewCount() + 1
        );
    }

    public KbDto.QaResponse qa(KbDto.QaRequest request) {
        String normalizedQuestion = request.getQuestion().trim();
        String generalReply = generalReply(normalizedQuestion);
        if (generalReply != null) {
            return new KbDto.QaResponse(generalReply, List.of(), 0.98);
        }

        if (kbRagService.enabled()) {
            try {
                return kbRagService.qa(normalizedQuestion, request.getHistory());
            } catch (RuntimeException ignored) {
                // Fail-open to legacy QA to keep demo available when vector/LLM upstream is unstable.
            }
        }

        String question = buildSearchText(normalizedQuestion, request.getHistory()).toLowerCase(Locale.ROOT);
        List<ArticleQaView> candidates = jdbcTemplate.query(
            """
                select id, title, standard_answer, source_file_name, source_file_id, keywords_text
                  from kb_article
                 where is_deleted = 0 and publish_status = 'published'
                """,
            this::mapQaArticle
        );

        ArticleQaView best = candidates.stream()
            .max(Comparator.comparingInt(item -> score(item, question)))
            .orElse(null);

        if (best == null || score(best, question) <= 0) {
            return new KbDto.QaResponse("未检索到可靠依据", List.of(), 0.0);
        }

        double confidence = Math.min(0.95, 0.55 + score(best, question) * 0.12);
        KbDto.QaSource source = new KbDto.QaSource(
            best.articleId(),
            best.title(),
            best.sourceFileName(),
            buildSourceUrl(best.sourceFileId())
        );
        return new KbDto.QaResponse(best.standardAnswer(), List.of(source), confidence);
    }

    private KbDto.ArticleView mapArticle(ResultSet rs, int rowNum) throws SQLException {
        Long sourceFileId = rs.getObject("source_file_id", Long.class);
        return new KbDto.ArticleView(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("summary"),
            rs.getString("category_label"),
            rs.getString("publish_status"),
            rs.getString("version_no"),
            rs.getString("source_file_name"),
            sourceFileId,
            buildSourceUrl(sourceFileId),
            parseKeywords(rs.getString("keywords_text"))
        );
    }

    private ArticleQaView mapQaArticle(ResultSet rs, int rowNum) throws SQLException {
        return new ArticleQaView(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("standard_answer"),
            rs.getString("source_file_name"),
            rs.getObject("source_file_id", Long.class),
            parseKeywords(rs.getString("keywords_text"))
        );
    }

    private String buildSourceUrl(Long fileId) {
        if (fileId == null) {
            return "";
        }
        return "/api/v1/files/" + fileId + "/download";
    }

    private List<String> parseKeywords(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        String[] values = raw.split("[,，、\\s]+");
        List<String> result = new ArrayList<>();
        for (String value : values) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private int score(ArticleQaView article, String question) {
        int score = 0;
        for (String keyword : article.keywords()) {
            if (question.contains(keyword.toLowerCase(Locale.ROOT))) {
                score += 2;
            }
        }
        if (question.contains(article.title().toLowerCase(Locale.ROOT))) {
            score += 2;
        }
        return score;
    }

    private String buildSearchText(String question, List<KbDto.QaHistoryMessage> history) {
        if (!looksLikeFollowUp(question) || history == null || history.isEmpty()) {
            return question;
        }

        StringBuilder searchText = new StringBuilder(question);
        int start = Math.max(0, history.size() - 4);
        for (int i = start; i < history.size(); i++) {
            KbDto.QaHistoryMessage item = history.get(i);
            if (item == null || !StringUtils.hasText(item.getContent())) {
                continue;
            }
            searchText.append(' ').append(item.getContent().trim());
        }
        return searchText.toString();
    }

    private boolean looksLikeFollowUp(String question) {
        if (!StringUtils.hasText(question)) {
            return false;
        }
        String text = question.trim().toLowerCase(Locale.ROOT);
        return text.startsWith("那")
            || text.startsWith("那么")
            || text.startsWith("这个")
            || text.startsWith("这")
            || text.startsWith("它")
            || text.startsWith("该")
            || text.startsWith("上述")
            || text.startsWith("前面")
            || text.contains("刚才")
            || text.contains("上一")
            || text.contains("截止时间")
            || text.contains("材料呢")
            || text.contains("流程呢")
            || text.contains("怎么办")
            || text.contains("怎么做")
            || text.contains("还有");
    }

    private boolean isManagerLike(CurrentUser user) {
        return user.getRoles().stream().anyMatch(role ->
            "teacher_admin".equals(role) || "college_leader".equals(role) || "system_admin".equals(role) || "class_cadre".equals(role));
    }

    private String generalReply(String question) {
        String text = question.toLowerCase(Locale.ROOT);
        if (text.contains("你是谁") || text.contains("你叫什么")) {
            return "我是学院知识库助手，可以基于平台已发布条目帮你检索政策、模板和办理说明。";
        }
        if (text.contains("你好") || text.contains("hello") || text.equals("hi")) {
            return "你好，我是学院知识库助手。你可以直接问我奖助、证明、党团、培养方案等问题。";
        }
        if (text.contains("你能做什么") || text.contains("你会什么") || text.contains("怎么用")) {
            return "我可以回答院内政策问题、给出相关来源条目，并支持你连续追问同一主题。";
        }
        return null;
    }

    private String normalizeFilter(String value) {
        if (!StringUtils.hasText(value) || "all".equalsIgnoreCase(value)) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String format(Timestamp timestamp) {
        return timestamp == null ? "-" : DATETIME_FORMATTER.format(timestamp.toLocalDateTime());
    }

    private record ArticleQaView(Long articleId,
                                 String title,
                                 String standardAnswer,
                                 String sourceFileName,
                                 Long sourceFileId,
                                 List<String> keywords) {
    }
}

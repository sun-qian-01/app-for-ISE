package com.ise.platform.modules.kb;

import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.modules.kb.rag.LlmResponsesClient;
import com.ise.platform.modules.kb.rag.RagChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class KbService {

    private static final Logger log = LoggerFactory.getLogger(KbService.class);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_KEYWORD_CONTEXT_ARTICLES = 5;

    private final JdbcTemplate jdbcTemplate;
    private final LlmResponsesClient llmResponsesClient;

    public KbService(JdbcTemplate jdbcTemplate, LlmResponsesClient llmResponsesClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.llmResponsesClient = llmResponsesClient;
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
        if (generalReply != null && isPureGeneralQuestion(normalizedQuestion)) {
            return new KbDto.QaResponse(generalReply, List.of(), 0.98);
        }

        String question = buildSearchText(normalizedQuestion, request.getHistory()).toLowerCase(Locale.ROOT);
        List<ArticleQaView> candidates = jdbcTemplate.query(
            """
                select id, title, summary, category_label, standard_answer,
                       source_file_name, source_file_id, keywords_text
                  from kb_article
                 where is_deleted = 0 and publish_status = 'published'
                """,
            this::mapQaArticle
        );

        List<ScoredArticle> scoredArticles = candidates.stream()
            .map(article -> new ScoredArticle(article, score(article, question)))
            .filter(item -> item.score() > 0)
            .sorted(Comparator.comparingInt(ScoredArticle::score).reversed()
                .thenComparing(item -> item.article().articleId()))
            .toList();
        int minScore = scoredArticles.isEmpty() ? 0 : minKeywordScore(scoredArticles.get(0).score());
        List<ScoredArticle> matchedArticles = scoredArticles.stream()
            .filter(item -> item.score() >= minScore)
            .limit(MAX_KEYWORD_CONTEXT_ARTICLES)
            .toList();
        matchedArticles = narrowCertificateMatches(question, matchedArticles);

        if (matchedArticles.isEmpty()) {
            String answer = safeAiAnswer(normalizedQuestion, request.getHistory(), List.of(), true);
            if (StringUtils.hasText(answer) && !"未检索到可靠依据".equals(answer)) {
                return new KbDto.QaResponse(answer, List.of(), 0.25);
            }
            String fallback = generalFallback(normalizedQuestion);
            if (StringUtils.hasText(fallback)) {
                return new KbDto.QaResponse(fallback, List.of(), 0.45);
            }
            return new KbDto.QaResponse("未检索到可靠依据", List.of(), 0.0);
        }

        List<RagChunk> evidence = toKeywordEvidence(matchedArticles);
        String answer = safeAiAnswer(normalizedQuestion, request.getHistory(), evidence, false);
        double responseConfidence = confidence(matchedArticles.get(0).score());
        if (!StringUtils.hasText(answer)) {
            answer = fallbackAnswer(matchedArticles);
            if (shouldUseDetailGapAnswer(normalizedQuestion, answer)) {
                answer = detailGapAnswer(normalizedQuestion, matchedArticles);
                responseConfidence = Math.min(responseConfidence, 0.55d);
            }
        } else if (isNoReliableAnswer(answer)) {
            answer = insufficientEvidenceAnswer(matchedArticles);
            responseConfidence = Math.min(responseConfidence, 0.55d);
        } else if (shouldUseDetailGapAnswer(normalizedQuestion, answer)) {
            answer = detailGapAnswer(normalizedQuestion, matchedArticles);
            responseConfidence = Math.min(responseConfidence, 0.55d);
        }
        answer = appendIdentityIfAsked(normalizedQuestion, answer);

        return new KbDto.QaResponse(answer, toSources(matchedArticles), responseConfidence);
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
            rs.getString("summary"),
            rs.getString("category_label"),
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
        String title = normalizeText(article.title());
        String summary = normalizeText(article.summary());
        String category = normalizeText(article.categoryLabel());
        String answer = normalizeText(article.standardAnswer());
        List<String> keywords = article.keywords().stream().map(this::normalizeText).toList();

        if (StringUtils.hasText(title) && question.contains(title)) {
            score += 30;
        }
        for (String keyword : keywords) {
            if (StringUtils.hasText(keyword) && question.contains(keyword)) {
                score += 18;
            }
        }

        for (String term : expandSearchTerms(question)) {
            if (term.length() < 2) {
                continue;
            }
            if (title.contains(term)) {
                score += 12;
            }
            if (category.contains(term)) {
                score += 8;
            }
            for (String keyword : keywords) {
                if (keyword.equals(term)) {
                    score += 14;
                } else if (keyword.contains(term) || term.contains(keyword)) {
                    score += 9;
                }
            }
            if (summary.contains(term)) {
                score += 6;
            }
            if (answer.contains(term)) {
                score += 5;
            }
        }
        return score;
    }

    private int minKeywordScore(int topScore) {
        if (topScore >= 80) {
            return 35;
        }
        if (topScore >= 50) {
            return 25;
        }
        return Math.max(12, topScore);
    }

    private Set<String> expandSearchTerms(String text) {
        Set<String> terms = new LinkedHashSet<>();
        if (!StringUtils.hasText(text)) {
            return terms;
        }
        for (String token : text.split("[^\\p{IsHan}a-zA-Z0-9]+")) {
            String normalized = normalizeText(token);
            if (normalized.length() >= 2) {
                terms.add(normalized);
                addNgrams(terms, normalized);
            }
        }
        if (text.contains("入党") || text.contains("党员发展") || text.contains("发展对象") || text.contains("积极分子") || text.contains("预备党员") || text.contains("思想汇报")) {
            terms.addAll(List.of("党员", "党员发展", "发展对象", "积极分子", "预备党员", "思想汇报", "党团"));
        }
        if (text.contains("党员证明") || text.contains("党员身份证明") || (text.contains("党员") && (text.contains("证明") || text.contains("开具")))) {
            terms.addAll(List.of("党员证明", "证明", "开具", "模板", "身份信息"));
        }
        if (text.contains("团员") || text.contains("入团")) {
            terms.addAll(List.of("团员", "团员证明", "党团"));
        }
        if (text.contains("奖学金") || text.contains("国奖")) {
            terms.addAll(List.of("国家奖学金", "奖学金", "评定", "材料"));
        }
        if (text.contains("获奖")) {
            terms.addAll(List.of("获奖", "佐证", "材料"));
        }
        return terms;
    }

    private List<ScoredArticle> narrowCertificateMatches(String question, List<ScoredArticle> articles) {
        if (!isCertificateIssueQuestion(question) || articles.isEmpty()) {
            return articles;
        }
        List<ScoredArticle> narrowed = articles.stream()
            .filter(item -> isCertificateArticle(item.article()))
            .toList();
        if (narrowed.isEmpty()) {
            return articles;
        }
        int topScore = narrowed.get(0).score();
        int minScore = Math.max(1, topScore - 20);
        return narrowed.stream()
            .filter(item -> item.score() >= minScore)
            .limit(MAX_KEYWORD_CONTEXT_ARTICLES)
            .toList();
    }

    private boolean isCertificateIssueQuestion(String question) {
        String text = normalizeText(question);
        return text.contains("证明")
            || text.contains("身份证明")
            || text.contains("开具")
            || text.contains("模板");
    }

    private boolean isCertificateArticle(ArticleQaView article) {
        String text = normalizeText(article.title())
            + " "
            + normalizeText(article.summary())
            + " "
            + normalizeText(article.standardAnswer())
            + " "
            + String.join(" ", article.keywords().stream().map(this::normalizeText).toList());
        return text.contains("证明") || text.contains("开具") || text.contains("模板");
    }

    private void addNgrams(Set<String> terms, String token) {
        int[] codePoints = token.codePoints().toArray();
        if (codePoints.length <= 2) {
            return;
        }
        int maxLength = Math.min(6, codePoints.length);
        for (int length = 2; length <= maxLength; length++) {
            for (int start = 0; start + length <= codePoints.length; start++) {
                terms.add(new String(codePoints, start, length));
            }
        }
    }

    private String safeAiAnswer(String question,
                                List<KbDto.QaHistoryMessage> history,
                                List<RagChunk> evidence,
                                boolean allowGeneralReply) {
        try {
            String answer = llmResponsesClient.answer(question, history, evidence, allowGeneralReply);
            String normalizedAnswer = StringUtils.hasText(answer) ? answer.trim() : "";
            log.info("kb qa llm completed, evidenceCount={}, allowGeneralReply={}, answerPresent={}",
                evidence.size(), allowGeneralReply, StringUtils.hasText(normalizedAnswer));
            return normalizedAnswer;
        } catch (RuntimeException ex) {
            log.warn("kb qa llm failed, evidenceCount={}, allowGeneralReply={}, reason={}",
                evidence.size(), allowGeneralReply, ex.getMessage());
            return "";
        }
    }

    private List<RagChunk> toKeywordEvidence(List<ScoredArticle> articles) {
        List<RagChunk> evidence = new ArrayList<>();
        for (ScoredArticle item : articles) {
            ArticleQaView article = item.article();
            evidence.add(new RagChunk(
                "keyword-" + article.articleId(),
                article.articleId(),
                article.title(),
                article.sourceFileName(),
                article.sourceFileId(),
                article.categoryLabel(),
                "published",
                "",
                buildEvidenceText(article),
                List.of(),
                Math.min(0.99d, item.score() / 100.0d)
            ));
        }
        return evidence;
    }

    private String buildEvidenceText(ArticleQaView article) {
        return """
            标题：%s
            分类：%s
            摘要：%s
            内容：%s
            关键词：%s
            """.formatted(
            article.title(),
            article.categoryLabel(),
            article.summary(),
            article.standardAnswer(),
            String.join("、", article.keywords())
        );
    }

    private List<KbDto.QaSource> toSources(List<ScoredArticle> articles) {
        Map<Long, KbDto.QaSource> sources = new LinkedHashMap<>();
        for (ScoredArticle item : articles) {
            ArticleQaView article = item.article();
            sources.putIfAbsent(article.articleId(), new KbDto.QaSource(
                article.articleId(),
                article.title(),
                article.sourceFileName(),
                buildSourceUrl(article.sourceFileId())
            ));
        }
        return new ArrayList<>(sources.values());
    }

    private String fallbackAnswer(List<ScoredArticle> articles) {
        ArticleQaView best = articles.get(0).article();
        return StringUtils.hasText(best.standardAnswer()) ? best.standardAnswer() : "未检索到可靠依据";
    }

    private String insufficientEvidenceAnswer(List<ScoredArticle> articles) {
        ArticleQaView best = articles.get(0).article();
        if (!StringUtils.hasText(best.standardAnswer())) {
            return "已找到相关知识条目，但条目没有提供足够细节，请以原文通知或学院老师确认为准。";
        }
        return "已找到相关知识条目，但条目没有直接说明你追问的具体细节。现有依据仅说明："
            + best.standardAnswer()
            + " 请以原文通知或学院老师确认为准。";
    }

    private boolean isNoReliableAnswer(String answer) {
        return "未检索到可靠依据".equals(answer == null ? "" : answer.trim());
    }

    private boolean shouldUseDetailGapAnswer(String question, String answer) {
        if (!asksSpecificMissingDetail(question) || !StringUtils.hasText(answer)) {
            return false;
        }
        String normalizedAnswer = normalizeText(answer);
        return !normalizedAnswer.contains("未说明")
            && !normalizedAnswer.contains("没有说明")
            && !normalizedAnswer.contains("没有直接说明")
            && !normalizedAnswer.contains("未提供")
            && !normalizedAnswer.contains("不明确")
            && !normalizedAnswer.contains("未明确");
    }

    private boolean asksSpecificMissingDetail(String question) {
        String text = normalizeText(question);
        return text.contains("什么水平")
            || text.contains("达到什么")
            || text.contains("达到多少")
            || text.contains("多少分")
            || text.contains("绩点")
            || text.contains("排名第")
            || text.contains("前几")
            || text.contains("比例")
            || text.contains("门槛")
            || text.contains("什么形式")
            || text.contains("哪种形式")
            || text.contains("什么格式")
            || text.contains("哪种格式")
            || text.contains("用什么形式")
            || text.contains("怎么提交佐证");
    }

    private String detailGapAnswer(String question, List<ScoredArticle> articles) {
        ArticleQaView best = articles.get(0).article();
        String policyQuestion = policyQuestionPart(question);
        return "已找到相关知识条目「"
            + best.title()
            + "」，但条目没有直接说明「"
            + policyQuestion
            + "」这个具体细节。现有依据仅说明："
            + (StringUtils.hasText(best.standardAnswer()) ? best.standardAnswer() : best.summary())
            + " 请以原文通知或学院老师确认为准。";
    }

    private String policyQuestionPart(String question) {
        String result = question
            .replace("你是谁？", "")
            .replace("你是谁?", "")
            .replace("你是什么模型？", "")
            .replace("你是什么模型?", "")
            .replace("什么模型？", "")
            .replace("什么模型?", "")
            .trim();
        return StringUtils.hasText(result) ? result : question;
    }

    private double confidence(int score) {
        return Math.min(0.95d, 0.55d + Math.min(score, 40) * 0.01d);
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String buildSearchText(String question, List<KbDto.QaHistoryMessage> history) {
        if (!shouldUseHistoryForSearch(question, history)) {
            return question;
        }

        StringBuilder searchText = new StringBuilder(question);
        int start = Math.max(0, history.size() - 4);
        for (int i = start; i < history.size(); i++) {
            KbDto.QaHistoryMessage item = history.get(i);
            if (item == null
                || !"user".equalsIgnoreCase(item.getRole())
                || !StringUtils.hasText(item.getContent())) {
                continue;
            }
            searchText.append(' ').append(item.getContent().trim());
        }
        return searchText.toString();
    }

    private boolean shouldUseHistoryForSearch(String question, List<KbDto.QaHistoryMessage> history) {
        if (history == null || history.isEmpty()) {
            return false;
        }
        if (looksLikeFollowUp(question)) {
            return true;
        }
        return !hasSpecificTopicAnchor(question) && hasGenericPolicyDetailIntent(question);
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

    private boolean hasSpecificTopicAnchor(String question) {
        String text = normalizeText(question);
        return text.contains("国家奖学金")
            || text.contains("奖学金")
            || text.contains("国奖")
            || text.contains("入党")
            || text.contains("党员")
            || text.contains("团员")
            || text.contains("入团")
            || text.contains("休学")
            || text.contains("复学")
            || text.contains("在读证明")
            || text.contains("成绩证明")
            || text.contains("困难认定")
            || text.contains("助学金");
    }

    private boolean hasGenericPolicyDetailIntent(String question) {
        String text = normalizeText(question);
        return text.contains("成绩")
            || text.contains("获奖")
            || text.contains("材料")
            || text.contains("形式")
            || text.contains("水平")
            || text.contains("要求")
            || text.contains("条件")
            || text.contains("排名")
            || text.contains("提交")
            || text.contains("申请")
            || text.contains("截止")
            || text.contains("时间")
            || text.contains("名额")
            || text.contains("怎么拿");
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
        if (text.contains("你是什么模型") || text.contains("什么模型")) {
            return "我是学院知识库助手，当前会调用系统配置的 AI 服务来整理回答。";
        }
        return null;
    }

    private boolean isPureGeneralQuestion(String question) {
        return !hasGenericPolicyDetailIntent(question) && !hasSpecificTopicAnchor(question);
    }

    private String appendIdentityIfAsked(String question, String answer) {
        if (!StringUtils.hasText(answer) || !asksAssistantIdentity(question) || answer.contains("学院知识库助手")) {
            return answer;
        }
        return answer + "\n\n另外，我是学院知识库助手，可以基于平台已发布条目帮你检索政策、模板和办理说明。";
    }

    private boolean asksAssistantIdentity(String question) {
        String text = normalizeText(question);
        return text.contains("你是谁") || text.contains("你叫什么") || text.contains("你是什么模型") || text.contains("什么模型");
    }

    private String generalFallback(String question) {
        String text = normalizeText(question);
        if (text.contains("何意味") || text.contains("什么意思") || text.contains("啥意思") || text.contains("什么含义")) {
            return "这句话是在询问“是什么意思”。如果你想问某个学院政策或流程，请补充完整关键词。";
        }
        if (text.contains("神了") || text.contains("厉害") || text.contains("离谱")) {
            return "我理解这是对上一轮回答的反馈。如果答案不符合你的问题，可以直接换一种问法或补充关键词。";
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
                                 String summary,
                                 String categoryLabel,
                                 String standardAnswer,
                                 String sourceFileName,
                                 Long sourceFileId,
                                 List<String> keywords) {
    }

    private record ScoredArticle(ArticleQaView article, int score) {
    }
}

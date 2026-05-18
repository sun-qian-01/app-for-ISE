package com.ise.platform.modules.kb;

import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class KbService {

    private final List<ArticleEntity> articles = List.of(
        new ArticleEntity(1L, "国家奖学金评定流程说明", "包含申请资格、材料清单和公示流程", "scholarship", "published", "v3", "国家奖学金通常需要提交申请表、成绩证明和相关获奖材料。", "国家奖学金评定办法.pdf", 12L),
        new ArticleEntity(2L, "休学与复学办理指南", "说明休学申请条件、复学材料和学院审核路径", "student_status", "published", "v2", "休学需提供申请书和相关证明材料，复学时按学院通知提交材料。", "学籍异动办理指南.docx", 13L),
        new ArticleEntity(3L, "党员发展阶段材料清单", "汇总积极分子、发展对象、预备党员所需材料", "party_league", "published", "v4", "阶段材料包括思想汇报、谈话记录和志愿服务记录。", "党员发展材料清单.xlsx", 14L)
    );

    public PagedData<KbDto.ArticleView> listArticles(CurrentUser user,
                                                     int pageNo,
                                                     int pageSize,
                                                     String keyword,
                                                     Long categoryId,
                                                     String publishStatus,
                                                     String tag) {
        List<KbDto.ArticleView> filtered = articles.stream()
            .sorted(Comparator.comparing(ArticleEntity::id))
            .filter(item -> !StringUtils.hasText(keyword) || containsIgnoreCase(item.title(), keyword) || containsIgnoreCase(item.summary(), keyword))
            .filter(item -> !StringUtils.hasText(publishStatus) || item.publishStatus().equalsIgnoreCase(publishStatus))
            .map(item -> new KbDto.ArticleView(item.id(), item.title(), item.summary(), item.category(), item.publishStatus(), item.versionNo()))
            .toList();
        return paginate(filtered, pageNo, pageSize);
    }

    public KbDto.QaResponse qa(KbDto.QaRequest request) {
        String question = request.getQuestion().toLowerCase(Locale.ROOT);
        if (question.contains("奖学金")) {
            ArticleEntity article = articles.get(0);
            return new KbDto.QaResponse(
                "国家奖学金通常需要提交申请表、成绩证明和相关获奖材料，具体以学院当年通知为准。",
                List.of(new KbDto.QaSource(article.id(), article.title(), article.fileName(), "/api/v1/files/" + article.fileId() + "/download")),
                0.86
            );
        }
        if (question.contains("党员") || question.contains("思想汇报")) {
            ArticleEntity article = articles.get(2);
            return new KbDto.QaResponse(
                "请按当前阶段提交思想汇报和相关证明，并关注支部审核意见。",
                List.of(new KbDto.QaSource(article.id(), article.title(), article.fileName(), "/api/v1/files/" + article.fileId() + "/download")),
                0.82
            );
        }
        return new KbDto.QaResponse("未检索到可靠依据", List.of(), 0.0);
    }

    private PagedData<KbDto.ArticleView> paginate(List<KbDto.ArticleView> source, int pageNo, int pageSize) {
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        int from = (safePageNo - 1) * safePageSize;
        if (from >= source.size()) {
            return new PagedData<>(List.of(), safePageNo, safePageSize, source.size());
        }
        int to = Math.min(from + safePageSize, source.size());
        return new PagedData<>(new ArrayList<>(source.subList(from, to)), safePageNo, safePageSize, source.size());
    }

    private boolean containsIgnoreCase(String source, String target) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(target.toLowerCase(Locale.ROOT));
    }

    private record ArticleEntity(Long id,
                                 String title,
                                 String summary,
                                 String category,
                                 String publishStatus,
                                 String versionNo,
                                 String standardAnswer,
                                 String fileName,
                                 Long fileId) {
    }
}

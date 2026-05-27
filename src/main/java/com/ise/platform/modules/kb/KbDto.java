package com.ise.platform.modules.kb;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class KbDto {

    private KbDto() {
    }

    public static class ArticleView {
        private Long articleId;
        private String title;
        private String summary;
        private String categoryLabel;
        private String publishStatus;
        private String version;
        private String source;
        private Long sourceFileId;
        private String sourceUrl;
        private List<String> keywords;

        public ArticleView(Long articleId,
                           String title,
                           String summary,
                           String categoryLabel,
                           String publishStatus,
                           String version,
                           String source,
                           Long sourceFileId,
                           String sourceUrl,
                           List<String> keywords) {
            this.articleId = articleId;
            this.title = title;
            this.summary = summary;
            this.categoryLabel = categoryLabel;
            this.publishStatus = publishStatus;
            this.version = version;
            this.source = source;
            this.sourceFileId = sourceFileId;
            this.sourceUrl = sourceUrl;
            this.keywords = keywords;
        }

        public Long getArticleId() {
            return articleId;
        }

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public String getCategoryLabel() {
            return categoryLabel;
        }

        public String getPublishStatus() {
            return publishStatus;
        }

        public String getVersion() {
            return version;
        }

        public String getSource() {
            return source;
        }

        public Long getSourceFileId() {
            return sourceFileId;
        }

        public String getSourceUrl() {
            return sourceUrl;
        }

        public List<String> getKeywords() {
            return keywords;
        }
    }

    public static class ArticleDetailView {
        private Long articleId;
        private String title;
        private String summary;
        private String categoryLabel;
        private String publishStatus;
        private String version;
        private String content;
        private String source;
        private Long sourceFileId;
        private String sourceUrl;
        private List<String> keywords;
        private Integer viewCount;

        public ArticleDetailView(Long articleId,
                                 String title,
                                 String summary,
                                 String categoryLabel,
                                 String publishStatus,
                                 String version,
                                 String content,
                                 String source,
                                 Long sourceFileId,
                                 String sourceUrl,
                                 List<String> keywords,
                                 Integer viewCount) {
            this.articleId = articleId;
            this.title = title;
            this.summary = summary;
            this.categoryLabel = categoryLabel;
            this.publishStatus = publishStatus;
            this.version = version;
            this.content = content;
            this.source = source;
            this.sourceFileId = sourceFileId;
            this.sourceUrl = sourceUrl;
            this.keywords = keywords;
            this.viewCount = viewCount;
        }

        public Long getArticleId() {
            return articleId;
        }

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public String getCategoryLabel() {
            return categoryLabel;
        }

        public String getPublishStatus() {
            return publishStatus;
        }

        public String getVersion() {
            return version;
        }

        public String getContent() {
            return content;
        }

        public String getSource() {
            return source;
        }

        public Long getSourceFileId() {
            return sourceFileId;
        }

        public String getSourceUrl() {
            return sourceUrl;
        }

        public List<String> getKeywords() {
            return keywords;
        }

        public Integer getViewCount() {
            return viewCount;
        }
    }

    public static class TemplateView {
        private Long templateId;
        private String name;
        private String categoryLabel;
        private String fileType;
        private String updatedAt;
        private String description;
        private Long fileId;
        private String fileUrl;

        public TemplateView(Long templateId,
                            String name,
                            String categoryLabel,
                            String fileType,
                            String updatedAt,
                            String description,
                            Long fileId,
                            String fileUrl) {
            this.templateId = templateId;
            this.name = name;
            this.categoryLabel = categoryLabel;
            this.fileType = fileType;
            this.updatedAt = updatedAt;
            this.description = description;
            this.fileId = fileId;
            this.fileUrl = fileUrl;
        }

        public Long getTemplateId() {
            return templateId;
        }

        public String getName() {
            return name;
        }

        public String getCategoryLabel() {
            return categoryLabel;
        }

        public String getFileType() {
            return fileType;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getDescription() {
            return description;
        }

        public Long getFileId() {
            return fileId;
        }

        public String getFileUrl() {
            return fileUrl;
        }
    }

    public static class QaRequest {
        @NotBlank(message = "question is required")
        private String question;
        private Long categoryId;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }
    }

    public static class QaResponse {
        private String answer;
        private List<QaSource> sources;
        private double confidence;

        public QaResponse(String answer, List<QaSource> sources, double confidence) {
            this.answer = answer;
            this.sources = sources;
            this.confidence = confidence;
        }

        public String getAnswer() {
            return answer;
        }

        public List<QaSource> getSources() {
            return sources;
        }

        public double getConfidence() {
            return confidence;
        }
    }

    public static class QaSource {
        private Long articleId;
        private String title;
        private String fileName;
        private String sourceUrl;

        public QaSource(Long articleId, String title, String fileName, String sourceUrl) {
            this.articleId = articleId;
            this.title = title;
            this.fileName = fileName;
            this.sourceUrl = sourceUrl;
        }

        public Long getArticleId() {
            return articleId;
        }

        public String getTitle() {
            return title;
        }

        public String getFileName() {
            return fileName;
        }

        public String getSourceUrl() {
            return sourceUrl;
        }
    }
}

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
        private String category;
        private String publishStatus;
        private String versionNo;

        public ArticleView(Long articleId, String title, String summary, String category, String publishStatus, String versionNo) {
            this.articleId = articleId;
            this.title = title;
            this.summary = summary;
            this.category = category;
            this.publishStatus = publishStatus;
            this.versionNo = versionNo;
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

        public String getCategory() {
            return category;
        }

        public String getPublishStatus() {
            return publishStatus;
        }

        public String getVersionNo() {
            return versionNo;
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

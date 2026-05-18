package com.ise.platform.modules.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public final class ApplicationDto {

    private ApplicationDto() {
    }

    public static class CreateRequest {
        @NotBlank(message = "applicationType is required")
        private String applicationType;

        @NotNull(message = "templateId is required")
        private Long templateId;

        @NotBlank(message = "title is required")
        private String title;

        private String purpose;
        private Map<String, Object> formData;

        public String getApplicationType() {
            return applicationType;
        }

        public void setApplicationType(String applicationType) {
            this.applicationType = applicationType;
        }

        public Long getTemplateId() {
            return templateId;
        }

        public void setTemplateId(Long templateId) {
            this.templateId = templateId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPurpose() {
            return purpose;
        }

        public void setPurpose(String purpose) {
            this.purpose = purpose;
        }

        public Map<String, Object> getFormData() {
            return formData;
        }

        public void setFormData(Map<String, Object> formData) {
            this.formData = formData;
        }
    }

    public static class ApplicationView {
        private Long id;
        private String applicationNo;
        private String applicationType;
        private String title;
        private String purpose;
        private String status;
        private String currentApprover;
        private String submittedAt;

        public ApplicationView(Long id,
                               String applicationNo,
                               String applicationType,
                               String title,
                               String purpose,
                               String status,
                               String currentApprover,
                               String submittedAt) {
            this.id = id;
            this.applicationNo = applicationNo;
            this.applicationType = applicationType;
            this.title = title;
            this.purpose = purpose;
            this.status = status;
            this.currentApprover = currentApprover;
            this.submittedAt = submittedAt;
        }

        public Long getId() {
            return id;
        }

        public String getApplicationNo() {
            return applicationNo;
        }

        public String getApplicationType() {
            return applicationType;
        }

        public String getTitle() {
            return title;
        }

        public String getPurpose() {
            return purpose;
        }

        public String getStatus() {
            return status;
        }

        public String getCurrentApprover() {
            return currentApprover;
        }

        public String getSubmittedAt() {
            return submittedAt;
        }
    }

    public static class CreateResponse {
        private String applicationNo;
        private String status;
        private String currentApprover;
        private Long previewFileId;

        public CreateResponse(String applicationNo, String status, String currentApprover, Long previewFileId) {
            this.applicationNo = applicationNo;
            this.status = status;
            this.currentApprover = currentApprover;
            this.previewFileId = previewFileId;
        }

        public String getApplicationNo() {
            return applicationNo;
        }

        public String getStatus() {
            return status;
        }

        public String getCurrentApprover() {
            return currentApprover;
        }

        public Long getPreviewFileId() {
            return previewFileId;
        }
    }
}

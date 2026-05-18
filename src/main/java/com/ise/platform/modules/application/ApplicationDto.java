package com.ise.platform.modules.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
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

    public static class ActionResponse {
        private String applicationNo;
        private String status;
        private String currentApprover;

        public ActionResponse(String applicationNo, String status, String currentApprover) {
            this.applicationNo = applicationNo;
            this.status = status;
            this.currentApprover = currentApprover;
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
    }

    public static class RevokeRequest {
        @NotBlank(message = "reason is required")
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class ApprovalActionRequest {
        @NotBlank(message = "comment is required")
        private String comment;

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    public static class ApplicationDetailView {
        private Long id;
        private String applicationNo;
        private String applicationType;
        private String title;
        private String purpose;
        private String status;
        private String currentApprover;
        private String submittedAt;
        private Map<String, Object> formData;
        private GeneratedFile generatedFile;
        private List<ApprovalRecord> approvalRecords;

        public ApplicationDetailView(Long id,
                                     String applicationNo,
                                     String applicationType,
                                     String title,
                                     String purpose,
                                     String status,
                                     String currentApprover,
                                     String submittedAt,
                                     Map<String, Object> formData,
                                     GeneratedFile generatedFile,
                                     List<ApprovalRecord> approvalRecords) {
            this.id = id;
            this.applicationNo = applicationNo;
            this.applicationType = applicationType;
            this.title = title;
            this.purpose = purpose;
            this.status = status;
            this.currentApprover = currentApprover;
            this.submittedAt = submittedAt;
            this.formData = formData;
            this.generatedFile = generatedFile;
            this.approvalRecords = approvalRecords;
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

        public Map<String, Object> getFormData() {
            return formData;
        }

        public GeneratedFile getGeneratedFile() {
            return generatedFile;
        }

        public List<ApprovalRecord> getApprovalRecords() {
            return approvalRecords;
        }
    }

    public static class GeneratedFile {
        private Long fileId;
        private String fileName;

        public GeneratedFile(Long fileId, String fileName) {
            this.fileId = fileId;
            this.fileName = fileName;
        }

        public Long getFileId() {
            return fileId;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public static class ApprovalRecord {
        private String nodeName;
        private String operator;
        private String action;
        private String opinion;
        private String operatedAt;

        public ApprovalRecord(String nodeName, String operator, String action, String opinion, String operatedAt) {
            this.nodeName = nodeName;
            this.operator = operator;
            this.action = action;
            this.opinion = opinion;
            this.operatedAt = operatedAt;
        }

        public String getNodeName() {
            return nodeName;
        }

        public String getOperator() {
            return operator;
        }

        public String getAction() {
            return action;
        }

        public String getOpinion() {
            return opinion;
        }

        public String getOperatedAt() {
            return operatedAt;
        }
    }
}

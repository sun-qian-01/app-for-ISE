package com.ise.platform.modules.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class StudentDto {

    private StudentDto() {
    }

    public static class MeProfileView {
        private StudentView student;
        private List<TagView> tags;
        private GrowthSummary growthSummary;
        private int todoCount;
        private int unreadNoticeCount;
        private String currentPartyStage;

        public MeProfileView(StudentView student,
                             List<TagView> tags,
                             GrowthSummary growthSummary,
                             int todoCount,
                             int unreadNoticeCount,
                             String currentPartyStage) {
            this.student = student;
            this.tags = tags;
            this.growthSummary = growthSummary;
            this.todoCount = todoCount;
            this.unreadNoticeCount = unreadNoticeCount;
            this.currentPartyStage = currentPartyStage;
        }

        public StudentView getStudent() {
            return student;
        }

        public List<TagView> getTags() {
            return tags;
        }

        public GrowthSummary getGrowthSummary() {
            return growthSummary;
        }

        public int getTodoCount() {
            return todoCount;
        }

        public int getUnreadNoticeCount() {
            return unreadNoticeCount;
        }

        public String getCurrentPartyStage() {
            return currentPartyStage;
        }
    }

    public static class StudentView {
        private Long id;
        private String studentNo;
        private String name;
        private String grade;
        private String major;
        private String className;
        private String politicalStatus;
        private String phoneMasked;

        public StudentView(Long id,
                           String studentNo,
                           String name,
                           String grade,
                           String major,
                           String className,
                           String politicalStatus,
                           String phoneMasked) {
            this.id = id;
            this.studentNo = studentNo;
            this.name = name;
            this.grade = grade;
            this.major = major;
            this.className = className;
            this.politicalStatus = politicalStatus;
            this.phoneMasked = phoneMasked;
        }

        public Long getId() {
            return id;
        }

        public String getStudentNo() {
            return studentNo;
        }

        public String getName() {
            return name;
        }

        public String getGrade() {
            return grade;
        }

        public String getMajor() {
            return major;
        }

        public String getClassName() {
            return className;
        }

        public String getPoliticalStatus() {
            return politicalStatus;
        }

        public String getPhoneMasked() {
            return phoneMasked;
        }
    }

    public static class TagView {
        private Long id;
        private String tagName;

        public TagView(Long id, String tagName) {
            this.id = id;
            this.tagName = tagName;
        }

        public Long getId() {
            return id;
        }

        public String getTagName() {
            return tagName;
        }
    }

    public static class GrowthSummary {
        private int competitionCount;
        private int volunteerHours;
        private int honorCount;

        public GrowthSummary(int competitionCount, int volunteerHours, int honorCount) {
            this.competitionCount = competitionCount;
            this.volunteerHours = volunteerHours;
            this.honorCount = honorCount;
        }

        public int getCompetitionCount() {
            return competitionCount;
        }

        public int getVolunteerHours() {
            return volunteerHours;
        }

        public int getHonorCount() {
            return honorCount;
        }
    }

    public static class GrowthRecordView {
        private Long id;
        private String recordType;
        private String title;
        private String startDate;
        private String endDate;
        private String description;
        private Long proofFileId;

        public GrowthRecordView(Long id,
                                String recordType,
                                String title,
                                String startDate,
                                String endDate,
                                String description,
                                Long proofFileId) {
            this.id = id;
            this.recordType = recordType;
            this.title = title;
            this.startDate = startDate;
            this.endDate = endDate;
            this.description = description;
            this.proofFileId = proofFileId;
        }

        public Long getId() {
            return id;
        }

        public String getRecordType() {
            return recordType;
        }

        public String getTitle() {
            return title;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public String getDescription() {
            return description;
        }

        public Long getProofFileId() {
            return proofFileId;
        }
    }

    public static class StudentListItemView {
        private Long id;
        private String studentNo;
        private String name;
        private String grade;
        private String major;
        private String className;
        private String politicalStatus;
        private String status;
        private String phoneMasked;
        private List<String> tags;

        public StudentListItemView(Long id,
                                   String studentNo,
                                   String name,
                                   String grade,
                                   String major,
                                   String className,
                                   String politicalStatus,
                                   String status,
                                   String phoneMasked,
                                   List<String> tags) {
            this.id = id;
            this.studentNo = studentNo;
            this.name = name;
            this.grade = grade;
            this.major = major;
            this.className = className;
            this.politicalStatus = politicalStatus;
            this.status = status;
            this.phoneMasked = phoneMasked;
            this.tags = tags;
        }

        public Long getId() {
            return id;
        }

        public String getStudentNo() {
            return studentNo;
        }

        public String getName() {
            return name;
        }

        public String getGrade() {
            return grade;
        }

        public String getMajor() {
            return major;
        }

        public String getClassName() {
            return className;
        }

        public String getPoliticalStatus() {
            return politicalStatus;
        }

        public String getStatus() {
            return status;
        }

        public String getPhoneMasked() {
            return phoneMasked;
        }

        public List<String> getTags() {
            return tags;
        }
    }

    public static class StudentDetailView {
        private StudentListItemView base;
        private String emailMasked;
        private SensitiveInfo sensitiveInfo;

        public StudentDetailView(StudentListItemView base, String emailMasked, SensitiveInfo sensitiveInfo) {
            this.base = base;
            this.emailMasked = emailMasked;
            this.sensitiveInfo = sensitiveInfo;
        }

        public StudentListItemView getBase() {
            return base;
        }

        public String getEmailMasked() {
            return emailMasked;
        }

        public SensitiveInfo getSensitiveInfo() {
            return sensitiveInfo;
        }
    }

    public static class SensitiveInfo {
        private String phone;
        private String email;

        public SensitiveInfo(String phone, String email) {
            this.phone = phone;
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return email;
        }
    }

    public static class UpdateStudentRequest {
        private String phone;
        private String email;
        private String politicalStatus;
        private String status;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPoliticalStatus() {
            return politicalStatus;
        }

        public void setPoliticalStatus(String politicalStatus) {
            this.politicalStatus = politicalStatus;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class CreateGrowthRecordRequest {
        @NotBlank(message = "recordType is required")
        private String recordType;

        @NotBlank(message = "title is required")
        private String title;

        @NotBlank(message = "startDate is required")
        private String startDate;

        private String endDate;
        private String description;
        private Long proofFileId;

        public String getRecordType() {
            return recordType;
        }

        public void setRecordType(String recordType) {
            this.recordType = recordType;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getProofFileId() {
            return proofFileId;
        }

        public void setProofFileId(Long proofFileId) {
            this.proofFileId = proofFileId;
        }
    }

    public static class UpdateTagsRequest {
        @NotNull(message = "tagIds is required")
        private List<Long> tagIds;

        public List<Long> getTagIds() {
            return tagIds;
        }

        public void setTagIds(List<Long> tagIds) {
            this.tagIds = tagIds;
        }
    }

    public static class BatchRegisterStudentRequest {
        private List<BatchRegisterStudentRow> rows;

        public List<BatchRegisterStudentRow> getRows() {
            return rows;
        }

        public void setRows(List<BatchRegisterStudentRow> rows) {
            this.rows = rows;
        }
    }

    public static class BatchRegisterStudentRow {
        private String studentNo;
        private String name;
        private String grade;
        private String major;
        private String className;
        private String phone;
        private String email;
        private String politicalStatusLabel;

        public String getStudentNo() {
            return studentNo;
        }

        public void setStudentNo(String studentNo) {
            this.studentNo = studentNo;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public String getMajor() {
            return major;
        }

        public void setMajor(String major) {
            this.major = major;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPoliticalStatusLabel() {
            return politicalStatusLabel;
        }

        public void setPoliticalStatusLabel(String politicalStatusLabel) {
            this.politicalStatusLabel = politicalStatusLabel;
        }
    }

    public static class BatchRegisterStudentResponse {
        private int successCount;
        private int skippedCount;
        private int failedCount;
        private List<String> messages;

        public BatchRegisterStudentResponse(int successCount, int skippedCount, int failedCount, List<String> messages) {
            this.successCount = successCount;
            this.skippedCount = skippedCount;
            this.failedCount = failedCount;
            this.messages = messages;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public List<String> getMessages() {
            return messages;
        }
    }

    public static class ImportTaskCreateRequest {
        @NotBlank(message = "importType is required")
        private String importType;

        @NotNull(message = "fileId is required")
        private Long fileId;

        public String getImportType() {
            return importType;
        }

        public void setImportType(String importType) {
            this.importType = importType;
        }

        public Long getFileId() {
            return fileId;
        }

        public void setFileId(Long fileId) {
            this.fileId = fileId;
        }
    }

    public static class ImportTaskCreateView {
        private String taskNo;
        private String status;
        private String createdAt;

        public ImportTaskCreateView(String taskNo, String status, String createdAt) {
            this.taskNo = taskNo;
            this.status = status;
            this.createdAt = createdAt;
        }

        public String getTaskNo() {
            return taskNo;
        }

        public String getStatus() {
            return status;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    public static class ImportTaskView {
        private String taskNo;
        private String importType;
        private Long fileId;
        private String status;
        private String createdAt;
        private String finishedAt;
        private String message;

        public ImportTaskView(String taskNo,
                              String importType,
                              Long fileId,
                              String status,
                              String createdAt,
                              String finishedAt,
                              String message) {
            this.taskNo = taskNo;
            this.importType = importType;
            this.fileId = fileId;
            this.status = status;
            this.createdAt = createdAt;
            this.finishedAt = finishedAt;
            this.message = message;
        }

        public String getTaskNo() {
            return taskNo;
        }

        public String getImportType() {
            return importType;
        }

        public Long getFileId() {
            return fileId;
        }

        public String getStatus() {
            return status;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getFinishedAt() {
            return finishedAt;
        }

        public String getMessage() {
            return message;
        }
    }
}

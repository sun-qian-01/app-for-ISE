package com.ise.platform.modules.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ApplicationService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ApplicationService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public PagedData<ApplicationDto.ApplicationView> myApplications(CurrentUser user,
                                                                    int pageNo,
                                                                    int pageSize,
                                                                    String applicationType,
                                                                    String status) {
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        String typeFilter = normalizeFilter(applicationType);
        String statusFilter = normalizeFilter(status);
        long total = countMyApplications(user, typeFilter, statusFilter);

        List<ApplicationDto.ApplicationView> records = jdbcTemplate.query("""
                select a.id, a.application_no, a.application_type, a.title, a.purpose, a.status,
                       coalesce(u.real_name, '-') as current_approver, a.submitted_at
                  from biz_application a
                  left join sys_user u on u.id = a.current_approver_id
                 where a.is_deleted = 0
                   and a.applicant_user_id = ?
                   and (? is null or lower(a.application_type) = ?)
                   and (? is null or lower(a.status) = ?)
                 order by a.submitted_at desc, a.id desc
                 limit ? offset ?
                """,
            this::mapApplicationView,
            user.getId(),
            typeFilter, typeFilter,
            statusFilter, statusFilter,
            safePageSize,
            (safePageNo - 1) * safePageSize
        );

        return new PagedData<>(records, safePageNo, safePageSize, total);
    }

    @Transactional
    public ApplicationDto.CreateResponse create(CurrentUser user, ApplicationDto.CreateRequest request) {
        Long applicationId = nextId("biz_application");
        String applicationNo = buildApplicationNo(applicationId);
        String formDataJson = toJson(request.getFormData());
        Long approverId = 8L;
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update("""
                insert into biz_application (
                    id, application_no, application_type, template_id, applicant_user_id, student_id,
                    title, purpose, form_data_json, status, current_approver_id, submitted_at,
                    created_at, updated_at, is_deleted
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, 'submitted', ?, ?, ?, ?, 0)
                """,
            applicationId,
            applicationNo,
            request.getApplicationType().toLowerCase(Locale.ROOT),
            request.getTemplateId(),
            user.getId(),
            user.getStudentId(),
            request.getTitle(),
            request.getPurpose(),
            formDataJson,
            approverId,
            Timestamp.valueOf(now),
            Timestamp.valueOf(now),
            Timestamp.valueOf(now)
        );

        insertApprovalRecord(applicationId, user.getId(), "submit", "提交申请", null, "submitted", now);
        return new ApplicationDto.CreateResponse(applicationNo, "submitted", "辅导员 李老师", null);
    }

    public ApplicationDto.ApplicationDetailView detail(CurrentUser user, Long applicationId) {
        ApplicationEntity entity = findById(applicationId);
        boolean isOwner = entity.applicantUserId().equals(user.getId());
        if (!isOwner && !isManager(user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "no permission to view this application");
        }

        return new ApplicationDto.ApplicationDetailView(
            entity.id(),
            entity.applicationNo(),
            entity.applicationType(),
            entity.title(),
            entity.purpose(),
            entity.status(),
            entity.currentApprover(),
            format(entity.submittedAt()),
            fromJson(entity.formDataJson()),
            null,
            approvalRecords(applicationId)
        );
    }

    @Transactional
    public ApplicationDto.ActionResponse revoke(CurrentUser user, Long applicationId, ApplicationDto.RevokeRequest request) {
        ApplicationEntity entity = findById(applicationId);
        if (!entity.applicantUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "only applicant can revoke");
        }
        if (!isRevocable(entity.status())) {
            throw new BusinessException(ErrorCode.STATUS_CONFLICT, "application status does not allow revoke");
        }

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("""
                update biz_application
                   set status = 'revoked',
                       current_approver_id = null,
                       finished_at = ?,
                       revoke_reason = ?,
                       updated_at = ?
                 where id = ? and is_deleted = 0
                """,
            Timestamp.valueOf(now),
            request.getReason(),
            Timestamp.valueOf(now),
            applicationId
        );
        insertApprovalRecord(applicationId, user.getId(), "revoke", request.getReason(), entity.status(), "revoked", now);
        return new ApplicationDto.ActionResponse(entity.applicationNo(), "revoked", "-");
    }

    public PagedData<ApplicationDto.ApplicationView> pendingApprovals(CurrentUser user,
                                                                      int pageNo,
                                                                      int pageSize,
                                                                      String applicationType,
                                                                      String status,
                                                                      Long templateId) {
        ensureManager(user);
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        String typeFilter = normalizeFilter(applicationType);
        String statusFilter = normalizeFilter(status);
        long total = countPendingApprovals(typeFilter, statusFilter, templateId);

        List<ApplicationDto.ApplicationView> records = jdbcTemplate.query("""
                select a.id, a.application_no, a.application_type, a.title, a.purpose, a.status,
                       coalesce(u.real_name, '-') as current_approver, a.submitted_at
                  from biz_application a
                  left join sys_user u on u.id = a.current_approver_id
                 where a.is_deleted = 0
                   and (? is null or lower(a.application_type) = ?)
                   and (? is null or a.template_id = ?)
                   and ((? is not null and lower(a.status) = ?)
                        or (? is null and a.status in ('submitted', 'reviewing')))
                 order by a.submitted_at desc, a.id desc
                 limit ? offset ?
                """,
            this::mapApplicationView,
            typeFilter, typeFilter,
            templateId, templateId,
            statusFilter, statusFilter,
            statusFilter,
            safePageSize,
            (safePageNo - 1) * safePageSize
        );

        return new PagedData<>(records, safePageNo, safePageSize, total);
    }

    @Transactional
    public ApplicationDto.ActionResponse approve(CurrentUser user,
                                                 Long applicationId,
                                                 ApplicationDto.ApprovalActionRequest request) {
        return review(user, applicationId, "approved", "approve", request.getComment());
    }

    @Transactional
    public ApplicationDto.ActionResponse reject(CurrentUser user,
                                                Long applicationId,
                                                ApplicationDto.ApprovalActionRequest request) {
        return review(user, applicationId, "rejected", "reject", request.getComment());
    }

    private ApplicationDto.ActionResponse review(CurrentUser user,
                                                 Long applicationId,
                                                 String targetStatus,
                                                 String action,
                                                 String comment) {
        ensureManager(user);
        ApplicationEntity entity = findById(applicationId);
        if (!isReviewingStatus(entity.status())) {
            throw new BusinessException(ErrorCode.STATUS_CONFLICT, "application is not in reviewable status");
        }

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("""
                update biz_application
                   set status = ?,
                       current_approver_id = null,
                       finished_at = ?,
                       updated_at = ?
                 where id = ? and is_deleted = 0
                """,
            targetStatus,
            Timestamp.valueOf(now),
            Timestamp.valueOf(now),
            applicationId
        );
        insertApprovalRecord(applicationId, user.getId(), action, comment, entity.status(), targetStatus, now);
        return new ApplicationDto.ActionResponse(entity.applicationNo(), targetStatus, "-");
    }

    private long countMyApplications(CurrentUser user, String typeFilter, String statusFilter) {
        Long count = jdbcTemplate.queryForObject("""
                select count(*)
                  from biz_application
                 where is_deleted = 0
                   and applicant_user_id = ?
                   and (? is null or lower(application_type) = ?)
                   and (? is null or lower(status) = ?)
                """,
            Long.class,
            user.getId(),
            typeFilter, typeFilter,
            statusFilter, statusFilter
        );
        return count == null ? 0 : count;
    }

    private long countPendingApprovals(String typeFilter, String statusFilter, Long templateId) {
        Long count = jdbcTemplate.queryForObject("""
                select count(*)
                  from biz_application
                 where is_deleted = 0
                   and (? is null or lower(application_type) = ?)
                   and (? is null or template_id = ?)
                   and ((? is not null and lower(status) = ?)
                        or (? is null and status in ('submitted', 'reviewing')))
                """,
            Long.class,
            typeFilter, typeFilter,
            templateId, templateId,
            statusFilter, statusFilter,
            statusFilter
        );
        return count == null ? 0 : count;
    }

    private ApplicationEntity findById(Long applicationId) {
        List<ApplicationEntity> rows = jdbcTemplate.query("""
                select a.id, a.application_no, a.application_type, a.template_id, a.applicant_user_id,
                       a.student_id, a.title, a.purpose, a.form_data_json, a.status,
                       coalesce(u.real_name, '-') as current_approver, a.submitted_at
                  from biz_application a
                  left join sys_user u on u.id = a.current_approver_id
                 where a.id = ? and a.is_deleted = 0
                """,
            this::mapApplicationEntity,
            applicationId
        );
        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "application not found");
        }
        return rows.get(0);
    }

    private List<ApplicationDto.ApprovalRecord> approvalRecords(Long applicationId) {
        return jdbcTemplate.query("""
                select r.action_type, r.action_comment, r.action_time, u.real_name
                  from biz_approval_record r
                  left join sys_user u on u.id = r.approver_user_id
                 where r.application_id = ?
                 order by r.action_time asc, r.id asc
                """,
            (rs, rowNum) -> new ApplicationDto.ApprovalRecord(
                nodeNameForAction(rs.getString("action_type")),
                rs.getString("real_name"),
                rs.getString("action_type"),
                rs.getString("action_comment"),
                format(rs.getTimestamp("action_time"))
            ),
            applicationId
        );
    }

    private void insertApprovalRecord(Long applicationId,
                                      Long operatorUserId,
                                      String action,
                                      String comment,
                                      String beforeStatus,
                                      String afterStatus,
                                      LocalDateTime actionTime) {
        jdbcTemplate.update("""
                insert into biz_approval_record (
                    id, application_id, approver_user_id, action_type, action_comment,
                    before_status, after_status, action_time
                ) values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
            nextId("biz_approval_record"),
            applicationId,
            operatorUserId,
            action,
            comment,
            beforeStatus,
            afterStatus,
            Timestamp.valueOf(actionTime)
        );
    }

    // Local H2 bootstrap keeps IDs explicit so seeded demo data can use stable primary keys.
    // Replace this with database sequences/identity retrieval before concurrent production use.
    private Long nextId(String tableName) {
        Long value = jdbcTemplate.queryForObject("select coalesce(max(id), 0) + 1 from " + tableName, Long.class);
        return value == null ? 1L : value;
    }

    private ApplicationDto.ApplicationView mapApplicationView(ResultSet rs, int rowNum) throws SQLException {
        return new ApplicationDto.ApplicationView(
            rs.getLong("id"),
            rs.getString("application_no"),
            rs.getString("application_type"),
            rs.getString("title"),
            rs.getString("purpose"),
            rs.getString("status"),
            rs.getString("current_approver"),
            format(rs.getTimestamp("submitted_at"))
        );
    }

    private ApplicationEntity mapApplicationEntity(ResultSet rs, int rowNum) throws SQLException {
        return new ApplicationEntity(
            rs.getLong("id"),
            rs.getString("application_no"),
            rs.getString("application_type"),
            rs.getLong("template_id"),
            rs.getLong("applicant_user_id"),
            rs.getLong("student_id"),
            rs.getString("title"),
            rs.getString("purpose"),
            rs.getString("form_data_json"),
            rs.getString("status"),
            rs.getString("current_approver"),
            rs.getTimestamp("submitted_at")
        );
    }

    private String buildApplicationNo(Long applicationId) {
        return "APP" + NO_FORMATTER.format(LocalDateTime.now()) + String.format("%04d", applicationId % 10000);
    }

    private String normalizeFilter(String value) {
        if (!StringUtils.hasText(value) || "all".equalsIgnoreCase(value)) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data == null ? Map.of() : data);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "formData is not serializable");
        }
    }

    private Map<String, Object> fromJson(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "stored formData is invalid");
        }
    }

    private String format(Timestamp timestamp) {
        return timestamp == null ? "-" : DATETIME_FORMATTER.format(timestamp.toLocalDateTime());
    }

    private boolean isManager(CurrentUser user) {
        return user.getRoles().stream().anyMatch(role ->
            "teacher_admin".equals(role) || "college_leader".equals(role) || "system_admin".equals(role));
    }

    private void ensureManager(CurrentUser user) {
        if (!isManager(user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "manager role required");
        }
    }

    private boolean isReviewingStatus(String status) {
        return "submitted".equals(status) || "reviewing".equals(status);
    }

    private boolean isRevocable(String status) {
        return isReviewingStatus(status);
    }

    private String nodeNameForAction(String action) {
        return switch (action) {
            case "submit" -> "提交申请";
            case "pending" -> "辅导员审核";
            case "approve" -> "审批通过";
            case "reject" -> "审批驳回";
            case "revoke" -> "申请撤回";
            default -> "审批记录";
        };
    }

    private record ApplicationEntity(Long id,
                                     String applicationNo,
                                     String applicationType,
                                     Long templateId,
                                     Long applicantUserId,
                                     Long studentId,
                                     String title,
                                     String purpose,
                                     String formDataJson,
                                     String status,
                                     String currentApprover,
                                     Timestamp submittedAt) {
    }
}

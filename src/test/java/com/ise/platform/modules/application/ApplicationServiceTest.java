package com.ise.platform.modules.application;

import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.common.security.DataScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ApplicationServiceTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createShouldReturnSubmittedStatusAndVisibleInMyList() {
        CurrentUser user = studentUser();
        ApplicationDto.CreateRequest request = createRequest();

        ApplicationDto.CreateResponse response = applicationService.create(user, request);
        assertThat(response.getStatus()).isEqualTo("submitted");
        assertThat(response.getApplicationNo()).startsWith("APP");

        PagedData<ApplicationDto.ApplicationView> page = applicationService.myApplications(user, 1, 20, "certificate", null);
        assertThat(page.getRecords()).anyMatch(item -> item.getApplicationNo().equals(response.getApplicationNo()));
    }

    @Test
    void applicantCanRevokeReviewingApplication() {
        CurrentUser user = studentUser();
        ApplicationDto.RevokeRequest request = new ApplicationDto.RevokeRequest();
        request.setReason("用途变更");

        ApplicationDto.ActionResponse response = applicationService.revoke(user, 1L, request);
        assertThat(response.getStatus()).isEqualTo("revoked");

        ApplicationDto.ApplicationDetailView detail = applicationService.detail(user, 1L);
        assertThat(detail.getStatus()).isEqualTo("revoked");
        assertThat(detail.getApprovalRecords()).anyMatch(record -> "revoke".equals(record.getAction()));
    }

    @Test
    void managerCanApproveSubmittedApplication() {
        CurrentUser student = studentUser();
        ApplicationDto.CreateResponse created = applicationService.create(student, createRequest());
        Long applicationId = applicationService.myApplications(student, 1, 20, null, null)
            .getRecords().stream()
            .filter(item -> item.getApplicationNo().equals(created.getApplicationNo()))
            .findFirst()
            .map(ApplicationDto.ApplicationView::getId)
            .orElseThrow();

        CurrentUser manager = managerUser();
        PagedData<ApplicationDto.ApplicationView> pending = applicationService.pendingApprovals(manager, 1, 20, null, null, null);
        assertThat(pending.getRecords()).anyMatch(item -> item.getId().equals(applicationId));

        ApplicationDto.ApprovalActionRequest approve = new ApplicationDto.ApprovalActionRequest();
        approve.setComment("材料齐全，同意通过");
        ApplicationDto.ActionResponse action = applicationService.approve(manager, applicationId, approve);

        assertThat(action.getStatus()).isEqualTo("approved");
        ApplicationDto.ApplicationDetailView detail = applicationService.detail(manager, applicationId);
        assertThat(detail.getStatus()).isEqualTo("approved");
        assertThat(detail.getApprovalRecords()).anyMatch(record -> "approve".equals(record.getAction()));
    }

    @Test
    void createdApplicationShouldSurviveServiceReadBackFromDatabase() {
        CurrentUser student = studentUser();
        ApplicationDto.CreateResponse created = applicationService.create(student, createRequest());

        ApplicationDto.ApplicationView view = applicationService.myApplications(student, 1, 20, null, null)
            .getRecords().stream()
            .filter(item -> item.getApplicationNo().equals(created.getApplicationNo()))
            .findFirst()
            .orElseThrow();

        ApplicationDto.ApplicationDetailView detail = applicationService.detail(student, view.getId());
        assertThat(detail.getApplicationNo()).isEqualTo(created.getApplicationNo());
        assertThat(detail.getFormData()).containsEntry("receiveOrg", "某科技公司");
        assertThat(detail.getApprovalRecords()).anyMatch(record -> "submit".equals(record.getAction()));

        Integer dbCount = jdbcTemplate.queryForObject(
            "select count(*) from biz_application where application_no = ?",
            Integer.class,
            created.getApplicationNo()
        );
        assertThat(dbCount).isEqualTo(1);
    }

    @Test
    void studentCannotApproveApplication() {
        CurrentUser student = studentUser();
        ApplicationDto.ApprovalActionRequest approve = new ApplicationDto.ApprovalActionRequest();
        approve.setComment("越权审批");

        assertThatThrownBy(() -> applicationService.approve(student, 1L, approve))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void approvalShouldRollbackStatusWhenApprovalRecordInsertFails() {
        CurrentUser student = studentUser();
        ApplicationDto.CreateResponse created = applicationService.create(student, createRequest());
        Long applicationId = applicationService.myApplications(student, 1, 20, null, null)
            .getRecords().stream()
            .filter(item -> item.getApplicationNo().equals(created.getApplicationNo()))
            .findFirst()
            .map(ApplicationDto.ApplicationView::getId)
            .orElseThrow();

        ApplicationDto.ApprovalActionRequest approve = new ApplicationDto.ApprovalActionRequest();
        approve.setComment("x".repeat(600));

        assertThatThrownBy(() -> applicationService.approve(managerUser(), applicationId, approve))
            .isInstanceOf(DataIntegrityViolationException.class);

        ApplicationDto.ApplicationDetailView detail = applicationService.detail(student, applicationId);
        assertThat(detail.getStatus()).isEqualTo("submitted");
        assertThat(detail.getApprovalRecords()).noneMatch(record -> "approve".equals(record.getAction()));
    }

    private ApplicationDto.CreateRequest createRequest() {
        ApplicationDto.CreateRequest request = new ApplicationDto.CreateRequest();
        request.setApplicationType("certificate");
        request.setTemplateId(1L);
        request.setTitle("在读证明申请");
        request.setPurpose("实习材料");
        request.setFormData(Map.of("receiveOrg", "某科技公司"));
        return request;
    }

    private CurrentUser studentUser() {
        return new CurrentUser(
            1L,
            "20220001",
            "赵晨曦",
            "student",
            List.of("student"),
            List.of("application:create", "application:self:view"),
            List.of(new DataScope("self", "1")),
            1L
        );
    }

    private CurrentUser managerUser() {
        return new CurrentUser(
            8L,
            "teacher001",
            "李老师",
            "teacher",
            List.of("teacher_admin"),
            List.of("application:approve", "application:reject"),
            List.of(new DataScope("department", "信息科学与工程学院")),
            null
        );
    }
}

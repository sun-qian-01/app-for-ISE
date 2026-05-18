package com.ise.platform.modules.student;

import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.common.security.DataScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudentServiceTest {

    private final StudentService studentService = new StudentService();

    @Test
    void studentCannotReadOtherGrowthRecords() {
        CurrentUser user = studentUser(1L, "20220001");
        assertThatThrownBy(() -> studentService.growthRecords(user, 2L, null))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void managerCanReadOtherGrowthRecords() {
        CurrentUser user = teacherUser();
        List<StudentDto.GrowthRecordView> records = studentService.growthRecords(user, 1L, "competition");
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getRecordType()).isEqualTo("competition");
    }

    @Test
    void meProfileShouldContainCoreFields() {
        CurrentUser user = studentUser(1L, "20220001");
        StudentDto.MeProfileView profile = studentService.meProfile(user);
        assertThat(profile.getStudent().getStudentNo()).isEqualTo("20220001");
        assertThat(profile.getTags()).isNotEmpty();
        assertThat(profile.getGrowthSummary().getCompetitionCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void listStudentsShouldSupportFiltersForManager() {
        CurrentUser user = teacherUser();
        var page = studentService.listStudents(user, 1, 10, null, null, "2022", "软件工程",
            null, null, null, null, null);
        assertThat(page.getTotal()).isGreaterThanOrEqualTo(2);
        assertThat(page.getRecords()).allMatch(item -> "2022".equals(item.getGrade()));
    }

    @Test
    void includeSensitiveRequiresManagerRole() {
        CurrentUser user = studentUser(1L, "20220001");
        assertThatThrownBy(() -> studentService.studentDetail(user, 1L, true))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void managerCanViewSensitiveDetail() {
        CurrentUser user = teacherUser();
        var detail = studentService.studentDetail(user, 1L, true);
        assertThat(detail.getSensitiveInfo()).isNotNull();
        assertThat(detail.getSensitiveInfo().getPhone()).isEqualTo("13800181234");
    }

    @Test
    void studentCanAddOwnGrowthRecord() {
        CurrentUser user = studentUser(1L, "20220001");
        StudentDto.CreateGrowthRecordRequest request = new StudentDto.CreateGrowthRecordRequest();
        request.setRecordType("practice");
        request.setTitle("企业实习");
        request.setStartDate("2026-06-01");
        request.setEndDate("2026-07-01");
        request.setDescription("参与后端开发");

        StudentDto.GrowthRecordView view = studentService.createGrowthRecord(user, 1L, request);
        assertThat(view.getId()).isNotNull();

        List<StudentDto.GrowthRecordView> records = studentService.growthRecords(user, 1L, "practice");
        assertThat(records).anyMatch(item -> item.getId().equals(view.getId()));
    }

    @Test
    void unknownRecordTypeShouldBeRejected() {
        CurrentUser user = studentUser(1L, "20220001");
        StudentDto.CreateGrowthRecordRequest request = new StudentDto.CreateGrowthRecordRequest();
        request.setRecordType("unknown");
        request.setTitle("非法记录");
        request.setStartDate("2026-06-01");

        assertThatThrownBy(() -> studentService.createGrowthRecord(user, 1L, request))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.PARAM_INVALID);
    }

    @Test
    void managerCanUpdateTags() {
        CurrentUser user = teacherUser();
        StudentDto.UpdateTagsRequest request = new StudentDto.UpdateTagsRequest();
        request.setTagIds(List.of(1L, 4L));

        List<StudentDto.TagView> tags = studentService.updateTags(user, 1L, request);
        assertThat(tags).hasSize(2);
        assertThat(tags).anyMatch(tag -> "学业帮扶".equals(tag.getTagName()));
    }

    @Test
    void importTaskRequiresManagerRole() {
        CurrentUser student = studentUser(1L, "20220001");
        StudentDto.ImportTaskCreateRequest request = new StudentDto.ImportTaskCreateRequest();
        request.setImportType("student_base");
        request.setFileId(10L);

        assertThatThrownBy(() -> studentService.createImportTask(student, request))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void managerCanCreateAndQueryImportTask() {
        CurrentUser teacher = teacherUser();
        StudentDto.ImportTaskCreateRequest request = new StudentDto.ImportTaskCreateRequest();
        request.setImportType("student_base");
        request.setFileId(10L);

        StudentDto.ImportTaskCreateView created = studentService.createImportTask(teacher, request);
        assertThat(created.getTaskNo()).startsWith("IMP");

        StudentDto.ImportTaskView detail = studentService.importTaskDetail(teacher, created.getTaskNo());
        assertThat(detail.getStatus()).isEqualTo("success");
        assertThat(detail.getImportType()).isEqualTo("student_base");
    }

    private CurrentUser studentUser(Long studentId, String username) {
        return new CurrentUser(
            1L,
            username,
            "测试学生",
            "student",
            List.of("student"),
            List.of("student:profile:view"),
            List.of(new DataScope("self", String.valueOf(studentId))),
            studentId
        );
    }

    private CurrentUser teacherUser() {
        return new CurrentUser(
            8L,
            "teacher001",
            "李老师",
            "teacher",
            List.of("teacher_admin"),
            List.of("student:detail:view"),
            List.of(new DataScope("department", "信息科学与工程学院")),
            null
        );
    }
}

package com.ise.platform.modules.student;

import com.ise.platform.common.api.ApiResponse;
import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.security.AuthContext;
import com.ise.platform.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/me/profile")
    public ApiResponse<StudentDto.MeProfileView> meProfile() {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(studentService.meProfile(user));
    }

    @GetMapping("/{studentId}/growth-records")
    public ApiResponse<List<StudentDto.GrowthRecordView>> growthRecords(@PathVariable Long studentId,
                                                                        @RequestParam(required = false) String recordType) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(studentService.growthRecords(user, studentId, recordType));
    }

    @GetMapping
    public ApiResponse<PagedData<StudentDto.StudentListItemView>> students(
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String studentNo,
        @RequestParam(required = false) String grade,
        @RequestParam(required = false) String major,
        @RequestParam(required = false) String className,
        @RequestParam(required = false) String politicalStatus,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long tagId,
        @RequestParam(required = false) Boolean isGraduating,
        @RequestParam(required = false) String keyword
    ) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(studentService.listStudents(
            user, pageNo, pageSize, name, studentNo, grade, major, className, politicalStatus, status, tagId, isGraduating, keyword
        ));
    }

    @GetMapping("/{studentId}")
    public ApiResponse<StudentDto.StudentDetailView> studentDetail(@PathVariable Long studentId,
                                                                   @RequestParam(defaultValue = "false") boolean includeSensitive) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(studentService.studentDetail(user, studentId, includeSensitive));
    }

    @PutMapping("/{studentId}")
    public ApiResponse<StudentDto.StudentDetailView> updateStudent(@PathVariable Long studentId,
                                                                   @Valid @RequestBody StudentDto.UpdateStudentRequest request) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(studentService.updateStudent(user, studentId, request));
    }

    @PostMapping("/{studentId}/growth-records")
    public ApiResponse<StudentDto.GrowthRecordView> createGrowthRecord(@PathVariable Long studentId,
                                                                       @Valid @RequestBody StudentDto.CreateGrowthRecordRequest request) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(studentService.createGrowthRecord(user, studentId, request));
    }

    @PutMapping("/{studentId}/tags")
    public ApiResponse<List<StudentDto.TagView>> updateTags(@PathVariable Long studentId,
                                                            @Valid @RequestBody StudentDto.UpdateTagsRequest request) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(studentService.updateTags(user, studentId, request));
    }

    @PostMapping("/batch-register")
    public ApiResponse<StudentDto.BatchRegisterStudentResponse> batchRegister(@RequestBody StudentDto.BatchRegisterStudentRequest request) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(studentService.batchRegisterStudents(user, request));
    }

    @PostMapping("/import-tasks")
    public ApiResponse<StudentDto.ImportTaskCreateView> createImportTask(@Valid @RequestBody StudentDto.ImportTaskCreateRequest request) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(studentService.createImportTask(user, request));
    }

    @GetMapping("/import-tasks/{taskNo}")
    public ApiResponse<StudentDto.ImportTaskView> importTaskDetail(@PathVariable String taskNo) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(studentService.importTaskDetail(user, taskNo));
    }
}

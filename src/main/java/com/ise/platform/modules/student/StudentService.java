package com.ise.platform.modules.student;

import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TASK_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Set<String> ALLOWED_RECORD_TYPES = Set.of(
        "competition", "practice", "volunteer", "cadre", "reward_punishment"
    );

    private static final Map<Long, String> TAG_CATALOG = Map.of(
        1L, "就业意向",
        2L, "奖学金关注",
        3L, "班团骨干",
        4L, "学业帮扶",
        5L, "科研潜力"
    );

    private static final Map<String, String> STATUS_LABEL = Map.of(
        "active", "在读",
        "graduating", "毕业年级",
        "warning", "重点关注",
        "graduated", "已毕业"
    );

    private final AtomicLong growthRecordIdGenerator = new AtomicLong(1000);
    private final AtomicLong importTaskSequence = new AtomicLong(1);

    private final Map<Long, StudentEntity> studentById = new ConcurrentHashMap<>();
    private final Map<Long, List<StudentDto.TagView>> tagsByStudentId = new ConcurrentHashMap<>();
    private final Map<Long, List<GrowthRecordEntity>> growthRecordsByStudentId = new ConcurrentHashMap<>();
    private final Map<String, ImportTaskEntity> importTaskByTaskNo = new ConcurrentHashMap<>();

    public StudentService() {
        studentById.put(1L, new StudentEntity(1L, "20220001", "赵晨曦", "2022", "软件工程", "软件工程2班", "预备党员", "active", "138****1234", "13800181234", "zhaochenxi@example.edu.cn"));
        studentById.put(2L, new StudentEntity(2L, "20220018", "陈一诺", "2022", "软件工程", "软件工程2班", "发展对象", "active", "139****8818", "13900188818", "chenyinuo@example.edu.cn"));
        studentById.put(3L, new StudentEntity(3L, "20260031", "林嘉禾", "2026", "数据科学", "数据科学1班", "共青团员", "graduating", "137****0631", "13700000631", "linjiahe@example.edu.cn"));
        studentById.put(4L, new StudentEntity(4L, "20230007", "周明远", "2023", "软件工程", "软件工程1班", "共青团员", "warning", "136****3007", "13600003007", "zhoumingyuan@example.edu.cn"));

        tagsByStudentId.put(1L, new CopyOnWriteArrayList<>(List.of(
            new StudentDto.TagView(1L, "就业意向"),
            new StudentDto.TagView(2L, "奖学金关注")
        )));
        tagsByStudentId.put(2L, new CopyOnWriteArrayList<>(List.of(
            new StudentDto.TagView(2L, "奖学金关注"),
            new StudentDto.TagView(3L, "班团骨干")
        )));
        tagsByStudentId.put(3L, new CopyOnWriteArrayList<>(List.of(
            new StudentDto.TagView(1L, "就业意向")
        )));

        growthRecordsByStudentId.put(1L, new CopyOnWriteArrayList<>(List.of(
            new GrowthRecordEntity(11L, "competition", "大学生创新训练项目", "2025-09-01", "2025-11-30", "院级立项，负责需求分析。", 101L),
            new GrowthRecordEntity(12L, "volunteer", "学院迎新志愿服务", "2026-04-01", "2026-04-02", "累计服务 8 小时。", 102L),
            new GrowthRecordEntity(13L, "cadre", "软件工程2班学习委员", "2024-09-01", null, "协助课程通知和学业帮扶。", null)
        )));
        growthRecordsByStudentId.put(2L, new CopyOnWriteArrayList<>(List.of(
            new GrowthRecordEntity(21L, "cadre", "团支部理论学习组织", "2026-03-01", "2026-03-30", "组织 4 次主题活动。", null),
            new GrowthRecordEntity(22L, "volunteer", "社区志愿服务", "2026-03-10", "2026-03-28", "累计服务 18 小时。", 103L)
        )));
    }

    public StudentDto.MeProfileView meProfile(CurrentUser user) {
        Long studentId = user.getStudentId();
        if (studentId == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "student account required");
        }
        StudentEntity student = studentById.get(studentId);
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "student not found");
        }
        List<GrowthRecordEntity> records = growthRecordsByStudentId.getOrDefault(studentId, List.of());
        int competitionCount = (int) records.stream().filter(record -> "competition".equals(record.recordType())).count();
        int volunteerHours = (int) records.stream().filter(record -> "volunteer".equals(record.recordType())).count() * 8;
        int honorCount = 1;
        return new StudentDto.MeProfileView(
            student.toView(),
            tagsByStudentId.getOrDefault(studentId, List.of()),
            new StudentDto.GrowthSummary(competitionCount, volunteerHours, honorCount),
            3,
            2,
            "预备党员"
        );
    }

    public List<StudentDto.GrowthRecordView> growthRecords(CurrentUser user, Long studentId, String recordType) {
        if (!isManager(user) && !studentId.equals(user.getStudentId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "cannot access other student's growth records");
        }
        ensureStudentExists(studentId);
        return growthRecordsByStudentId.getOrDefault(studentId, List.of()).stream()
            .filter(record -> !StringUtils.hasText(recordType) || record.recordType().equals(recordType))
            .map(GrowthRecordEntity::toView)
            .toList();
    }

    public PagedData<StudentDto.StudentListItemView> listStudents(CurrentUser user,
                                                                  int pageNo,
                                                                  int pageSize,
                                                                  String name,
                                                                  String studentNo,
                                                                  String grade,
                                                                  String major,
                                                                  String className,
                                                                  String politicalStatus,
                                                                  String status,
                                                                  Long tagId,
                                                                  Boolean isGraduating) {
        if (!isManager(user) && !isCadre(user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "only cadre or manager can query student list");
        }
        List<StudentDto.StudentListItemView> filtered = studentById.values().stream()
            .filter(item -> !StringUtils.hasText(name) || containsIgnoreCase(item.name(), name))
            .filter(item -> !StringUtils.hasText(studentNo) || containsIgnoreCase(item.studentNo(), studentNo))
            .filter(item -> !StringUtils.hasText(grade) || Objects.equals(item.grade(), grade))
            .filter(item -> !StringUtils.hasText(major) || Objects.equals(item.major(), major))
            .filter(item -> !StringUtils.hasText(className) || Objects.equals(item.className(), className))
            .filter(item -> !StringUtils.hasText(politicalStatus) || Objects.equals(item.politicalStatus(), politicalStatus))
            .filter(item -> !StringUtils.hasText(status) || Objects.equals(statusLabel(item.status()), status))
            .filter(item -> isGraduating == null || item.isGraduating() == isGraduating)
            .map(item -> item.toListItem(tagsByStudentId.getOrDefault(item.id(), List.of()).stream()
                .map(StudentDto.TagView::getTagName)
                .collect(Collectors.toList())))
            .filter(item -> tagId == null || tagsByStudentId.getOrDefault(item.getId(), List.of()).stream().anyMatch(tag -> tag.getId().equals(tagId)))
            .toList();

        return paginate(filtered, pageNo, pageSize);
    }

    public StudentDto.StudentDetailView studentDetail(CurrentUser user, Long studentId, boolean includeSensitive) {
        if (!isManager(user) && !isCadre(user) && !studentId.equals(user.getStudentId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "no permission to view this student");
        }
        StudentEntity entity = studentById.get(studentId);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "student not found");
        }

        if (includeSensitive && !canViewSensitive(user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "only teacher admin can view sensitive fields");
        }

        StudentDto.SensitiveInfo sensitiveInfo = includeSensitive
            ? new StudentDto.SensitiveInfo(entity.phone(), entity.email())
            : null;

        return new StudentDto.StudentDetailView(
            entity.toListItem(tagsByStudentId.getOrDefault(entity.id(), List.of()).stream()
                .map(StudentDto.TagView::getTagName)
                .collect(Collectors.toList())),
            maskEmail(entity.email()),
            sensitiveInfo
        );
    }

    public StudentDto.StudentDetailView updateStudent(CurrentUser user,
                                                      Long studentId,
                                                      StudentDto.UpdateStudentRequest request) {
        if (!isManager(user) && !isCadre(user) && !studentId.equals(user.getStudentId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "no permission to update this student");
        }
        StudentEntity current = studentById.get(studentId);
        if (current == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "student not found");
        }

        String phone = firstNonBlank(request.getPhone(), current.phone());
        String email = firstNonBlank(request.getEmail(), current.email());
        String politicalStatus = firstNonBlank(request.getPoliticalStatus(), current.politicalStatus());
        String status = firstNonBlank(request.getStatus(), current.status());

        StudentEntity updated = new StudentEntity(
            current.id(),
            current.studentNo(),
            current.name(),
            current.grade(),
            current.major(),
            current.className(),
            politicalStatus,
            status,
            maskPhone(phone),
            phone,
            email
        );
        studentById.put(studentId, updated);
        return studentDetail(user, studentId, false);
    }

    public StudentDto.GrowthRecordView createGrowthRecord(CurrentUser user,
                                                           Long studentId,
                                                           StudentDto.CreateGrowthRecordRequest request) {
        if (!isManager(user) && !isCadre(user) && !studentId.equals(user.getStudentId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "no permission to add growth record");
        }
        if (!ALLOWED_RECORD_TYPES.contains(request.getRecordType())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "unsupported recordType");
        }
        ensureStudentExists(studentId);

        GrowthRecordEntity entity = new GrowthRecordEntity(
            growthRecordIdGenerator.incrementAndGet(),
            request.getRecordType(),
            request.getTitle(),
            request.getStartDate(),
            request.getEndDate(),
            request.getDescription(),
            request.getProofFileId()
        );
        growthRecordsByStudentId
            .computeIfAbsent(studentId, ignored -> new CopyOnWriteArrayList<>())
            .add(entity);
        return entity.toView();
    }

    public List<StudentDto.TagView> updateTags(CurrentUser user,
                                               Long studentId,
                                               StudentDto.UpdateTagsRequest request) {
        if (!isManager(user) && !isCadre(user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "only cadre or manager can update tags");
        }
        ensureStudentExists(studentId);

        List<StudentDto.TagView> updatedTags = request.getTagIds().stream()
            .distinct()
            .map(tagId -> {
                String tagName = TAG_CATALOG.get(tagId);
                if (tagName == null) {
                    throw new BusinessException(ErrorCode.PARAM_INVALID, "unknown tagId: " + tagId);
                }
                return new StudentDto.TagView(tagId, tagName);
            })
            .toList();

        tagsByStudentId.put(studentId, new CopyOnWriteArrayList<>(updatedTags));
        return updatedTags;
    }

    public StudentDto.ImportTaskCreateView createImportTask(CurrentUser user,
                                                            StudentDto.ImportTaskCreateRequest request) {
        ensureManager(user);
        String now = DATETIME_FORMATTER.format(LocalDateTime.now());
        String taskNo = "IMP" + TASK_NO_FORMATTER.format(LocalDateTime.now()) + String.format("%03d", importTaskSequence.getAndIncrement() % 1000);

        ImportTaskEntity task = new ImportTaskEntity(
            taskNo,
            request.getImportType(),
            request.getFileId(),
            "success",
            now,
            now,
            "导入任务已完成（演示模式）"
        );
        importTaskByTaskNo.put(taskNo, task);
        return new StudentDto.ImportTaskCreateView(taskNo, task.status(), now);
    }

    public StudentDto.ImportTaskView importTaskDetail(CurrentUser user, String taskNo) {
        ensureManager(user);
        ImportTaskEntity task = importTaskByTaskNo.get(taskNo);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "import task not found");
        }
        return task.toView();
    }

    private void ensureStudentExists(Long studentId) {
        if (!studentById.containsKey(studentId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "student not found");
        }
    }

    private void ensureManager(CurrentUser user) {
        if (!isManager(user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "manager role required");
        }
    }

    private boolean isManager(CurrentUser user) {
        return user.getRoles().stream().anyMatch(role ->
            "teacher_admin".equals(role) || "college_leader".equals(role) || "system_admin".equals(role));
    }

    private boolean canViewSensitive(CurrentUser user) {
        return user.getRoles().stream().anyMatch("teacher_admin"::equals);
    }

    private boolean isCadre(CurrentUser user) {
        return user.getRoles().stream().anyMatch("class_cadre"::equals);
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private String firstNonBlank(String incoming, String current) {
        return StringUtils.hasText(incoming) ? incoming : current;
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@", 2);
        if (parts[0].length() <= 2) {
            return parts[0].charAt(0) + "***@" + parts[1];
        }
        return parts[0].substring(0, 2) + "***@" + parts[1];
    }

    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String statusLabel(String status) {
        return STATUS_LABEL.getOrDefault(status, status);
    }

    private PagedData<StudentDto.StudentListItemView> paginate(List<StudentDto.StudentListItemView> source,
                                                               int pageNo,
                                                               int pageSize) {
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        int from = (safePageNo - 1) * safePageSize;
        if (from >= source.size()) {
            return new PagedData<>(List.of(), safePageNo, safePageSize, source.size());
        }
        int to = Math.min(from + safePageSize, source.size());
        return new PagedData<>(new ArrayList<>(source.subList(from, to)), safePageNo, safePageSize, source.size());
    }

    private record StudentEntity(Long id,
                                 String studentNo,
                                 String name,
                                 String grade,
                                 String major,
                                 String className,
                                 String politicalStatus,
                                 String status,
                                 String phoneMasked,
                                 String phone,
                                 String email) {

        StudentDto.StudentView toView() {
            return new StudentDto.StudentView(id, studentNo, name, grade, major, className, politicalStatus, phoneMasked);
        }

        StudentDto.StudentListItemView toListItem(List<String> tags) {
            return new StudentDto.StudentListItemView(
                id,
                studentNo,
                name,
                grade,
                major,
                className,
                politicalStatus,
                STATUS_LABEL.getOrDefault(status, status),
                phoneMasked,
                tags
            );
        }

        boolean isGraduating() {
            return "graduating".equals(status);
        }
    }

    private record GrowthRecordEntity(Long id,
                                      String recordType,
                                      String title,
                                      String startDate,
                                      String endDate,
                                      String description,
                                      Long proofFileId) {
        StudentDto.GrowthRecordView toView() {
            return new StudentDto.GrowthRecordView(id, recordType, title, startDate, endDate, description, proofFileId);
        }
    }

    private record ImportTaskEntity(String taskNo,
                                    String importType,
                                    Long fileId,
                                    String status,
                                    String createdAt,
                                    String updatedAt,
                                    String message) {
        StudentDto.ImportTaskView toView() {
            return new StudentDto.ImportTaskView(taskNo, importType, fileId, status, createdAt, updatedAt, message);
        }
    }
}

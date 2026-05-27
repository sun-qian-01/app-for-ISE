package com.ise.platform.modules.party;

import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PartyService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AtomicLong materialIdGenerator = new AtomicLong(5000);

    private final Map<Long, PartyInstanceState> instanceByStudentId = new HashMap<>();
    private final List<PartyDto.FlowDefinitionView> flowDefinitions;

    public PartyService() {
        this.flowDefinitions = List.of(
            new PartyDto.FlowDefinitionView(
                1L,
                "party_join",
                "入党流程",
                List.of(
                    new PartyDto.StageDefinitionView("applicant", "入党申请人", 1),
                    new PartyDto.StageDefinitionView("activist", "积极分子", 2),
                    new PartyDto.StageDefinitionView("development_candidate", "发展对象", 3),
                    new PartyDto.StageDefinitionView("probationary_party_member", "预备党员", 4),
                    new PartyDto.StageDefinitionView("party_member", "正式党员", 5)
                )
            )
        );

        List<StageState> stageStates1 = new ArrayList<>();
        stageStates1.add(new StageState(1000L, "applicant", "入党申请人", 1, "approved", "2025-09-30 23:59:59"));
        stageStates1.add(new StageState(1001L, "activist", "积极分子", 2, "approved", "2025-12-20 23:59:59"));
        stageStates1.add(new StageState(1002L, "development_candidate", "发展对象", 3, "approved", "2026-03-20 23:59:59"));
        stageStates1.add(new StageState(1003L, "probationary_party_member", "预备党员", 4, "reviewing", "2026-04-25 23:59:59"));
        stageStates1.add(new StageState(1004L, "party_member", "正式党员", 5, "pending", "2027-04-25 23:59:59"));
        stageStates1.get(3).materials.add(new MaterialState(4001L, "季度思想汇报", 31L, "pending", "2026-04-18 14:30:00"));
        instanceByStudentId.put(1L, new PartyInstanceState(1L, "20220001", "赵晨曦", "软件工程2班", "入党流程", "processing", "probationary_party_member", 1003L, stageStates1));

        List<StageState> stageStates2 = new ArrayList<>();
        stageStates2.add(new StageState(1100L, "applicant", "入党申请人", 1, "approved", "2025-10-15 23:59:59"));
        stageStates2.add(new StageState(1101L, "activist", "积极分子", 2, "approved", "2026-01-18 23:59:59"));
        stageStates2.add(new StageState(1102L, "development_candidate", "发展对象", 3, "submitted", "2026-05-30 23:59:59"));
        stageStates2.add(new StageState(1103L, "probationary_party_member", "预备党员", 4, "pending", "2026-12-30 23:59:59"));
        stageStates2.add(new StageState(1104L, "party_member", "正式党员", 5, "pending", "2027-12-30 23:59:59"));
        stageStates2.get(2).materials.add(new MaterialState(4101L, "发展对象培训结业证", 32L, "pending", "2026-04-21 09:10:00"));
        stageStates2.get(2).materials.add(new MaterialState(4102L, "季度思想汇报", 33L, "pending", "2026-04-21 09:15:00"));
        instanceByStudentId.put(2L, new PartyInstanceState(2L, "20220018", "陈一诺", "软件工程2班", "入党流程", "processing", "development_candidate", 1102L, stageStates2));
    }

    public List<PartyDto.FlowDefinitionView> flows() {
        return flowDefinitions;
    }

    public PartyDto.PartyInstanceView myInstance(CurrentUser user) {
        Long studentId = user.getStudentId();
        if (studentId == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "student account required");
        }
        PartyInstanceState instance = instanceByStudentId.get(studentId);
        if (instance == null) {
            return new PartyDto.PartyInstanceView("入党流程", "processing", "applicant", List.of());
        }
        return toView(instance);
    }

    public PartyDto.MaterialView submitMaterial(CurrentUser user, Long stageRecordId, PartyDto.MaterialSubmitRequest request) {
        Long studentId = user.getStudentId();
        if (studentId == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "student account required");
        }
        PartyInstanceState instance = instanceByStudentId.get(studentId);
        if (instance == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "party instance not found");
        }
        if (!instance.currentStageRecordId.equals(stageRecordId)) {
            throw new BusinessException(ErrorCode.STATUS_CONFLICT, "only current stage accepts material submission");
        }
        StageState stageState = instance.stages.stream()
            .filter(item -> item.stageRecordId.equals(stageRecordId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "stage record not found"));

        MaterialState material = new MaterialState(
            materialIdGenerator.incrementAndGet(),
            request.getMaterialName(),
            request.getFileId(),
            "pending",
            DATETIME_FORMATTER.format(LocalDateTime.now())
        );
        stageState.materials.add(material);
        stageState.stageStatus = "submitted";
        return new PartyDto.MaterialView(material.materialId, material.materialName, material.fileId, material.reviewStatus, material.submittedAt);
    }

    public List<PartyDto.PartyTodoItem> managerTodos(CurrentUser user, String status, String keyword) {
        ensureManagerOrCadre(user);
        String statusFilter = normalizeFilter(status);
        String keywordFilter = StringUtils.hasText(keyword) ? keyword.trim().toLowerCase(Locale.ROOT) : null;

        List<PartyDto.PartyTodoItem> todos = new ArrayList<>();
        for (PartyInstanceState instance : instanceByStudentId.values()) {
            for (StageState stage : instance.stages) {
                if ("approved".equals(stage.stageStatus) || "pending".equals(stage.stageStatus)) {
                    continue;
                }
                if (statusFilter != null && !stage.stageStatus.equals(statusFilter)) {
                    continue;
                }
                if (isCadre(user) && !instance.studentId.equals(user.getStudentId())) {
                    continue;
                }

                String searchable = (instance.studentNo + " " + instance.studentName + " " + instance.className + " " + stage.stageName)
                    .toLowerCase(Locale.ROOT);
                if (keywordFilter != null && !searchable.contains(keywordFilter)) {
                    continue;
                }

                int pendingCount = (int) stage.materials.stream().filter(item -> "pending".equals(item.reviewStatus)).count();
                todos.add(new PartyDto.PartyTodoItem(
                    stage.stageRecordId,
                    instance.studentName,
                    instance.studentNo,
                    instance.className,
                    stage.stageName,
                    stage.stageStatus,
                    stage.dueAt,
                    stage.materials.size(),
                    pendingCount
                ));
            }
        }

        todos.sort(Comparator.comparing(PartyDto.PartyTodoItem::getDueAt));
        return todos;
    }

    public PartyDto.PartyReviewResult reviewStage(CurrentUser user,
                                                  Long stageRecordId,
                                                  PartyDto.PartyReviewRequest request) {
        ensureManagerOrCadre(user);
        String action = normalizeFilter(request.getAction());
        if (!"approve".equals(action) && !"reject".equals(action)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "action must be approve or reject");
        }

        StageState stage = null;
        PartyInstanceState owner = null;
        for (PartyInstanceState item : instanceByStudentId.values()) {
            StageState matched = item.stages.stream().filter(s -> s.stageRecordId.equals(stageRecordId)).findFirst().orElse(null);
            if (matched != null) {
                stage = matched;
                owner = item;
                break;
            }
        }
        if (stage == null || owner == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "stage record not found");
        }

        if (!"submitted".equals(stage.stageStatus) && !"reviewing".equals(stage.stageStatus)) {
            throw new BusinessException(ErrorCode.STATUS_CONFLICT, "stage is not in reviewable status");
        }

        if ("approve".equals(action)) {
            stage.stageStatus = "approved";
            int nextIndex = stage.stageOrder;
            if (nextIndex < owner.stages.size()) {
                StageState next = owner.stages.get(nextIndex);
                if ("pending".equals(next.stageStatus)) {
                    next.stageStatus = "reviewing";
                    owner.currentStageCode = next.stageCode;
                    owner.currentStageRecordId = next.stageRecordId;
                }
            }
        } else {
            stage.stageStatus = "returned";
            owner.currentStageCode = stage.stageCode;
            owner.currentStageRecordId = stage.stageRecordId;
        }

        return new PartyDto.PartyReviewResult(stage.stageRecordId, stage.stageStatus, request.getComment());
    }

    private PartyDto.PartyInstanceView toView(PartyInstanceState instance) {
        List<PartyDto.StageView> stages = instance.stages.stream()
            .sorted(Comparator.comparing(stage -> stage.stageOrder))
            .map(stage -> new PartyDto.StageView(
                stage.stageRecordId,
                stage.stageCode,
                stage.stageName,
                stage.stageOrder,
                stage.stageStatus,
                stage.dueAt,
                stage.materials.stream()
                    .map(material -> new PartyDto.MaterialView(material.materialId, material.materialName, material.fileId, material.reviewStatus, material.submittedAt))
                    .toList()
            ))
            .toList();
        return new PartyDto.PartyInstanceView(instance.flowName, instance.instanceStatus, instance.currentStageCode, stages);
    }

    private void ensureManagerOrCadre(CurrentUser user) {
        if (!isManager(user) && !isCadre(user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "manager or cadre role required");
        }
    }

    private boolean isCadre(CurrentUser user) {
        return user.getRoles().contains("class_cadre");
    }

    private boolean isManager(CurrentUser user) {
        return user.getRoles().stream().anyMatch(role ->
            "teacher_admin".equals(role) || "college_leader".equals(role) || "system_admin".equals(role));
    }

    private String normalizeFilter(String value) {
        if (!StringUtils.hasText(value) || "all".equalsIgnoreCase(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static class PartyInstanceState {
        private final Long studentId;
        private final String studentNo;
        private final String studentName;
        private final String className;
        private final String flowName;
        private final String instanceStatus;
        private String currentStageCode;
        private Long currentStageRecordId;
        private final List<StageState> stages;

        private PartyInstanceState(Long studentId,
                                   String studentNo,
                                   String studentName,
                                   String className,
                                   String flowName,
                                   String instanceStatus,
                                   String currentStageCode,
                                   Long currentStageRecordId,
                                   List<StageState> stages) {
            this.studentId = studentId;
            this.studentNo = studentNo;
            this.studentName = studentName;
            this.className = className;
            this.flowName = flowName;
            this.instanceStatus = instanceStatus;
            this.currentStageCode = currentStageCode;
            this.currentStageRecordId = currentStageRecordId;
            this.stages = stages;
        }
    }

    private static class StageState {
        private final Long stageRecordId;
        private final String stageCode;
        private final String stageName;
        private final int stageOrder;
        private String stageStatus;
        private final String dueAt;
        private final List<MaterialState> materials = new ArrayList<>();

        private StageState(Long stageRecordId,
                           String stageCode,
                           String stageName,
                           int stageOrder,
                           String stageStatus,
                           String dueAt) {
            this.stageRecordId = stageRecordId;
            this.stageCode = stageCode;
            this.stageName = stageName;
            this.stageOrder = stageOrder;
            this.stageStatus = stageStatus;
            this.dueAt = dueAt;
        }
    }

    private record MaterialState(Long materialId, String materialName, Long fileId, String reviewStatus, String submittedAt) {
    }
}

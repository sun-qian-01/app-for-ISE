package com.ise.platform.modules.party;

import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PartyService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AtomicLong materialIdGenerator = new AtomicLong(5000);

    private final Map<Long, PartyInstanceState> instanceByStudentId = new HashMap<>();

    public PartyService() {
        List<StageState> stageStates = new ArrayList<>();
        stageStates.add(new StageState(1000L, "applicant", "入党申请人", 1, "approved", "2025-09-30 23:59:59"));
        stageStates.add(new StageState(1001L, "activist", "积极分子", 2, "approved", "2025-12-20 23:59:59"));
        stageStates.add(new StageState(1002L, "development_candidate", "发展对象", 3, "approved", "2026-03-20 23:59:59"));
        stageStates.add(new StageState(1003L, "probationary_party_member", "预备党员", 4, "reviewing", "2026-04-25 23:59:59"));
        stageStates.add(new StageState(1004L, "party_member", "正式党员", 5, "pending", "2027-04-25 23:59:59"));
        stageStates.get(3).materials.add(new MaterialState(4001L, "季度思想汇报", 31L, "pending", "2026-04-18 14:30:00"));
        instanceByStudentId.put(1L, new PartyInstanceState("入党流程", "processing", "probationary_party_member", 1003L, stageStates));
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

    public PartyDto.MaterialView submitMaterial(CurrentUser user, Long stageRecordId, PartyDto.MaterialSubmitRequest request) {
        Long studentId = user.getStudentId();
        if (studentId == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "student account required");
        }
        PartyInstanceState instance = instanceByStudentId.get(studentId);
        if (instance == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "party instance not found");
        }
        // Enforce linear flow: students can only submit materials for their current stage.
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

    private record PartyInstanceState(String flowName,
                                      String instanceStatus,
                                      String currentStageCode,
                                      Long currentStageRecordId,
                                      List<StageState> stages) {
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

package com.ise.platform.modules.party;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class PartyDto {

    private PartyDto() {
    }

    public static class MaterialSubmitRequest {
        @NotBlank(message = "materialName is required")
        private String materialName;

        @NotNull(message = "fileId is required")
        private Long fileId;

        public String getMaterialName() {
            return materialName;
        }

        public void setMaterialName(String materialName) {
            this.materialName = materialName;
        }

        public Long getFileId() {
            return fileId;
        }

        public void setFileId(Long fileId) {
            this.fileId = fileId;
        }
    }

    public static class PartyInstanceView {
        private String flowName;
        private String instanceStatus;
        private String currentStageCode;
        private List<StageView> stages;

        public PartyInstanceView(String flowName, String instanceStatus, String currentStageCode, List<StageView> stages) {
            this.flowName = flowName;
            this.instanceStatus = instanceStatus;
            this.currentStageCode = currentStageCode;
            this.stages = stages;
        }

        public String getFlowName() {
            return flowName;
        }

        public String getInstanceStatus() {
            return instanceStatus;
        }

        public String getCurrentStageCode() {
            return currentStageCode;
        }

        public List<StageView> getStages() {
            return stages;
        }
    }

    public static class StageView {
        private Long stageRecordId;
        private String stageCode;
        private String stageName;
        private int stageOrder;
        private String stageStatus;
        private String dueAt;
        private List<MaterialView> materials;

        public StageView(Long stageRecordId,
                         String stageCode,
                         String stageName,
                         int stageOrder,
                         String stageStatus,
                         String dueAt,
                         List<MaterialView> materials) {
            this.stageRecordId = stageRecordId;
            this.stageCode = stageCode;
            this.stageName = stageName;
            this.stageOrder = stageOrder;
            this.stageStatus = stageStatus;
            this.dueAt = dueAt;
            this.materials = materials;
        }

        public Long getStageRecordId() {
            return stageRecordId;
        }

        public String getStageCode() {
            return stageCode;
        }

        public String getStageName() {
            return stageName;
        }

        public int getStageOrder() {
            return stageOrder;
        }

        public String getStageStatus() {
            return stageStatus;
        }

        public String getDueAt() {
            return dueAt;
        }

        public List<MaterialView> getMaterials() {
            return materials;
        }
    }

    public static class MaterialView {
        private Long materialId;
        private String materialName;
        private Long fileId;
        private String reviewStatus;
        private String submittedAt;

        public MaterialView(Long materialId, String materialName, Long fileId, String reviewStatus, String submittedAt) {
            this.materialId = materialId;
            this.materialName = materialName;
            this.fileId = fileId;
            this.reviewStatus = reviewStatus;
            this.submittedAt = submittedAt;
        }

        public Long getMaterialId() {
            return materialId;
        }

        public String getMaterialName() {
            return materialName;
        }

        public Long getFileId() {
            return fileId;
        }

        public String getReviewStatus() {
            return reviewStatus;
        }

        public String getSubmittedAt() {
            return submittedAt;
        }
    }

    public static class FlowDefinitionView {
        private Long flowId;
        private String flowCode;
        private String flowName;
        private List<StageDefinitionView> stages;

        public FlowDefinitionView(Long flowId, String flowCode, String flowName, List<StageDefinitionView> stages) {
            this.flowId = flowId;
            this.flowCode = flowCode;
            this.flowName = flowName;
            this.stages = stages;
        }

        public Long getFlowId() {
            return flowId;
        }

        public String getFlowCode() {
            return flowCode;
        }

        public String getFlowName() {
            return flowName;
        }

        public List<StageDefinitionView> getStages() {
            return stages;
        }
    }

    public static class StageDefinitionView {
        private String stageCode;
        private String stageName;
        private int stageOrder;

        public StageDefinitionView(String stageCode, String stageName, int stageOrder) {
            this.stageCode = stageCode;
            this.stageName = stageName;
            this.stageOrder = stageOrder;
        }

        public String getStageCode() {
            return stageCode;
        }

        public String getStageName() {
            return stageName;
        }

        public int getStageOrder() {
            return stageOrder;
        }
    }
}

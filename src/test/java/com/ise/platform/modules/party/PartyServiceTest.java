package com.ise.platform.modules.party;

import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.common.security.DataScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PartyServiceTest {

    private final PartyService partyService = new PartyService();

    @Test
    void flowsShouldContainOrderedStages() {
        var flows = partyService.flows();
        assertThat(flows).extracting(PartyDto.FlowDefinitionView::getFlowName)
            .containsExactly("入党流程", "入团流程");
        assertThat(flows.get(0).getStages()).isNotEmpty();
        assertThat(flows.get(0).getStages().get(0).getStageOrder()).isEqualTo(1);
        assertThat(flows.get(1).getStages()).extracting(PartyDto.StageDefinitionView::getStageName)
            .containsExactly("入团申请人", "入团积极分子", "发展对象", "正式团员");
    }

    @Test
    void myInstancesShouldExposePartyAndLeagueFlows() {
        CurrentUser user = studentUser(1L, "20220001");

        var flows = partyService.myInstances(user);

        assertThat(flows).extracting(PartyDto.PartyInstanceView::getFlowName)
            .containsExactly("入党流程", "入团流程");
        assertThat(flows.get(1).getCurrentStageCode()).isEqualTo("league_member");
        assertThat(flows.get(1).getStages()).extracting(PartyDto.StageView::getStageName)
            .containsExactly("入团申请人", "入团积极分子", "发展对象", "正式团员");
    }

    @Test
    void submitMaterialShouldFailWhenNotCurrentStage() {
        CurrentUser user = studentUser(1L, "20220001");
        PartyDto.MaterialSubmitRequest request = new PartyDto.MaterialSubmitRequest();
        request.setMaterialName("测试材料");
        request.setFileId(99L);

        assertThatThrownBy(() -> partyService.submitMaterial(user, 1001L, request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("only current stage")
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.STATUS_CONFLICT);
    }

    @Test
    void submitMaterialShouldSucceedOnCurrentStage() {
        CurrentUser user = studentUser(1L, "20220001");
        PartyDto.MaterialSubmitRequest request = new PartyDto.MaterialSubmitRequest();
        request.setMaterialName("季度思想汇报补交");
        request.setFileId(88L);
        request.setDescription("补交本季度思想汇报");

        PartyDto.MaterialView view = partyService.submitMaterial(user, 1003L, request);
        assertThat(view.getMaterialId()).isNotNull();
        assertThat(view.getMaterialName()).isEqualTo("季度思想汇报补交");
        assertThat(view.getReviewStatus()).isEqualTo("pending");
        assertThat(view.getDescription()).isEqualTo("补交本季度思想汇报");
    }

    @Test
    void myInstanceShouldExposeCurrentStageMaterialSubmissionInfo() {
        CurrentUser user = studentUser(1L, "20220001");

        PartyDto.PartyInstanceView view = partyService.myInstance(user);
        PartyDto.StageView currentStage = view.getStages().stream()
            .filter(stage -> view.getCurrentStageCode().equals(stage.getStageCode()))
            .findFirst()
            .orElseThrow();

        assertThat(currentStage.getStageName()).isEqualTo("预备党员");
        assertThat(currentStage.isSubmissionAllowed()).isTrue();
        assertThat(currentStage.getRequiredMaterials()).contains("季度思想汇报");
        assertThat(currentStage.getSubmitInstruction()).contains("思想汇报");
    }

    @Test
    void rejectedStageShouldExposeReturnReasonAndAllowResubmit() {
        CurrentUser teacher = teacherUser();
        PartyDto.PartyReviewRequest reviewRequest = new PartyDto.PartyReviewRequest();
        reviewRequest.setAction("reject");
        reviewRequest.setComment("材料需补充，请按清单补正后重提");

        partyService.reviewStage(teacher, 1003L, reviewRequest);
        PartyDto.PartyInstanceView view = partyService.myInstance(studentUser(1L, "20220001"));
        PartyDto.StageView currentStage = view.getStages().stream()
            .filter(stage -> stage.getStageRecordId().equals(1003L))
            .findFirst()
            .orElseThrow();

        assertThat(currentStage.getStageStatus()).isEqualTo("returned");
        assertThat(currentStage.isSubmissionAllowed()).isTrue();
        assertThat(currentStage.getReviewComment()).isEqualTo("材料需补充，请按清单补正后重提");
    }

    private CurrentUser studentUser(Long studentId, String username) {
        return new CurrentUser(
            1L,
            username,
            "测试学生",
            "student",
            List.of("student"),
            List.of("party:material:self:submit"),
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
            List.of("party:instance:scope:view", "party:todo:remind"),
            List.of(new DataScope("class", "软件工程2班")),
            null
        );
    }
}

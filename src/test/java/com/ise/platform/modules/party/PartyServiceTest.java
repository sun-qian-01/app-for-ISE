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
        assertThat(flows).isNotEmpty();
        assertThat(flows.get(0).getStages()).isNotEmpty();
        assertThat(flows.get(0).getStages().get(0).getStageOrder()).isEqualTo(1);
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

        PartyDto.MaterialView view = partyService.submitMaterial(user, 1003L, request);
        assertThat(view.getMaterialId()).isNotNull();
        assertThat(view.getMaterialName()).isEqualTo("季度思想汇报补交");
        assertThat(view.getReviewStatus()).isEqualTo("pending");
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
}

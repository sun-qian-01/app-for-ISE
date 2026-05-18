package com.ise.platform.modules.party;

import com.ise.platform.common.api.ApiResponse;
import com.ise.platform.common.security.AuthContext;
import com.ise.platform.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/party")
public class PartyController {

    private final PartyService partyService;

    public PartyController(PartyService partyService) {
        this.partyService = partyService;
    }

    @GetMapping("/instances/me")
    public ApiResponse<PartyDto.PartyInstanceView> myInstance() {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(partyService.myInstance(user));
    }

    @PostMapping("/stage-records/{stageRecordId}/materials")
    public ApiResponse<PartyDto.MaterialView> submitMaterial(@PathVariable Long stageRecordId,
                                                             @Valid @RequestBody PartyDto.MaterialSubmitRequest request) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(partyService.submitMaterial(user, stageRecordId, request));
    }
}

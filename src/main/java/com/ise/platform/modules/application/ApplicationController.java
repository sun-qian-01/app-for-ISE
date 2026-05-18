package com.ise.platform.modules.application;

import com.ise.platform.common.api.ApiResponse;
import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.security.AuthContext;
import com.ise.platform.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/my")
    public ApiResponse<PagedData<ApplicationDto.ApplicationView>> myApplications(
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String applicationType,
        @RequestParam(required = false) String status
    ) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(applicationService.myApplications(user, pageNo, pageSize, applicationType, status));
    }

    @PostMapping
    public ApiResponse<ApplicationDto.CreateResponse> create(@Valid @RequestBody ApplicationDto.CreateRequest request) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(applicationService.create(user, request));
    }

    @GetMapping("/{applicationId}")
    public ApiResponse<ApplicationDto.ApplicationDetailView> detail(@PathVariable Long applicationId) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(applicationService.detail(user, applicationId));
    }

    @PostMapping("/{applicationId}/revoke")
    public ApiResponse<ApplicationDto.ActionResponse> revoke(@PathVariable Long applicationId,
                                                             @Valid @RequestBody ApplicationDto.RevokeRequest request) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(applicationService.revoke(user, applicationId, request));
    }

    @GetMapping("/approvals/pending")
    public ApiResponse<PagedData<ApplicationDto.ApplicationView>> pendingApprovals(
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String applicationType,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long templateId
    ) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(
            applicationService.pendingApprovals(user, pageNo, pageSize, applicationType, status, templateId)
        );
    }

    @PostMapping("/{applicationId}/approve")
    public ApiResponse<ApplicationDto.ActionResponse> approve(@PathVariable Long applicationId,
                                                              @Valid @RequestBody ApplicationDto.ApprovalActionRequest request) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(applicationService.approve(user, applicationId, request));
    }

    @PostMapping("/{applicationId}/reject")
    public ApiResponse<ApplicationDto.ActionResponse> reject(@PathVariable Long applicationId,
                                                             @Valid @RequestBody ApplicationDto.ApprovalActionRequest request) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(applicationService.reject(user, applicationId, request));
    }
}

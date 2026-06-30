package com.ise.platform.modules.audit;

import com.ise.platform.common.api.ApiResponse;
import com.ise.platform.common.security.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<AuditLogDto.AuditLogView>> logs() {
        AuthContext.requireAnyRole("teacher_admin", "college_leader", "system_admin");
        return ApiResponse.success(auditLogService.listLogs());
    }
}

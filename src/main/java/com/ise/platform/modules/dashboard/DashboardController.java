package com.ise.platform.modules.dashboard;

import com.ise.platform.common.api.ApiResponse;
import com.ise.platform.common.security.AuthContext;
import com.ise.platform.common.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/student")
    public ApiResponse<Map<String, Object>> student() {
        CurrentUser user = AuthContext.requireUser();
        AuthContext.requireAnyRole("student", "class_cadre");
        return ApiResponse.success(dashboardService.studentDashboard(user));
    }

    @GetMapping("/admin")
    public ApiResponse<Map<String, Object>> admin() {
        AuthContext.requireAnyRole("teacher_admin", "college_leader", "system_admin");
        return ApiResponse.success(dashboardService.adminDashboard());
    }

    @GetMapping("/leader")
    public ApiResponse<Map<String, Object>> leader() {
        AuthContext.requireAnyRole("college_leader", "teacher_admin", "system_admin");
        return ApiResponse.success(dashboardService.leaderDashboard());
    }
}

package com.ise.platform.modules.dashboard;

import com.ise.platform.common.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    public Map<String, Object> studentDashboard(CurrentUser user) {
        return Map.of(
            "todoCount", 3,
            "unreadNoticeCount", 2,
            "recentNotices", List.of(
                Map.of("noticeId", 1, "title", "2026 年春季学期奖学金材料提交通知", "publishAt", "2026-04-19 12:00:00"),
                Map.of("noticeId", 2, "title", "预备党员季度思想汇报提醒", "publishAt", "2026-04-17 16:30:00")
            ),
            "currentPartyStage", "预备党员",
            "pendingMaterials", List.of(
                Map.of("stageRecordId", 1001, "materialName", "季度思想汇报", "dueAt", "2026-04-25 23:59:59")
            ),
            "applicationProgress", List.of(
                Map.of("applicationNo", "APP20260418001", "status", "reviewing", "currentApprover", "辅导员 李老师")
            ),
            "profileTags", List.of("2022级", "软件工程", "就业意向"),
            "studentNo", user.getUsername()
        );
    }

    public Map<String, Object> adminDashboard() {
        return Map.of(
            "studentTotal", 1200,
            "pendingApprovalCount", 17,
            "todayPushCount", 9,
            "riskWarningCount", 12,
            "todoTasks", List.of(
                Map.of("module", "application", "task", "待审批申请 11 条", "priority", "high"),
                Map.of("module", "party", "task", "需补材料 6 条", "priority", "medium")
            ),
            "hotKnowledgeArticles", List.of(
                Map.of("articleId", 1, "title", "国家奖学金评定流程说明", "viewCount", 426),
                Map.of("articleId", 3, "title", "党员发展阶段材料清单", "viewCount", 311)
            ),
            "noticeReadStats", Map.of("totalUsers", 238, "readUsers", 196, "readRate", 0.82)
        );
    }

    public Map<String, Object> leaderDashboard() {
        return Map.of(
            "studentTotal", 1200,
            "partyProcessActive", 71,
            "noticeReadRate", 0.82,
            "applicationApprovedRate", 0.91
        );
    }
}

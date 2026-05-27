package com.ise.platform.modules.dashboard;

import com.ise.platform.common.security.CurrentUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;

    public DashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> studentDashboard(CurrentUser user) {
        Long userId = user.getId();

        Integer unreadNoticeCount = jdbcTemplate.queryForObject(
            """
                select count(*)
                  from biz_notice n
                  left join biz_notice_read r
                    on r.notice_id = n.id and r.user_id = ?
                 where n.is_deleted = 0
                   and n.status = 'published'
                   and r.notice_id is null
                """,
            Integer.class,
            userId
        );

        Integer todoCount = jdbcTemplate.queryForObject(
            "select count(*) from biz_application where applicant_user_id = ? and status in ('submitted', 'reviewing') and is_deleted = 0",
            Integer.class,
            userId
        );

        String currentPartyStage = "预备党员";
        return Map.of(
            "todoCount", todoCount == null ? 0 : todoCount,
            "unreadNoticeCount", unreadNoticeCount == null ? 0 : unreadNoticeCount,
            "currentPartyStage", currentPartyStage,
            "growthCount", 3
        );
    }

    public Map<String, Object> adminDashboard() {
        Integer studentTotal = jdbcTemplate.queryForObject("select count(*) from stu_student where is_deleted = 0", Integer.class);
        Integer pendingApprovalCount = jdbcTemplate.queryForObject(
            "select count(*) from biz_application where is_deleted = 0 and status in ('submitted', 'reviewing')",
            Integer.class
        );
        Integer knowledgeCount = jdbcTemplate.queryForObject("select count(*) from kb_article where is_deleted = 0", Integer.class);
        Integer templateCount = jdbcTemplate.queryForObject("select count(*) from kb_template where is_deleted = 0", Integer.class);

        return Map.of(
            "studentCount", studentTotal == null ? 0 : studentTotal,
            "pendingApprovalCount", pendingApprovalCount == null ? 0 : pendingApprovalCount,
            "todayPushCount", 3,
            "riskCount", 5,
            "board", List.of(
                List.of("知识库条目", String.valueOf(knowledgeCount == null ? 0 : knowledgeCount)),
                List.of("政策模板", templateCount == null ? "0 份" : templateCount + " 份"),
                List.of("党团流程进行中", "2 人"),
                List.of("通知平均已读率", "84%")
            )
        );
    }

    public Map<String, Object> leaderDashboard() {
        Integer studentTotal = jdbcTemplate.queryForObject("select count(*) from stu_student where is_deleted = 0", Integer.class);
        Integer partyProcessActive = 2;
        return Map.of(
            "studentTotal", studentTotal == null ? 0 : studentTotal,
            "partyProcessActive", partyProcessActive,
            "noticeReadRate", 0.84,
            "applicationApprovedRate", 0.89
        );
    }
}

package com.ise.platform.modules.dashboard;

import com.ise.platform.common.security.CurrentUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DashboardService {

    private static final Pattern GRADE_PATTERN = Pattern.compile("(20\\d{2})(?:级|届)?");

    private final JdbcTemplate jdbcTemplate;

    public DashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> studentDashboard(CurrentUser user) {
        Long userId = user.getId();

        StudentTargetProfile profile = findStudentTargetProfile(user);
        Integer unreadNoticeCount = jdbcTemplate.query(
            """
                select n.audience
                  from biz_notice n
                  left join biz_notice_read r
                    on r.notice_id = n.id and r.user_id = ?
                 where n.is_deleted = 0
                   and n.status = 'published'
                   and r.notice_id is null
                """,
            (rs, rowNum) -> rs.getString("audience"),
            userId
        ).stream()
            .filter(audience -> matchesAudience(audience, profile))
            .toList()
            .size();

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
        Integer todayPushCount = jdbcTemplate.queryForObject(
            """
                select count(*)
                  from biz_notice
                 where is_deleted = 0
                   and status = 'published'
                   and publish_at >= ?
                   and publish_at < ?
                """,
            Integer.class,
            Timestamp.valueOf(LocalDate.now().atStartOfDay()),
            Timestamp.valueOf(LocalDate.now().plusDays(1).atStartOfDay())
        );
        NoticeStats noticeStats = loadNoticeStats();

        return Map.of(
            "studentCount", studentTotal == null ? 0 : studentTotal,
            "pendingApprovalCount", pendingApprovalCount == null ? 0 : pendingApprovalCount,
            "todayPushCount", todayPushCount == null ? 0 : todayPushCount,
            "riskCount", 5,
            "board", List.of(
                List.of("知识库条目", String.valueOf(knowledgeCount == null ? 0 : knowledgeCount)),
                List.of("政策模板", templateCount == null ? "0 份" : templateCount + " 份"),
                List.of("党团流程进行中", "2 人"),
                List.of("通知平均已读率", noticeStats.readRatePercent() + "%")
            ),
            "noticeReadRate", noticeStats.readRate()
        );
    }

    public Map<String, Object> leaderDashboard() {
        Integer studentTotal = jdbcTemplate.queryForObject("select count(*) from stu_student where is_deleted = 0", Integer.class);
        Integer partyProcessActive = 2;
        NoticeStats noticeStats = loadNoticeStats();
        return Map.of(
            "studentTotal", studentTotal == null ? 0 : studentTotal,
            "partyProcessActive", partyProcessActive,
            "noticeReadRate", noticeStats.readRate(),
            "applicationApprovedRate", 0.89
        );
    }

    private NoticeStats loadNoticeStats() {
        List<Map<String, Object>> notices = jdbcTemplate.queryForList(
            """
                select audience, delivered_count, read_count
                  from biz_notice
                 where is_deleted = 0
                   and status = 'published'
                """
        );

        int totalDelivered = 0;
        int totalRead = 0;
        for (Map<String, Object> notice : notices) {
            int readCount = Math.max(numberValue(notice.get("read_count")), 0);
            int deliveredCount = Math.max(numberValue(notice.get("delivered_count")), 0);
            if (deliveredCount <= 0) {
                deliveredCount = countTargetStudents((String) notice.get("audience"));
            }
            deliveredCount = Math.max(deliveredCount, readCount);
            totalDelivered += deliveredCount;
            totalRead += readCount;
        }
        return new NoticeStats(totalDelivered, totalRead);
    }

    private int numberValue(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private int countTargetStudents(String audience) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "select count(*) from stu_student where is_deleted = 0 and status <> 'graduated'"
        );

        Set<String> grades = extractGrades(audience);
        if (!grades.isEmpty()) {
            sql.append(" and grade in (");
            sql.append("?,".repeat(grades.size()));
            sql.setLength(sql.length() - 1);
            sql.append(")");
            args.addAll(grades);
        }

        Integer matched = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        if (matched != null && matched > 0) {
            return matched;
        }

        Integer total = jdbcTemplate.queryForObject(
            "select count(*) from stu_student where is_deleted = 0 and status <> 'graduated'",
            Integer.class
        );
        return total == null ? 0 : total;
    }

    private Set<String> extractGrades(String audience) {
        Set<String> grades = new LinkedHashSet<>();
        if (audience == null || audience.isBlank()) {
            return grades;
        }
        Matcher matcher = GRADE_PATTERN.matcher(audience);
        while (matcher.find()) {
            grades.add(matcher.group(1));
        }
        return grades;
    }

    private StudentTargetProfile findStudentTargetProfile(CurrentUser user) {
        if (user.getStudentId() == null) {
            return null;
        }
        List<StudentTargetProfile> rows = jdbcTemplate.query(
            """
                select grade, status
                  from stu_student
                 where id = ?
                   and is_deleted = 0
                   and status <> 'graduated'
                """,
            (rs, rowNum) -> new StudentTargetProfile(rs.getString("grade"), rs.getString("status")),
            user.getStudentId()
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    private boolean matchesAudience(String audience, StudentTargetProfile profile) {
        if (profile == null) {
            return false;
        }
        TargetCriteria criteria = parseTargetCriteria(audience);
        if (criteria.isEmpty()) {
            return true;
        }
        return criteria.grades().contains(profile.grade()) || criteria.statuses().contains(profile.status());
    }

    private TargetCriteria parseTargetCriteria(String audience) {
        Set<String> grades = new LinkedHashSet<>();
        Set<String> statuses = new LinkedHashSet<>();
        if (!StringUtils.hasText(audience)) {
            return new TargetCriteria(grades, statuses);
        }
        Matcher matcher = GRADE_PATTERN.matcher(audience);
        while (matcher.find()) {
            grades.add(matcher.group(1));
        }
        String normalized = audience.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("重点关注") || normalized.contains("warning")) {
            statuses.add("warning");
        }
        if (normalized.contains("毕业年级") || normalized.contains("graduating")) {
            statuses.add("graduating");
        }
        return new TargetCriteria(grades, statuses);
    }

    private record TargetCriteria(Set<String> grades, Set<String> statuses) {
        boolean isEmpty() {
            return grades.isEmpty() && statuses.isEmpty();
        }
    }

    private record StudentTargetProfile(String grade, String status) {
    }

    private record NoticeStats(int deliveredCount, int readCount) {

        double readRate() {
            if (deliveredCount <= 0) {
                return 0;
            }
            return (double) readCount / deliveredCount;
        }

        int readRatePercent() {
            return deliveredCount <= 0 ? 0 : (int) Math.round(readRate() * 100);
        }
    }
}

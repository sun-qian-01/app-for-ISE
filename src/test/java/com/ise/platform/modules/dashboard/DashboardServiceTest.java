package com.ise.platform.modules.dashboard;

import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.common.security.DataScope;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardServiceTest {

    private JdbcTemplate jdbcTemplate;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:dashboard-service-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        jdbcTemplate = new JdbcTemplate(dataSource);
        createSchema();
        resetData();
        dashboardService = new DashboardService(jdbcTemplate);
    }

    @Test
    void adminDashboardShouldUseActualNoticeStats() {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
            """
                insert into biz_notice (id, title, audience, delivered_count, read_count, status, publish_at, is_deleted)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
            1L, "今日通知", "全体学生", 3, 1, "published", Timestamp.valueOf(now), 0
        );
        jdbcTemplate.update(
            """
                insert into biz_notice (id, title, audience, delivered_count, read_count, status, publish_at, is_deleted)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
            2L, "历史通知", "2022级学生", 0, 2, "published", Timestamp.valueOf(now.minusDays(1)), 0
        );
        jdbcTemplate.update(
            """
                insert into biz_notice (id, title, audience, delivered_count, read_count, status, publish_at, is_deleted)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
            3L, "草稿通知", "全体学生", 5, 5, "draft", Timestamp.valueOf(now), 0
        );

        Map<String, Object> dashboard = dashboardService.adminDashboard();

        assertThat(dashboard.get("todayPushCount")).isEqualTo(1);
        assertThat(dashboard.get("noticeReadRate")).isEqualTo(0.6);
        assertThat(boardValue(dashboard, "通知平均已读率")).isEqualTo("60%");
    }

    @Test
    void adminDashboardShouldCapNoticeReadRateWhenStoredReadCountExceedsDeliveredCount() {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
            """
                insert into biz_notice (id, title, audience, delivered_count, read_count, status, publish_at, is_deleted)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
            1L, "异常计数通知", "2022级学生", 1, 3, "published", Timestamp.valueOf(now), 0
        );

        Map<String, Object> dashboard = dashboardService.adminDashboard();

        assertThat(dashboard.get("noticeReadRate")).isEqualTo(1.0);
        assertThat(boardValue(dashboard, "通知平均已读率")).isEqualTo("100%");
    }

    @Test
    void studentDashboardUnreadNoticeCountShouldOnlyIncludeVisibleNotices() {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
            """
                insert into biz_notice (id, title, audience, delivered_count, read_count, status, publish_at, is_deleted)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
            1L, "本年级未读通知", "2022级学生", 2, 0, "published", Timestamp.valueOf(now), 0
        );
        jdbcTemplate.update(
            """
                insert into biz_notice (id, title, audience, delivered_count, read_count, status, publish_at, is_deleted)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
            2L, "其他年级通知", "2026级学生", 1, 0, "published", Timestamp.valueOf(now), 0
        );
        jdbcTemplate.update(
            """
                insert into biz_notice (id, title, audience, delivered_count, read_count, status, publish_at, is_deleted)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
            3L, "本年级已读通知", "2022级学生", 2, 1, "published", Timestamp.valueOf(now), 0
        );
        jdbcTemplate.update(
            "insert into biz_notice_read (id, notice_id, user_id, read_at) values (?, ?, ?, ?)",
            1L, 3L, 1L, Timestamp.valueOf(now)
        );

        Map<String, Object> dashboard = dashboardService.studentDashboard(studentUser());

        assertThat(dashboard.get("unreadNoticeCount")).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    private String boardValue(Map<String, Object> dashboard, String label) {
        return ((List<List<String>>) dashboard.get("board")).stream()
            .filter(item -> item.get(0).equals(label))
            .map(item -> item.get(1))
            .findFirst()
            .orElseThrow();
    }

    private void createSchema() {
        jdbcTemplate.execute(
            """
                create table if not exists stu_student (
                    id bigint primary key,
                    student_no varchar(32) not null,
                    name varchar(64) not null,
                    grade varchar(16) not null,
                    status varchar(32) not null default 'active',
                    is_deleted smallint not null default 0
                )
                """
        );
        jdbcTemplate.execute(
            """
                create table if not exists biz_application (
                    id bigint primary key,
                    applicant_user_id bigint,
                    status varchar(32) not null,
                    is_deleted smallint not null default 0
                )
                """
        );
        jdbcTemplate.execute(
            """
                create table if not exists kb_article (
                    id bigint primary key,
                    is_deleted smallint not null default 0
                )
                """
        );
        jdbcTemplate.execute(
            """
                create table if not exists kb_template (
                    id bigint primary key,
                    is_deleted smallint not null default 0
                )
                """
        );
        jdbcTemplate.execute(
            """
                create table if not exists biz_notice (
                    id bigint primary key,
                    title varchar(255) not null,
                    audience varchar(255) not null,
                    delivered_count int not null default 0,
                    read_count int not null default 0,
                    status varchar(32) not null default 'published',
                    publish_at timestamp not null default current_timestamp,
                    is_deleted smallint not null default 0
                )
                """
        );
        jdbcTemplate.execute(
            """
                create table if not exists biz_notice_read (
                    id bigint primary key,
                    notice_id bigint not null,
                    user_id bigint not null,
                    read_at timestamp
                )
                """
        );
    }

    private void resetData() {
        jdbcTemplate.update("delete from biz_notice_read");
        jdbcTemplate.update("delete from biz_notice");
        jdbcTemplate.update("delete from kb_template");
        jdbcTemplate.update("delete from kb_article");
        jdbcTemplate.update("delete from biz_application");
        jdbcTemplate.update("delete from stu_student");
        jdbcTemplate.update(
            "insert into stu_student (id, student_no, name, grade, status, is_deleted) values (?, ?, ?, ?, ?, ?)",
            1L, "20220001", "学生1", "2022", "active", 0
        );
        jdbcTemplate.update(
            "insert into stu_student (id, student_no, name, grade, status, is_deleted) values (?, ?, ?, ?, ?, ?)",
            2L, "20220002", "学生2", "2022", "active", 0
        );
        jdbcTemplate.update(
            "insert into stu_student (id, student_no, name, grade, status, is_deleted) values (?, ?, ?, ?, ?, ?)",
            3L, "20230001", "学生3", "2023", "active", 0
        );
        jdbcTemplate.update(
            "insert into stu_student (id, student_no, name, grade, status, is_deleted) values (?, ?, ?, ?, ?, ?)",
            4L, "20200001", "毕业生", "2020", "graduated", 0
        );
    }

    private CurrentUser studentUser() {
        return new CurrentUser(
            1L,
            "20220001",
            "学生1",
            "student",
            List.of("student"),
            List.of("student:dashboard:view"),
            List.of(new DataScope("self", "1")),
            1L
        );
    }
}

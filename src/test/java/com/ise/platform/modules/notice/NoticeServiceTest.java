package com.ise.platform.modules.notice;

import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.common.security.DataScope;
import com.ise.platform.modules.audit.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeServiceTest {

    private final NoticeService noticeService;
    private final JdbcTemplate jdbcTemplate;

    NoticeServiceTest() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:notice-service-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute("""
            create table if not exists stu_student (
                id bigint primary key,
                student_no varchar(32),
                name varchar(64),
                grade varchar(16),
                major varchar(64),
                class_name varchar(64),
                political_status varchar(32),
                status varchar(32),
                is_deleted smallint
            )
            """);
        jdbcTemplate.execute("""
            create table if not exists biz_notice (
                id bigint primary key,
                title varchar(255),
                content varchar(2000),
                audience varchar(255),
                channel_labels varchar(255),
                tag_labels varchar(255),
                delivered_count int,
                read_count int,
                status varchar(32),
                publish_at timestamp,
                created_by bigint,
                created_at timestamp,
                updated_at timestamp,
                is_deleted smallint
            )
            """);
        jdbcTemplate.execute("""
            create table if not exists biz_notice_read (
                id bigint primary key,
                notice_id bigint,
                user_id bigint,
                read_at timestamp
            )
            """);
        jdbcTemplate.execute("""
            create table if not exists sys_audit_log (
                id bigint primary key,
                actor_user_id bigint,
                actor_name varchar(128),
                module_name varchar(64),
                action_text varchar(500),
                result_text varchar(32),
                created_at timestamp
            )
            """);
        jdbcTemplate.update("delete from stu_student");
        jdbcTemplate.update("delete from sys_audit_log");
        jdbcTemplate.update("delete from biz_notice_read");
        jdbcTemplate.update("delete from biz_notice");
        jdbcTemplate.update("""
            insert into stu_student (id, student_no, name, grade, major, class_name, political_status, status, is_deleted)
            values
            (1, '20220001', '赵晨曦', '2022', '软件工程', '软件工程2班', '预备党员', 'active', 0),
            (2, '20220018', '陈一诺', '2022', '软件工程', '软件工程2班', '发展对象', 'active', 0),
            (3, '20260031', '林嘉禾', '2026', '数据科学', '数据科学1班', '共青团员', 'active', 0),
            (4, '20230007', '周明远', '2023', '软件工程', '软件工程1班', '共青团员', 'warning', 0)
            """);
        jdbcTemplate.update("""
            insert into biz_notice (id, title, content, audience, channel_labels, tag_labels, delivered_count, read_count, status, publish_at, created_by, created_at, updated_at, is_deleted)
            values
            (1, '通知1', '内容1', '2022级', '站内,微信', '奖助', 100, 10, 'published', current_timestamp(), 8, current_timestamp(), current_timestamp(), 0),
            (2, '通知2', '内容2', '2022级', '站内', '党团', 100, 20, 'published', current_timestamp(), 8, current_timestamp(), current_timestamp(), 0),
            (3, '通知3', '内容3', '2026届毕业生', '站内', '就业', 0, 0, 'published', current_timestamp(), 8, current_timestamp(), current_timestamp(), 0)
            """);
        this.noticeService = new NoticeService(jdbcTemplate, new AuditLogService(jdbcTemplate));
    }

    @Test
    void markReadShouldAffectUnreadResult() {
        CurrentUser user = studentUser();
        noticeService.markRead(user, 1L);
        PagedData<NoticeDto.NoticeView> unread = noticeService.myNotices(user, 1, 10, "unread", null);
        assertThat(unread.getRecords()).allMatch(item -> !Long.valueOf(1L).equals(item.getId()));
    }

    @Test
    void noticeViewShouldExposeUnreadCount() {
        PagedData<NoticeDto.NoticeView> page = noticeService.myNotices(studentUser(), 1, 10, "all", null);

        NoticeDto.NoticeView notice = page.getRecords().stream()
            .filter(item -> Long.valueOf(1L).equals(item.getId()))
            .findFirst()
            .orElseThrow();

        assertThat(notice.getDeliveredCount()).isEqualTo(100);
        assertThat(notice.getReadCount()).isEqualTo(10);
        assertThat(notice.getUnreadCount()).isEqualTo(90);
    }

    @Test
    void legacyZeroDeliveredNoticeShouldFallbackToAudienceStudentCount() {
        PagedData<NoticeDto.NoticeView> page = noticeService.myNotices(otherStudentUser(), 1, 10, "all", null);

        NoticeDto.NoticeView notice = page.getRecords().stream()
            .filter(item -> Long.valueOf(3L).equals(item.getId()))
            .findFirst()
            .orElseThrow();

        assertThat(notice.getDeliveredCount()).isEqualTo(1);
        assertThat(notice.getReadCount()).isZero();
        assertThat(notice.getUnreadCount()).isEqualTo(1);
    }

    @Test
    void createNoticeShouldSetDeliveredCountFromAudienceStudents() {
        NoticeDto.CreateNoticeRequest request = new NoticeDto.CreateNoticeRequest();
        request.setTitle("2022级通知");
        request.setContent("请及时查看通知");
        request.setAudience("2022级学生");
        request.setChannelLabels(List.of("站内"));
        request.setTags(List.of("测试"));

        NoticeDto.NoticeView created = noticeService.createNotice(managerUser(), request);

        assertThat(created.getDeliveredCount()).isEqualTo(2);
        assertThat(created.getReadCount()).isZero();
        assertThat(created.getUnreadCount()).isEqualTo(2);
    }

    @Test
    void createNoticeShouldUnionGradeAndStatusAudience() {
        NoticeDto.CreateNoticeRequest request = new NoticeDto.CreateNoticeRequest();
        request.setTitle("重点范围通知");
        request.setContent("请及时查看通知");
        request.setAudience("2022级和重点关注");
        request.setChannelLabels(List.of("站内"));
        request.setTags(List.of("测试"));

        NoticeDto.NoticeView created = noticeService.createNotice(managerUser(), request);

        assertThat(created.getDeliveredCount()).isEqualTo(3);
        assertThat(created.getReadCount()).isZero();
        assertThat(created.getUnreadCount()).isEqualTo(3);
    }

    @Test
    void myNoticesShouldOnlyShowMatchedAudience() {
        NoticeDto.CreateNoticeRequest request = new NoticeDto.CreateNoticeRequest();
        request.setTitle("定向通知");
        request.setContent("请及时查看通知");
        request.setAudience("2022级和重点关注");
        request.setChannelLabels(List.of("站内"));
        request.setTags(List.of("测试"));

        NoticeDto.NoticeView created = noticeService.createNotice(managerUser(), request);

        assertThat(noticeService.myNotices(studentUser(), 1, 10, "all", null).getRecords())
            .anyMatch(item -> created.getId().equals(item.getId()));
        assertThat(noticeService.myNotices(warningStudentUser(), 1, 10, "all", null).getRecords())
            .anyMatch(item -> created.getId().equals(item.getId()));
        assertThat(noticeService.myNotices(otherStudentUser(), 1, 10, "all", null).getRecords())
            .noneMatch(item -> created.getId().equals(item.getId()));
    }

    @Test
    void adminNoticeListShouldNotExposeReadCountAboveDeliveredCount() {
        jdbcTemplate.update("""
            insert into biz_notice (id, title, content, audience, channel_labels, tag_labels, delivered_count, read_count, status, publish_at, created_by, created_at, updated_at, is_deleted)
            values
            (10, '异常计数通知', '内容', '2022级', '站内', '测试', 1, 3, 'published', current_timestamp(), 8, current_timestamp(), current_timestamp(), 0)
            """);

        NoticeDto.NoticeView notice = noticeService.listNotices(managerUser(), 1, 10, "all", null)
            .getRecords()
            .stream()
            .filter(item -> Long.valueOf(10L).equals(item.getId()))
            .findFirst()
            .orElseThrow();

        assertThat(notice.getDeliveredCount()).isEqualTo(3);
        assertThat(notice.getReadCount()).isEqualTo(3);
        assertThat(notice.getUnreadCount()).isZero();
    }

    @Test
    void createNoticeShouldRecordAuditLog() {
        NoticeDto.CreateNoticeRequest request = new NoticeDto.CreateNoticeRequest();
        request.setTitle("审计日志通知");
        request.setContent("请及时查看通知");
        request.setAudience("2022级学生");
        request.setChannelLabels(List.of("站内"));
        request.setTags(List.of("测试"));

        noticeService.createNotice(managerUser(), request);

        Integer count = jdbcTemplate.queryForObject(
            """
                select count(*)
                  from sys_audit_log
                 where actor_user_id = ?
                   and actor_name = ?
                   and module_name = ?
                   and action_text = ?
                   and result_text = ?
                """,
            Integer.class,
            8L,
            "李老师",
            "通知",
            "发布定向通知：审计日志通知",
            "成功"
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void markAllReadShouldClearUnreadList() {
        CurrentUser user = studentUser();
        noticeService.markAllRead(user);
        PagedData<NoticeDto.NoticeView> unread = noticeService.myNotices(user, 1, 10, "unread", null);
        assertThat(unread.getRecords()).isEmpty();
    }

    private CurrentUser studentUser() {
        return new CurrentUser(
            1L,
            "20220001",
            "赵晨曦",
            "student",
            List.of("student"),
            List.of("notice:my:view"),
            List.of(new DataScope("self", "1")),
            1L
        );
    }

    private CurrentUser otherStudentUser() {
        return new CurrentUser(
            3L,
            "20260031",
            "林嘉禾",
            "student",
            List.of("student"),
            List.of("notice:my:view"),
            List.of(new DataScope("self", "3")),
            3L
        );
    }

    private CurrentUser warningStudentUser() {
        return new CurrentUser(
            4L,
            "20230007",
            "周明远",
            "student",
            List.of("student"),
            List.of("notice:my:view"),
            List.of(new DataScope("self", "4")),
            4L
        );
    }

    private CurrentUser managerUser() {
        return new CurrentUser(
            8L,
            "teacher001",
            "李老师",
            "teacher",
            List.of("teacher_admin"),
            List.of("notice:publish"),
            List.of(new DataScope("college", "information-school")),
            null
        );
    }
}

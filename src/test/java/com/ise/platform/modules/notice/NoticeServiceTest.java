package com.ise.platform.modules.notice;

import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.common.security.DataScope;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeServiceTest {

    private final NoticeService noticeService;

    NoticeServiceTest() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:notice-service-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

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
        jdbcTemplate.update("delete from biz_notice_read");
        jdbcTemplate.update("delete from biz_notice");
        jdbcTemplate.update("""
            insert into biz_notice (id, title, content, audience, channel_labels, tag_labels, delivered_count, read_count, status, publish_at, created_by, created_at, updated_at, is_deleted)
            values
            (1, '通知1', '内容1', '2022级', '站内,微信', '奖助', 100, 10, 'published', current_timestamp(), 8, current_timestamp(), current_timestamp(), 0),
            (2, '通知2', '内容2', '2022级', '站内', '党团', 100, 20, 'published', current_timestamp(), 8, current_timestamp(), current_timestamp(), 0)
            """);
        this.noticeService = new NoticeService(jdbcTemplate);
    }

    @Test
    void markReadShouldAffectUnreadResult() {
        CurrentUser user = studentUser();
        noticeService.markRead(user, 1L);
        PagedData<NoticeDto.NoticeView> unread = noticeService.myNotices(user, 1, 10, "unread", null);
        assertThat(unread.getRecords()).allMatch(item -> !Long.valueOf(1L).equals(item.getId()));
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
}

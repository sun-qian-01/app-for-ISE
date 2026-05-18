package com.ise.platform.modules.notice;

import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.common.security.DataScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeServiceTest {

    private final NoticeService noticeService = new NoticeService();

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

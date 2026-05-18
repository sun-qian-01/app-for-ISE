package com.ise.platform.modules.notice;

import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class NoticeService {

    private final List<NoticeEntity> notices = List.of(
        new NoticeEntity(1L, "2026 年春季学期奖学金材料提交通知", "请于 4 月 24 日前完成材料提交。", List.of("奖助", "2022级"), "2026-04-19 12:00:00"),
        new NoticeEntity(2L, "预备党员季度思想汇报提醒", "你所在支部需于本周内补齐季度思想汇报。", List.of("党团"), "2026-04-17 16:30:00"),
        new NoticeEntity(3L, "毕业生就业信息登记更新说明", "就业去向信息已开放二次更新。", List.of("就业", "毕业年级"), "2026-04-15 09:00:00")
    );

    private final Map<Long, Set<Long>> readNoticeIdsByUserId = new ConcurrentHashMap<>();

    public PagedData<NoticeDto.NoticeView> myNotices(CurrentUser user,
                                                     int pageNo,
                                                     int pageSize,
                                                     String readStatus,
                                                     String tag) {
        Set<Long> readSet = readNoticeIdsByUserId.computeIfAbsent(user.getId(), ignored -> new HashSet<>());
        List<NoticeDto.NoticeView> filtered = notices.stream()
            .sorted(Comparator.comparing(NoticeEntity::publishAt).reversed())
            .filter(item -> !StringUtils.hasText(tag) || "all".equalsIgnoreCase(tag) || item.tags().contains(tag))
            .map(item -> {
                String status = readSet.contains(item.id()) ? "read" : "unread";
                return new NoticeDto.NoticeView(item.id(), item.title(), item.content(), item.tags(), item.publishAt(), status);
            })
            .filter(item -> !StringUtils.hasText(readStatus) || "all".equalsIgnoreCase(readStatus) || readStatus.equals(item.getReadStatus()))
            .collect(Collectors.toList());

        return paginate(filtered, pageNo, pageSize);
    }

    public void markRead(CurrentUser user, Long noticeId) {
        boolean exists = notices.stream().anyMatch(notice -> notice.id().equals(noticeId));
        if (!exists) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "notice not found");
        }
        readNoticeIdsByUserId.computeIfAbsent(user.getId(), ignored -> new HashSet<>()).add(noticeId);
    }

    public void markAllRead(CurrentUser user) {
        Set<Long> all = notices.stream().map(NoticeEntity::id).collect(Collectors.toSet());
        readNoticeIdsByUserId.put(user.getId(), all);
    }

    private PagedData<NoticeDto.NoticeView> paginate(List<NoticeDto.NoticeView> source, int pageNo, int pageSize) {
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        int from = (safePageNo - 1) * safePageSize;
        if (from >= source.size()) {
            return new PagedData<>(List.of(), safePageNo, safePageSize, source.size());
        }
        int to = Math.min(from + safePageSize, source.size());
        return new PagedData<>(new ArrayList<>(source.subList(from, to)), safePageNo, safePageSize, source.size());
    }

    private record NoticeEntity(Long id, String title, String content, List<String> tags, String publishAt) {
    }
}

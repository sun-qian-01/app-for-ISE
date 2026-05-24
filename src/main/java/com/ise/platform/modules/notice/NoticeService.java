package com.ise.platform.modules.notice;

import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class NoticeService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;

    public NoticeService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PagedData<NoticeDto.NoticeView> myNotices(CurrentUser user,
                                                     int pageNo,
                                                     int pageSize,
                                                     String readStatus,
                                                     String tag) {
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        String readStatusFilter = normalizeFilter(readStatus);
        String tagFilter = StringUtils.hasText(tag) ? tag.trim().toLowerCase(Locale.ROOT) : null;

        Long total = jdbcTemplate.queryForObject(
            """
                select count(*)
                  from biz_notice n
                  left join biz_notice_read r
                    on r.notice_id = n.id and r.user_id = ?
                 where n.is_deleted = 0
                   and n.status = 'published'
                   and (? is null
                        or (? = 'read' and r.notice_id is not null)
                        or (? = 'unread' and r.notice_id is null))
                   and (? is null or lower(coalesce(n.tag_labels, '')) like ?)
                """,
            Long.class,
            user.getId(),
            readStatusFilter,
            readStatusFilter,
            readStatusFilter,
            tagFilter,
            tagFilter == null ? null : "%" + tagFilter + "%"
        );

        List<NoticeDto.NoticeView> records = jdbcTemplate.query(
            """
                select n.id, n.title, n.content, n.tag_labels, n.channel_labels, n.audience,
                       n.delivered_count, n.read_count, n.publish_at,
                       case when r.notice_id is null then 'unread' else 'read' end as read_status
                  from biz_notice n
                  left join biz_notice_read r
                    on r.notice_id = n.id and r.user_id = ?
                 where n.is_deleted = 0
                   and n.status = 'published'
                   and (? is null
                        or (? = 'read' and r.notice_id is not null)
                        or (? = 'unread' and r.notice_id is null))
                   and (? is null or lower(coalesce(n.tag_labels, '')) like ?)
                 order by n.publish_at desc, n.id desc
                 limit ? offset ?
                """,
            (rs, rowNum) -> new NoticeDto.NoticeView(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("content"),
                splitLabels(rs.getString("tag_labels")),
                splitLabels(rs.getString("channel_labels")),
                rs.getString("audience"),
                rs.getInt("delivered_count"),
                rs.getInt("read_count"),
                format(rs.getTimestamp("publish_at")),
                rs.getString("read_status")
            ),
            user.getId(),
            readStatusFilter,
            readStatusFilter,
            readStatusFilter,
            tagFilter,
            tagFilter == null ? null : "%" + tagFilter + "%",
            safePageSize,
            (safePageNo - 1) * safePageSize
        );

        return new PagedData<>(records, safePageNo, safePageSize, total == null ? 0 : total);
    }

    public PagedData<NoticeDto.NoticeView> listNotices(CurrentUser user,
                                                       int pageNo,
                                                       int pageSize,
                                                       String readStatus,
                                                       String keyword) {
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        String readStatusFilter = normalizeFilter(readStatus);
        String keywordLike = StringUtils.hasText(keyword) ? "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%" : null;

        Long total = jdbcTemplate.queryForObject(
            """
                select count(*)
                  from biz_notice n
                 where n.is_deleted = 0
                   and (? is null
                        or (? = 'read' and n.read_count > 0)
                        or (? = 'unread' and n.read_count < n.delivered_count))
                   and (? is null or lower(n.title) like ? or lower(n.audience) like ? or lower(coalesce(n.tag_labels, '')) like ?)
                """,
            Long.class,
            readStatusFilter,
            readStatusFilter,
            readStatusFilter,
            keywordLike,
            keywordLike,
            keywordLike,
            keywordLike
        );

        List<NoticeDto.NoticeView> records = jdbcTemplate.query(
            """
                select n.id, n.title, n.content, n.tag_labels, n.channel_labels, n.audience,
                       n.delivered_count, n.read_count, n.publish_at,
                       case when n.read_count > 0 then 'read' else 'unread' end as read_status
                  from biz_notice n
                 where n.is_deleted = 0
                   and (? is null
                        or (? = 'read' and n.read_count > 0)
                        or (? = 'unread' and n.read_count < n.delivered_count))
                   and (? is null or lower(n.title) like ? or lower(n.audience) like ? or lower(coalesce(n.tag_labels, '')) like ?)
                 order by n.publish_at desc, n.id desc
                 limit ? offset ?
                """,
            (rs, rowNum) -> new NoticeDto.NoticeView(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("content"),
                splitLabels(rs.getString("tag_labels")),
                splitLabels(rs.getString("channel_labels")),
                rs.getString("audience"),
                rs.getInt("delivered_count"),
                rs.getInt("read_count"),
                format(rs.getTimestamp("publish_at")),
                rs.getString("read_status")
            ),
            readStatusFilter,
            readStatusFilter,
            readStatusFilter,
            keywordLike,
            keywordLike,
            keywordLike,
            keywordLike,
            safePageSize,
            (safePageNo - 1) * safePageSize
        );

        return new PagedData<>(records, safePageNo, safePageSize, total == null ? 0 : total);
    }

    public NoticeDto.NoticeView myNoticeDetail(CurrentUser user, Long noticeId) {
        List<NoticeDto.NoticeView> rows = jdbcTemplate.query(
            """
                select n.id, n.title, n.content, n.tag_labels, n.channel_labels, n.audience,
                       n.delivered_count, n.read_count, n.publish_at,
                       case when r.notice_id is null then 'unread' else 'read' end as read_status
                  from biz_notice n
                  left join biz_notice_read r
                    on r.notice_id = n.id and r.user_id = ?
                 where n.id = ?
                   and n.is_deleted = 0
                   and n.status = 'published'
                """,
            (rs, rowNum) -> new NoticeDto.NoticeView(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("content"),
                splitLabels(rs.getString("tag_labels")),
                splitLabels(rs.getString("channel_labels")),
                rs.getString("audience"),
                rs.getInt("delivered_count"),
                rs.getInt("read_count"),
                format(rs.getTimestamp("publish_at")),
                rs.getString("read_status")
            ),
            user.getId(),
            noticeId
        );
        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "notice not found");
        }
        return rows.get(0);
    }

    public NoticeDto.NoticeView createNotice(CurrentUser user, NoticeDto.CreateNoticeRequest request) {
        ensureManager(user);
        Long nextId = nextId("biz_notice");
        LocalDateTime now = LocalDateTime.now();

        String channels = joinLabels(request.getChannelLabels());
        List<String> safeTags = request.getTags() == null ? List.of() : request.getTags();
        String tags = joinLabels(safeTags);

        jdbcTemplate.update(
            """
                insert into biz_notice (
                    id, title, content, audience, channel_labels, tag_labels,
                    delivered_count, read_count, status, publish_at, created_by,
                    created_at, updated_at, is_deleted
                ) values (?, ?, ?, ?, ?, ?, 0, 0, 'published', ?, ?, ?, ?, 0)
                """,
            nextId,
            request.getTitle(),
            request.getContent(),
            request.getAudience(),
            channels,
            tags,
            Timestamp.valueOf(now),
            user.getId(),
            Timestamp.valueOf(now),
            Timestamp.valueOf(now)
        );

        return new NoticeDto.NoticeView(
            nextId,
            request.getTitle(),
            request.getContent(),
            safeTags,
            request.getChannelLabels(),
            request.getAudience(),
            0,
            0,
            DATETIME_FORMATTER.format(now),
            "unread"
        );
    }

    public void markRead(CurrentUser user, Long noticeId) {
        int exists = jdbcTemplate.queryForObject(
            "select count(*) from biz_notice where id = ? and is_deleted = 0",
            Integer.class,
            noticeId
        );
        if (exists == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "notice not found");
        }

        Integer alreadyRead = jdbcTemplate.queryForObject(
            "select count(*) from biz_notice_read where notice_id = ? and user_id = ?",
            Integer.class,
            noticeId,
            user.getId()
        );
        if (alreadyRead != null && alreadyRead > 0) {
            return;
        }

        jdbcTemplate.update(
            "insert into biz_notice_read (id, notice_id, user_id, read_at) values (?, ?, ?, ?)",
            nextId("biz_notice_read"),
            noticeId,
            user.getId(),
            Timestamp.valueOf(LocalDateTime.now())
        );
        jdbcTemplate.update(
            "update biz_notice set read_count = read_count + 1, updated_at = ? where id = ?",
            Timestamp.valueOf(LocalDateTime.now()),
            noticeId
        );
    }

    public void markAllRead(CurrentUser user) {
        List<Long> unreadIds = jdbcTemplate.query(
            """
                select n.id
                  from biz_notice n
                  left join biz_notice_read r
                    on r.notice_id = n.id and r.user_id = ?
                 where n.is_deleted = 0
                   and n.status = 'published'
                   and r.notice_id is null
                """,
            (rs, rowNum) -> rs.getLong("id"),
            user.getId()
        );

        for (Long noticeId : unreadIds) {
            markRead(user, noticeId);
        }
    }

    private void ensureManager(CurrentUser user) {
        boolean manager = user.getRoles().stream().anyMatch(role ->
            "teacher_admin".equals(role) || "college_leader".equals(role) || "system_admin".equals(role));
        if (!manager) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "manager role required");
        }
    }

    private Long nextId(String tableName) {
        Long value = jdbcTemplate.queryForObject("select coalesce(max(id), 0) + 1 from " + tableName, Long.class);
        return value == null ? 1L : value;
    }

    private String normalizeFilter(String value) {
        if (!StringUtils.hasText(value) || "all".equalsIgnoreCase(value)) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private List<String> splitLabels(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        String[] values = raw.split("[,，、]+");
        List<String> labels = new ArrayList<>();
        for (String value : values) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                labels.add(trimmed);
            }
        }
        return labels;
    }

    private String joinLabels(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        List<String> sanitized = values.stream()
            .map(String::trim)
            .filter(item -> !item.isEmpty())
            .toList();
        if (sanitized.isEmpty()) {
            return "";
        }
        return String.join(",", sanitized);
    }

    private String format(Timestamp timestamp) {
        return timestamp == null ? "-" : DATETIME_FORMATTER.format(timestamp.toLocalDateTime());
    }
}

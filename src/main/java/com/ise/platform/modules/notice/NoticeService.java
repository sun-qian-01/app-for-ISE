package com.ise.platform.modules.notice;

import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.modules.audit.AuditLogService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NoticeService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern GRADE_PATTERN = Pattern.compile("(20\\d{2})(?:级|届)?");

    private final JdbcTemplate jdbcTemplate;
    private final AuditLogService auditLogService;

    public NoticeService(JdbcTemplate jdbcTemplate, AuditLogService auditLogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditLogService = auditLogService;
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
        StudentTargetProfile profile = findStudentTargetProfile(user);

        List<NoticeDto.NoticeView> matched = jdbcTemplate.query(
            """
                select n.id, n.title, n.content, n.tag_labels, n.channel_labels, n.audience,
                       n.delivered_count, n.read_count, n.publish_at,
                       case when r.notice_id is null then 'unread' else 'read' end as read_status
                  from biz_notice n
                  left join biz_notice_read r
                    on r.notice_id = n.id and r.user_id = ?
                 where n.is_deleted = 0
                   and n.status = 'published'
                   and (? is null or lower(coalesce(n.tag_labels, '')) like ?)
                 order by n.publish_at desc, n.id desc
                """,
            (rs, rowNum) -> mapNoticeView(rs),
            user.getId(),
            tagFilter,
            tagFilter == null ? null : "%" + tagFilter + "%"
        ).stream()
            .filter(item -> matchesAudience(item.getAudience(), profile))
            .filter(item -> readStatusFilter == null || readStatusFilter.equals(item.getReadStatus()))
            .toList();

        int fromIndex = Math.min((safePageNo - 1) * safePageSize, matched.size());
        int toIndex = Math.min(fromIndex + safePageSize, matched.size());
        return new PagedData<>(matched.subList(fromIndex, toIndex), safePageNo, safePageSize, matched.size());
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
            (rs, rowNum) -> mapNoticeView(rs),
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
            (rs, rowNum) -> mapNoticeView(rs),
            user.getId(),
            noticeId
        );
        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "notice not found");
        }
        NoticeDto.NoticeView notice = rows.get(0);
        if (!matchesAudience(notice.getAudience(), findStudentTargetProfile(user))) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "notice not found");
        }
        return notice;
    }

    public NoticeDto.NoticeView createNotice(CurrentUser user, NoticeDto.CreateNoticeRequest request) {
        ensureManager(user);
        Long nextId = nextId("biz_notice");
        LocalDateTime now = LocalDateTime.now();

        String channels = joinLabels(request.getChannelLabels());
        List<String> safeTags = request.getTags() == null ? List.of() : request.getTags();
        String tags = joinLabels(safeTags);

        int recipientCount = countTargetStudents(request.getAudience());

        jdbcTemplate.update(
            """
                insert into biz_notice (
                    id, title, content, audience, channel_labels, tag_labels,
                    delivered_count, read_count, status, publish_at, created_by,
                    created_at, updated_at, is_deleted
                ) values (?, ?, ?, ?, ?, ?, ?, 0, 'published', ?, ?, ?, ?, 0)
                """,
            nextId,
            request.getTitle(),
            request.getContent(),
            request.getAudience(),
            channels,
            tags,
            recipientCount,
            Timestamp.valueOf(now),
            user.getId(),
            Timestamp.valueOf(now),
            Timestamp.valueOf(now)
        );

        NoticeDto.NoticeView created = new NoticeDto.NoticeView(
            nextId,
            request.getTitle(),
            request.getContent(),
            safeTags,
            request.getChannelLabels(),
            request.getAudience(),
            recipientCount,
            0,
            DATETIME_FORMATTER.format(now),
            "unread"
        );
        auditLogService.record(user, "通知", "发布定向通知：" + request.getTitle(), "成功");
        return created;
    }

    public void markRead(CurrentUser user, Long noticeId) {
        List<String> audiences = jdbcTemplate.query(
            "select audience from biz_notice where id = ? and is_deleted = 0 and status = 'published'",
            (rs, rowNum) -> rs.getString("audience"),
            noticeId
        );
        if (audiences.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "notice not found");
        }
        if (!matchesAudience(audiences.get(0), findStudentTargetProfile(user))) {
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
        StudentTargetProfile profile = findStudentTargetProfile(user);
        List<Long> unreadIds = jdbcTemplate.query(
            """
                select n.id, n.audience
                  from biz_notice n
                  left join biz_notice_read r
                    on r.notice_id = n.id and r.user_id = ?
                 where n.is_deleted = 0
                   and n.status = 'published'
                   and r.notice_id is null
                """,
            (rs, rowNum) -> new NoticeTarget(rs.getLong("id"), rs.getString("audience")),
            user.getId()
        ).stream()
            .filter(item -> matchesAudience(item.audience(), profile))
            .map(NoticeTarget::id)
            .toList();

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

    private NoticeDto.NoticeView mapNoticeView(ResultSet rs) throws SQLException {
        String audience = rs.getString("audience");
        int deliveredCount = rs.getInt("delivered_count");
        if (deliveredCount <= 0) {
            deliveredCount = countTargetStudents(audience);
        }
        return new NoticeDto.NoticeView(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("content"),
            splitLabels(rs.getString("tag_labels")),
            splitLabels(rs.getString("channel_labels")),
            audience,
            deliveredCount,
            rs.getInt("read_count"),
            format(rs.getTimestamp("publish_at")),
            rs.getString("read_status")
        );
    }

    private int countTargetStudents(String audience) {
        TargetCriteria criteria = parseTargetCriteria(audience);
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "select count(*) from stu_student where is_deleted = 0 and status <> 'graduated'"
        );

        if (criteria.isEmpty()) {
            Integer total = jdbcTemplate.queryForObject(sql.toString(), Integer.class);
            return total == null ? 0 : total;
        }

        sql.append(" and (");
        if (!criteria.grades().isEmpty()) {
            sql.append("grade in (");
            sql.append("?,".repeat(criteria.grades().size()));
            sql.setLength(sql.length() - 1);
            sql.append(")");
            args.addAll(criteria.grades());
        }
        if (!criteria.statuses().isEmpty()) {
            if (!args.isEmpty()) {
                sql.append(" or ");
            }
            sql.append("status in (");
            sql.append("?,".repeat(criteria.statuses().size()));
            sql.setLength(sql.length() - 1);
            sql.append(")");
            args.addAll(criteria.statuses());
        }
        sql.append(")");

        Integer matched = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        return matched == null ? 0 : matched;
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

    private record TargetCriteria(Set<String> grades, Set<String> statuses) {
        boolean isEmpty() {
            return grades.isEmpty() && statuses.isEmpty();
        }
    }

    private record StudentTargetProfile(String grade, String status) {
    }

    private record NoticeTarget(Long id, String audience) {
    }
}

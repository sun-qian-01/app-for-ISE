package com.ise.platform.modules.audit;

import com.ise.platform.common.security.CurrentUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AuditLogService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;

    public AuditLogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AuditLogDto.AuditLogView> listLogs() {
        return jdbcTemplate.query(
            """
                select id, actor_name, module_name, action_text, result_text, created_at
                  from sys_audit_log
                 order by created_at desc, id desc
                """,
            (rs, rowNum) -> new AuditLogDto.AuditLogView(
                rs.getLong("id"),
                rs.getString("actor_name"),
                rs.getString("module_name"),
                rs.getString("action_text"),
                DATETIME_FORMATTER.format(rs.getTimestamp("created_at").toLocalDateTime()),
                rs.getString("result_text")
            )
        );
    }

    public void record(CurrentUser user, String module, String action, String result) {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
            """
                insert into sys_audit_log (
                    id, actor_user_id, actor_name, module_name, action_text, result_text, created_at
                ) values (?, ?, ?, ?, ?, ?, ?)
                """,
            nextId(),
            user.getId(),
            actorName(user),
            module,
            action,
            result,
            Timestamp.valueOf(now)
        );
    }

    private Long nextId() {
        Long value = jdbcTemplate.queryForObject("select coalesce(max(id), 0) + 1 from sys_audit_log", Long.class);
        return value == null ? 1L : value;
    }

    private String actorName(CurrentUser user) {
        if (user.getRealName() == null || user.getRealName().isBlank()) {
            return user.getUsername();
        }
        return user.getRealName();
    }
}

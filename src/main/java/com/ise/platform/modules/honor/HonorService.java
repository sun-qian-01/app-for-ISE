package com.ise.platform.modules.honor;

import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.common.security.DataScope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class HonorService {

    private final JdbcTemplate jdbcTemplate;

    public HonorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<HonorDto.HonorView> myHonors(CurrentUser user) {
        if (isManager(user)) {
            return listAllHonors();
        }
        if (isCadre(user)) {
            String className = resolveClassName(user);
            String classLike = StringUtils.hasText(className) ? "%" + className.trim().toLowerCase(Locale.ROOT) + "%" : null;
            return jdbcTemplate.query(
                """
                    select id, title, owner_name, honor_year, category_label, story, honor_scope
                      from biz_honor
                     where is_deleted = 0
                       and (
                          (honor_scope = 'personal' and owner_user_id = ?)
                          or (? is not null and honor_scope = 'collective' and lower(owner_name) like ?)
                       )
                     order by honor_year desc, id desc
                    """,
                this::mapHonor,
                user.getId(),
                classLike,
                classLike
            );
        }

        return jdbcTemplate.query(
            """
                select id, title, owner_name, honor_year, category_label, story, honor_scope
                 from biz_honor
                where is_deleted = 0
                  and honor_scope = 'personal'
                  and owner_user_id = ?
                 order by honor_year desc, id desc
                """,
            this::mapHonor,
            user.getId()
        );
    }

    public List<HonorDto.HonorView> allHonors(CurrentUser user) {
        if (!isManager(user)) {
            return myHonors(user);
        }
        return listAllHonors();
    }

    private List<HonorDto.HonorView> listAllHonors() {
        return jdbcTemplate.query(
            """
                select id, title, owner_name, honor_year, category_label, story, honor_scope
                  from biz_honor
                 where is_deleted = 0
                 order by honor_year desc, id desc
                """,
            this::mapHonor
        );
    }

    private HonorDto.HonorView mapHonor(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new HonorDto.HonorView(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("owner_name"),
            rs.getString("honor_year"),
            rs.getString("category_label"),
            rs.getString("story"),
            rs.getString("honor_scope")
        );
    }

    private String resolveClassName(CurrentUser user) {
        for (DataScope scope : user.getDataScopes()) {
            if ("class".equals(scope.getScopeType()) && StringUtils.hasText(scope.getScopeValue())) {
                return scope.getScopeValue();
            }
        }
        if (user.getStudentId() == null) {
            return null;
        }
        List<String> rows = jdbcTemplate.query(
            "select class_name from stu_student where id = ? and is_deleted = 0",
            (rs, rowNum) -> rs.getString("class_name"),
            user.getStudentId()
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    private boolean isCadre(CurrentUser user) {
        return user.getRoles().contains("class_cadre");
    }

    private boolean isManager(CurrentUser user) {
        return user.getRoles().stream().anyMatch(role ->
            "teacher_admin".equals(role) || "college_leader".equals(role) || "system_admin".equals(role));
    }
}

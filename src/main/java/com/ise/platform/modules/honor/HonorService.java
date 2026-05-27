package com.ise.platform.modules.honor;

import com.ise.platform.common.security.CurrentUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HonorService {

    private final JdbcTemplate jdbcTemplate;

    public HonorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<HonorDto.HonorView> myHonors(CurrentUser user) {
        boolean managerLike = isManager(user) || isCadre(user);
        String sql = """
                select id, title, owner_name, honor_year, category_label, story, honor_scope
                 from biz_honor
                where is_deleted = 0
                  and (
                     ? = true
                     or (honor_scope = 'personal' and owner_user_id = ?)
                   )
                 order by honor_year desc, id desc
                """;
        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new HonorDto.HonorView(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("owner_name"),
                rs.getString("honor_year"),
                rs.getString("category_label"),
                rs.getString("story"),
                rs.getString("honor_scope")
            ),
            managerLike,
            user.getId()
        );
    }

    public List<HonorDto.HonorView> allHonors(CurrentUser user) {
        if (!(isManager(user) || isCadre(user))) {
            return myHonors(user);
        }
        return jdbcTemplate.query(
            """
                select id, title, owner_name, honor_year, category_label, story, honor_scope
                  from biz_honor
                 where is_deleted = 0
                 order by honor_year desc, id desc
                """,
            (rs, rowNum) -> new HonorDto.HonorView(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("owner_name"),
                rs.getString("honor_year"),
                rs.getString("category_label"),
                rs.getString("story"),
                rs.getString("honor_scope")
            )
        );
    }

    private boolean isCadre(CurrentUser user) {
        return user.getRoles().contains("class_cadre");
    }

    private boolean isManager(CurrentUser user) {
        return user.getRoles().stream().anyMatch(role ->
            "teacher_admin".equals(role) || "college_leader".equals(role) || "system_admin".equals(role));
    }
}

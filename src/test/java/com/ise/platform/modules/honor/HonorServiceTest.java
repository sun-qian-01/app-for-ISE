package com.ise.platform.modules.honor;

import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.common.security.DataScope;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HonorServiceTest {

    private HonorService honorService;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:honor-service-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        createSchema(jdbcTemplate);
        resetData(jdbcTemplate);
        honorService = new HonorService(jdbcTemplate);
    }

    @Test
    void studentShouldOnlySeeOwnPersonalHonors() {
        List<HonorDto.HonorView> honors = honorService.myHonors(studentUser());

        assertThat(honors).extracting(HonorDto.HonorView::getOwner)
            .containsExactlyInAnyOrder("赵晨曦");
    }

    @Test
    void classCadreShouldSeeOwnAndClassCollectiveHonorsOnly() {
        List<HonorDto.HonorView> honors = honorService.myHonors(classCadreUser());

        assertThat(honors).extracting(HonorDto.HonorView::getOwner)
            .containsExactlyInAnyOrder("陈一诺", "软件工程2班", "软件工程2班团支部");
        assertThat(honors).extracting(HonorDto.HonorView::getOwner)
            .doesNotContain("赵晨曦", "梅园 3 栋 412", "本科生第一党支部");
    }

    @Test
    void classCadreAllHonorsShouldStillBeScoped() {
        List<HonorDto.HonorView> honors = honorService.allHonors(classCadreUser());

        assertThat(honors).extracting(HonorDto.HonorView::getOwner)
            .containsExactlyInAnyOrder("陈一诺", "软件工程2班", "软件工程2班团支部");
    }

    @Test
    void managerShouldSeeAllHonors() {
        List<HonorDto.HonorView> honors = honorService.allHonors(managerUser());

        assertThat(honors).extracting(HonorDto.HonorView::getOwner)
            .contains("赵晨曦", "陈一诺", "软件工程2班", "软件工程2班团支部", "梅园 3 栋 412", "本科生第一党支部");
    }

    private void createSchema(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
            """
                create table if not exists stu_student (
                    id bigint primary key,
                    class_name varchar(64) not null,
                    is_deleted smallint not null default 0
                )
                """
        );
        jdbcTemplate.execute(
            """
                create table if not exists biz_honor (
                    id bigint primary key,
                    title varchar(255) not null,
                    owner_name varchar(128) not null,
                    owner_user_id bigint,
                    honor_scope varchar(32) not null,
                    honor_year varchar(8) not null,
                    category_label varchar(64) not null,
                    story varchar(500),
                    is_deleted smallint not null default 0
                )
                """
        );
    }

    private void resetData(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("delete from biz_honor");
        jdbcTemplate.update("delete from stu_student");
        jdbcTemplate.update("insert into stu_student (id, class_name, is_deleted) values (?, ?, ?)", 1L, "软件工程2班", 0);
        jdbcTemplate.update("insert into stu_student (id, class_name, is_deleted) values (?, ?, ?)", 2L, "软件工程2班", 0);
        jdbcTemplate.update(
            """
                insert into biz_honor (id, title, owner_name, owner_user_id, honor_scope, honor_year, category_label, story, is_deleted)
                values
                (1, '国家奖学金获得者', '赵晨曦', 1, 'personal', '2025', '国家奖学金', '个人荣誉', 0),
                (2, '优秀共青团干部', '陈一诺', 2, 'personal', '2026', '党团荣誉', '个人荣誉', 0),
                (3, '先进班集体', '软件工程2班', null, 'collective', '2025', '先进集体', '班级荣誉', 0),
                (4, '十佳团支部', '软件工程2班团支部', null, 'collective', '2026', '党团荣誉', '班级荣誉', 0),
                (5, '学院就业先锋宿舍', '梅园 3 栋 412', null, 'collective', '2026', '就业与成长', '非本班集体荣誉', 0),
                (6, '学院党建工作示范支部', '本科生第一党支部', null, 'party', '2025', '党团荣誉', '支部荣誉', 0)
                """
        );
    }

    private CurrentUser studentUser() {
        return new CurrentUser(
            1L,
            "20220001",
            "赵晨曦",
            "student",
            List.of("student"),
            List.of("student:honor:view"),
            List.of(new DataScope("self", "1")),
            1L
        );
    }

    private CurrentUser classCadreUser() {
        return new CurrentUser(
            2L,
            "20220018",
            "陈一诺",
            "student",
            List.of("class_cadre"),
            List.of("student:honor:view"),
            List.of(new DataScope("class", "软件工程2班")),
            2L
        );
    }

    private CurrentUser managerUser() {
        return new CurrentUser(
            8L,
            "teacher001",
            "李老师",
            "teacher",
            List.of("teacher_admin"),
            List.of("admin:honor:view"),
            List.of(new DataScope("class", "软件工程2班")),
            null
        );
    }
}

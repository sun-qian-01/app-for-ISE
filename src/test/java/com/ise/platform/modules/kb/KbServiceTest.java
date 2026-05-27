package com.ise.platform.modules.kb;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.assertj.core.api.Assertions.assertThat;

class KbServiceTest {

    private final KbService kbService;

    KbServiceTest() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:kb-service-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute("""
            create table if not exists kb_article (
                id bigint primary key,
                title varchar(255),
                summary varchar(500),
                category_label varchar(64),
                publish_status varchar(32),
                version_no varchar(32),
                standard_answer text,
                source_file_name varchar(255),
                source_file_id bigint,
                keywords_text varchar(500),
                view_count int,
                created_at timestamp,
                updated_at timestamp,
                is_deleted smallint
            )
            """);
        jdbcTemplate.update("delete from kb_article");
        jdbcTemplate.update("""
            insert into kb_article (id, title, summary, category_label, publish_status, version_no, standard_answer, source_file_name, source_file_id, keywords_text, view_count, created_at, updated_at, is_deleted)
            values
            (1, '国家奖学金评定流程说明', '摘要', '奖助', 'published', 'v1', '标准答案', 'source.pdf', null, '奖学金,评定', 1, current_timestamp(), current_timestamp(), 0)
            """);
        this.kbService = new KbService(jdbcTemplate);
    }

    @Test
    void qaShouldFallbackWhenNoReliableSource() {
        KbDto.QaRequest request = new KbDto.QaRequest();
        request.setQuestion("一个完全不相关的问题");
        KbDto.QaResponse response = kbService.qa(request);
        assertThat(response.getAnswer()).isEqualTo("未检索到可靠依据");
        assertThat(response.getSources()).isEmpty();
        assertThat(response.getConfidence()).isEqualTo(0.0d);
    }
}

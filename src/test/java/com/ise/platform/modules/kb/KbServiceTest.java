package com.ise.platform.modules.kb;

import com.ise.platform.modules.kb.rag.LlmResponsesClient;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KbServiceTest {

    private final KbService kbService;
    private final LlmResponsesClient llmResponsesClient;

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
            (1, '国家奖学金评定流程说明', '摘要', '奖助', 'published', 'v1', '标准答案', 'source.pdf', null, '奖学金,国家奖学金,评定,材料', 1, current_timestamp(), current_timestamp(), 0),
            (3, '党员发展阶段材料清单', '汇总积极分子、发展对象、预备党员各阶段所需材料。', '党团', 'published', 'v1', '党员发展阶段需按节点提交思想汇报、谈话记录、培训记录和志愿服务证明等材料。', 'party-dev.xlsx', null, '党员,思想汇报,发展对象,预备党员', 1, current_timestamp(), current_timestamp(), 0),
            (4, '在读证明与成绩证明办理说明', '说明在读证明、成绩证明申请场景和附件要求。', '证明', 'published', 'v1', '在读证明和成绩证明均需填写用途并确认收件单位，审批通过后可下载电子版证明文件。', 'proof.pdf', null, '在读证明,成绩证明,申请,附件', 1, current_timestamp(), current_timestamp(), 0),
            (9, '党员证明开具说明', '介绍党员证明模板用途、填写要点与申请流程。', '党团', 'published', 'v1', '党员证明需按学院模板填写并经支部审核后提交，建议同步准备身份信息与用途说明。', 'party-proof.docx', null, '党员证明,党团,模板,开具', 1, current_timestamp(), current_timestamp(), 0),
            (10, '团员证明开具说明', '介绍团员证明模板填写规范与常见使用场景。', '党团', 'published', 'v1', '团员证明建议填写完整用途、接收单位与日期，提交前确认模板字段无遗漏。', 'league-proof.docx', null, '团员证明,党团,模板,开具', 1, current_timestamp(), current_timestamp(), 0)
            """);
        this.llmResponsesClient = mock(LlmResponsesClient.class);
        this.kbService = new KbService(jdbcTemplate, llmResponsesClient);
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

    @Test
    void qaShouldReplyForAssistantIdentityQuestion() {
        KbDto.QaRequest request = new KbDto.QaRequest();
        request.setQuestion("你是谁");
        KbDto.QaResponse response = kbService.qa(request);
        assertThat(response.getAnswer()).contains("学院知识库助手");
        assertThat(response.getConfidence()).isGreaterThan(0.9d);
    }

    @Test
    void qaShouldUseRecentHistoryForFollowUpQuestion() {
        KbDto.QaHistoryMessage previousQuestion = new KbDto.QaHistoryMessage();
        previousQuestion.setRole("user");
        previousQuestion.setContent("国家奖学金评定流程是什么？");

        KbDto.QaRequest request = new KbDto.QaRequest();
        request.setQuestion("那截止时间呢？");
        request.setHistory(List.of(previousQuestion));

        KbDto.QaResponse response = kbService.qa(request);

        assertThat(response.getAnswer()).isEqualTo("标准答案");
        assertThat(response.getSources()).extracting(KbDto.QaSource::getArticleId).containsExactly(1L);
        assertThat(response.getConfidence()).isGreaterThan(0.5d);
    }

    @Test
    void qaShouldPassKeywordEvidenceToAiWhenArticleMatches() {
        KbDto.QaRequest request = new KbDto.QaRequest();
        request.setQuestion("国家奖学金需要哪些材料？");
        request.setHistory(List.of());
        when(llmResponsesClient.answer(eq("国家奖学金需要哪些材料？"), anyList(), anyList(), eq(false)))
            .thenReturn("AI 整理后的国家奖学金材料说明。");

        KbDto.QaResponse response = kbService.qa(request);

        assertThat(response.getAnswer()).isEqualTo("AI 整理后的国家奖学金材料说明。");
        assertThat(response.getSources()).extracting(KbDto.QaSource::getArticleId).containsExactly(1L);
        verify(llmResponsesClient).answer(eq("国家奖学金需要哪些材料？"), anyList(), anyList(), eq(false));
    }

    @Test
    void qaShouldNotUseHistoryForIndependentNewQuestion() {
        KbDto.QaHistoryMessage previousQuestion = new KbDto.QaHistoryMessage();
        previousQuestion.setRole("user");
        previousQuestion.setContent("国家奖学金评定流程是什么？");

        KbDto.QaRequest request = new KbDto.QaRequest();
        request.setQuestion("团员");
        request.setHistory(List.of(previousQuestion));

        KbDto.QaResponse response = kbService.qa(request);

        assertThat(response.getAnswer()).isEqualTo("团员证明建议填写完整用途、接收单位与日期，提交前确认模板字段无遗漏。");
        assertThat(response.getSources()).extracting(KbDto.QaSource::getArticleId).containsExactly(10L);
    }

    @Test
    void qaShouldReplyForModelQuestionWithoutUsingHistory() {
        KbDto.QaHistoryMessage previousQuestion = new KbDto.QaHistoryMessage();
        previousQuestion.setRole("user");
        previousQuestion.setContent("国家奖学金评定流程是什么？");

        KbDto.QaRequest request = new KbDto.QaRequest();
        request.setQuestion("你是什么模型");
        request.setHistory(List.of(previousQuestion));

        KbDto.QaResponse response = kbService.qa(request);

        assertThat(response.getAnswer()).contains("学院知识库助手");
        assertThat(response.getSources()).isEmpty();
        assertThat(response.getConfidence()).isGreaterThan(0.9d);
    }

    @Test
    void qaShouldKeepScholarshipTopicForGenericGradeFollowUp() {
        KbDto.QaHistoryMessage previousQuestion = new KbDto.QaHistoryMessage();
        previousQuestion.setRole("user");
        previousQuestion.setContent("国家奖学金怎么拿？");

        KbDto.QaRequest request = new KbDto.QaRequest();
        request.setQuestion("成绩需要达到什么水平？");
        request.setHistory(List.of(previousQuestion));
        when(llmResponsesClient.answer(eq("成绩需要达到什么水平？"), anyList(), anyList(), eq(false)))
            .thenReturn("标准答案");

        KbDto.QaResponse response = kbService.qa(request);

        assertThat(response.getAnswer()).contains("没有直接说明").contains("成绩");
        assertThat(response.getSources()).extracting(KbDto.QaSource::getArticleId).containsExactly(1L);
        assertThat(response.getConfidence()).isLessThan(0.75d);
    }

    @Test
    void qaShouldNotShortCircuitMixedPolicyAndIdentityQuestion() {
        KbDto.QaHistoryMessage previousQuestion = new KbDto.QaHistoryMessage();
        previousQuestion.setRole("user");
        previousQuestion.setContent("国家奖学金怎么拿？");

        KbDto.QaRequest request = new KbDto.QaRequest();
        request.setQuestion("获奖需要用什么形式提交？你是谁？");
        request.setHistory(List.of(previousQuestion));
        when(llmResponsesClient.answer(eq("获奖需要用什么形式提交？你是谁？"), anyList(), anyList(), eq(false)))
            .thenReturn("获奖通常按知识条目提交佐证材料；我是学院知识库助手。");

        KbDto.QaResponse response = kbService.qa(request);

        assertThat(response.getAnswer()).contains("获奖").contains("学院知识库助手");
        assertThat(response.getSources()).extracting(KbDto.QaSource::getArticleId).containsExactly(1L);
        verify(llmResponsesClient).answer(eq("获奖需要用什么形式提交？你是谁？"), anyList(), anyList(), eq(false));
    }

    @Test
    void qaShouldPreferPartyProofOverPartyDevelopmentForIdentityCertificate() {
        KbDto.QaRequest request = new KbDto.QaRequest();
        request.setQuestion("党员身份证明如何开具？");
        request.setHistory(List.of());

        KbDto.QaResponse response = kbService.qa(request);

        assertThat(response.getAnswer()).isEqualTo("党员证明需按学院模板填写并经支部审核后提交，建议同步准备身份信息与用途说明。");
        assertThat(response.getSources()).extracting(KbDto.QaSource::getArticleId).containsExactly(9L);
    }
}

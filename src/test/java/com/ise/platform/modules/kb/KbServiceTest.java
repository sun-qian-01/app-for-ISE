package com.ise.platform.modules.kb;

import com.ise.platform.modules.kb.rag.QaAnswerClient;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KbServiceTest {

    private final KbService kbService;
    private final QaAnswerClient qaAnswerClient;

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
            (6, '2024级大类培养方案（含辅修）说明', '覆盖培养目标、课程结构、学分要求与辅修路径。', '培养方案', 'published', 'v1', '2024级培养方案包含主修与辅修课程规划，建议结合学院教务通知与个人培养计划同步执行。', '2024-plan.pdf', null, '培养方案,2024级,辅修,学分', 1, current_timestamp(), current_timestamp(), 0),
            (7, '2025级大类培养方案解读', '面向 2025 级学生，梳理课程地图与选课建议。', '培养方案', 'published', 'v1', '2025级培养方案强调基础课程与方向模块衔接，建议在导师指导下规划选课路径。', '2025-plan.pdf', null, '培养方案,2025级,课程地图,选课', 1, current_timestamp(), current_timestamp(), 0),
            (8, '信息学院 2025 年综合类政策摘要', '汇总学院 2025 年综合类通知重点与办理窗口。', '学院政策', 'published', 'v1', '综合类政策涉及培养、事务办理与相关时间节点，请以学院正式通知为最终依据。', 'policy-2025.pdf', null, '学院政策,综合类,办理窗口,通知', 1, current_timestamp(), current_timestamp(), 0),
            (9, '党员证明开具说明', '介绍党员证明模板用途、填写要点与申请流程。', '党团', 'published', 'v1', '党员证明需按学院模板填写并经支部审核后提交，建议同步准备身份信息与用途说明。', 'party-proof.docx', null, '党员证明,党团,模板,开具', 1, current_timestamp(), current_timestamp(), 0),
            (10, '团员证明开具说明', '介绍团员证明模板填写规范与常见使用场景。', '党团', 'published', 'v1', '团员证明建议填写完整用途、接收单位与日期，提交前确认模板字段无遗漏。', 'league-proof.docx', null, '团员证明,党团,模板,开具', 1, current_timestamp(), current_timestamp(), 0)
            """);
        this.qaAnswerClient = mock(QaAnswerClient.class);
        this.kbService = new KbService(jdbcTemplate, qaAnswerClient);
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
        request.setHistory(List.of());

        KbDto.QaResponse response = kbService.qa(request);

        assertThat(response.getAnswer()).isEqualTo("我是学院知识库助手，可以基于平台已发布条目帮你检索政策、模板和办理说明。");
        assertThat(response.getSources()).isEmpty();
        assertThat(response.getConfidence()).isEqualTo(0.98d);
        verify(qaAnswerClient, never()).answer(eq("你是谁"), anyList(), anyList(), eq(true));
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
        when(qaAnswerClient.answer(eq("国家奖学金需要哪些材料？"), anyList(), anyList(), eq(false)))
            .thenReturn("AI 整理后的国家奖学金材料说明。");

        KbDto.QaResponse response = kbService.qa(request);

        assertThat(response.getAnswer()).isEqualTo("AI 整理后的国家奖学金材料说明。");
        assertThat(response.getSources()).extracting(KbDto.QaSource::getArticleId).containsExactly(1L);
        verify(qaAnswerClient).answer(eq("国家奖学金需要哪些材料？"), anyList(), anyList(), eq(false));
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

        assertThat(response.getAnswer()).isEqualTo("我是学院知识库助手，当前会调用系统配置的 AI 服务来整理回答。");
        assertThat(response.getAnswer()).doesNotContain("Codex").doesNotContain("gpt-5.4-mini");
        assertThat(response.getSources()).isEmpty();
        assertThat(response.getConfidence()).isEqualTo(0.98d);
        verify(qaAnswerClient, never()).answer(eq("你是什么模型"), anyList(), anyList(), eq(true));
    }

    @Test
    void qaShouldKeepScholarshipTopicForGenericGradeFollowUp() {
        KbDto.QaHistoryMessage previousQuestion = new KbDto.QaHistoryMessage();
        previousQuestion.setRole("user");
        previousQuestion.setContent("国家奖学金怎么拿？");

        KbDto.QaRequest request = new KbDto.QaRequest();
        request.setQuestion("成绩需要达到什么水平？");
        request.setHistory(List.of(previousQuestion));
        when(qaAnswerClient.answer(eq("成绩需要达到什么水平？"), anyList(), anyList(), eq(false)))
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
        when(qaAnswerClient.answer(eq("获奖需要用什么形式提交？你是谁？"), anyList(), anyList(), eq(false)))
            .thenReturn("获奖通常按知识条目提交佐证材料；我是学院知识库助手。");

        KbDto.QaResponse response = kbService.qa(request);

        assertThat(response.getAnswer()).contains("获奖").contains("学院知识库助手");
        assertThat(response.getSources()).extracting(KbDto.QaSource::getArticleId).containsExactly(1L);
        verify(qaAnswerClient).answer(eq("获奖需要用什么形式提交？你是谁？"), anyList(), anyList(), eq(false));
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

    @Test
    void qaShouldRetrieveProcessedMaterialArticleForProgramPlan() {
        KbDto.QaRequest request = new KbDto.QaRequest();
        request.setQuestion("2025级培养方案选课怎么规划？");
        request.setHistory(List.of());
        when(qaAnswerClient.answer(eq("2025级培养方案选课怎么规划？"), anyList(), anyList(), eq(false)))
            .thenReturn("应结合 2025 级培养方案中的基础课程与方向模块规划选课。");

        KbDto.QaResponse response = kbService.qa(request);

        assertThat(response.getAnswer()).contains("2025 级培养方案");
        assertThat(response.getSources()).extracting(KbDto.QaSource::getArticleId).containsExactly(7L);
        assertThat(response.getSources()).extracting(KbDto.QaSource::getFileName).containsExactly("2025-plan.pdf");
        verify(qaAnswerClient).answer(eq("2025级培养方案选课怎么规划？"), anyList(), anyList(), eq(false));
    }

    @Test
    void qaShouldRetrieveProcessedMaterialArticleForComprehensivePolicy() {
        KbDto.QaRequest request = new KbDto.QaRequest();
        request.setQuestion("信息学院2025年综合类政策有哪些办理窗口？");
        request.setHistory(List.of());
        when(qaAnswerClient.answer(eq("信息学院2025年综合类政策有哪些办理窗口？"), anyList(), anyList(), eq(false)))
            .thenReturn("综合类政策涉及培养、事务办理与相关时间节点，应以学院正式通知为准。");

        KbDto.QaResponse response = kbService.qa(request);

        assertThat(response.getAnswer()).contains("综合类政策");
        assertThat(response.getSources()).extracting(KbDto.QaSource::getArticleId).containsExactly(8L);
        verify(qaAnswerClient).answer(eq("信息学院2025年综合类政策有哪些办理窗口？"), anyList(), anyList(), eq(false));
    }
}

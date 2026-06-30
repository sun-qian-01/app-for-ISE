package com.ise.platform.modules.kb.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ise.platform.modules.kb.KbDto;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CodexQaClientTest {

    @Test
    void answerShouldRunConfiguredCodexExecAndExtractJsonMessage() {
        CodexQaProperties properties = enabledProperties();
        CapturingRunner runner = new CapturingRunner(new CodexProcessResult(
            0,
            """
                {"type":"session.started","message":"ignored"}
                {"type":"item.completed","item":{"id":"item_0","type":"agent_message","text":"Codex 整理后的回答。"}}
                """,
            "",
            false
        ));
        CodexQaClient client = new CodexQaClient(properties, new ObjectMapper(), runner);

        KbDto.QaHistoryMessage previous = new KbDto.QaHistoryMessage();
        previous.setRole("user");
        previous.setContent("国家奖学金怎么拿？");
        RagChunk evidence = new RagChunk(
            "chunk-1",
            1L,
            "国家奖学金说明",
            "source.pdf",
            9L,
            "奖助",
            "published",
            "2026-06-29",
            "需要提交申请表和佐证材料。",
            List.of(0.1d, 0.2d),
            0.83d
        );

        String answer = client.answer("需要哪些材料？", List.of(previous), List.of(evidence), false);

        assertThat(answer).isEqualTo("Codex 整理后的回答。");
        assertThat(runner.command).containsExactly(
            "codex", "exec",
            "--json",
            "--ephemeral",
            "--sandbox", "read-only",
            "-m", "gpt-5.4-mini",
            "-C", "/opt/app-for-ise",
            "-"
        );
        assertThat(runner.stdin).contains("问题：需要哪些材料？")
            .contains("历史对话")
            .contains("国家奖学金怎么拿？")
            .contains("证据")
            .contains("需要提交申请表和佐证材料。")
            .contains("知识库证据来自平台已处理的 material/ 原始材料")
            .contains("不要向用户暴露 Codex、模型名、降级链路、内部路径或工具调用细节")
            .contains("来源：source.pdf");
        assertThat(runner.timeout).isEqualTo(Duration.ofSeconds(45));
    }

    @Test
    void answerShouldReturnBlankWhenDisabled() {
        CodexQaProperties properties = enabledProperties();
        ReflectionTestUtils.setField(properties, "enabled", false);
        CapturingRunner runner = new CapturingRunner(new CodexProcessResult(0, "不会执行", "", false));
        CodexQaClient client = new CodexQaClient(properties, new ObjectMapper(), runner);

        String answer = client.answer("hello", List.of(), List.of(), true);

        assertThat(answer).isBlank();
        assertThat(runner.command).isEmpty();
    }

    private CodexQaProperties enabledProperties() {
        CodexQaProperties properties = new CodexQaProperties();
        ReflectionTestUtils.setField(properties, "enabled", true);
        ReflectionTestUtils.setField(properties, "command", "codex");
        ReflectionTestUtils.setField(properties, "model", "gpt-5.4-mini");
        ReflectionTestUtils.setField(properties, "workingDirectory", "/opt/app-for-ise");
        ReflectionTestUtils.setField(properties, "timeoutMs", 45_000);
        ReflectionTestUtils.setField(properties, "maxPromptChars", 12_000);
        return properties;
    }

    private static class CapturingRunner implements CodexProcessRunner {
        private final CodexProcessResult result;
        private final List<String> command = new ArrayList<>();
        private String stdin = "";
        private Duration timeout = Duration.ZERO;

        private CapturingRunner(CodexProcessResult result) {
            this.result = result;
        }

        @Override
        public CodexProcessResult run(List<String> command, String stdin, Duration timeout) {
            this.command.clear();
            this.command.addAll(command);
            this.stdin = stdin;
            this.timeout = timeout;
            return result;
        }
    }
}

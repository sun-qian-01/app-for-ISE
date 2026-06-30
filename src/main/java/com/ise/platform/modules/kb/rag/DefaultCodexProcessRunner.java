package com.ise.platform.modules.kb.rag;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class DefaultCodexProcessRunner implements CodexProcessRunner {

    @Override
    public CodexProcessResult run(List<String> command, String stdin, Duration timeout) {
        try {
            Process process = new ProcessBuilder(command).start();
            CompletableFuture<String> stdout = CompletableFuture.supplyAsync(() -> readStream(process, true));
            CompletableFuture<String> stderr = CompletableFuture.supplyAsync(() -> readStream(process, false));
            process.getOutputStream().write(stdin.getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().close();

            boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new CodexProcessResult(-1, readFuture(stdout), readFuture(stderr), true);
            }
            return new CodexProcessResult(process.exitValue(), readFuture(stdout), readFuture(stderr), false);
        } catch (IOException e) {
            return new CodexProcessResult(-1, "", e.getMessage(), false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new CodexProcessResult(-1, "", "codex process interrupted", false);
        }
    }

    private String readStream(Process process, boolean stdout) {
        try {
            byte[] bytes = stdout ? process.getInputStream().readAllBytes() : process.getErrorStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    private String readFuture(CompletableFuture<String> future) {
        try {
            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            return "";
        }
    }
}

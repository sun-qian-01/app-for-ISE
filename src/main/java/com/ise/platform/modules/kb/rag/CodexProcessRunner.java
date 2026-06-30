package com.ise.platform.modules.kb.rag;

import java.time.Duration;
import java.util.List;

public interface CodexProcessRunner {
    CodexProcessResult run(List<String> command, String stdin, Duration timeout);
}

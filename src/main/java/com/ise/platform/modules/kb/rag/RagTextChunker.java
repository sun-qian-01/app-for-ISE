package com.ise.platform.modules.kb.rag;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class RagTextChunker {

    private static final int DEFAULT_CHUNK_SIZE = 700;
    private static final int DEFAULT_OVERLAP = 120;

    public List<String> split(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        String normalized = text.replace("\r\n", "\n").replace('\r', '\n').trim();
        if (normalized.isEmpty()) {
            return List.of();
        }
        if (normalized.length() <= DEFAULT_CHUNK_SIZE) {
            return List.of(normalized);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(normalized.length(), start + DEFAULT_CHUNK_SIZE);
            if (end < normalized.length()) {
                int separator = findLastSeparator(normalized, start, end);
                if (separator > start + 80) {
                    end = separator;
                }
            }
            String chunk = normalized.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            if (end >= normalized.length()) {
                break;
            }
            start = Math.max(start + 1, end - DEFAULT_OVERLAP);
        }

        return chunks;
    }

    private int findLastSeparator(String value, int start, int end) {
        for (int i = end - 1; i > start; i--) {
            char c = value.charAt(i);
            if (c == '\n' || c == '。' || c == '！' || c == '？' || c == ';' || c == '；') {
                return i + 1;
            }
        }
        return -1;
    }
}

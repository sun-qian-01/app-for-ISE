package com.ise.platform.modules.kb.rag;

import java.util.List;

public record RagChunk(
    String chunkId,
    Long articleId,
    String title,
    String sourceFileName,
    Long sourceFileId,
    String categoryLabel,
    String publishStatus,
    String updatedAt,
    String text,
    List<Double> vector,
    double score
) {
}

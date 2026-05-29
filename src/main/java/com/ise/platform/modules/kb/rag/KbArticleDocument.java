package com.ise.platform.modules.kb.rag;

public record KbArticleDocument(
    Long articleId,
    String title,
    String summary,
    String categoryLabel,
    String publishStatus,
    String versionNo,
    String standardAnswer,
    String sourceFileName,
    Long sourceFileId,
    String keywords,
    String updatedAt
) {
}

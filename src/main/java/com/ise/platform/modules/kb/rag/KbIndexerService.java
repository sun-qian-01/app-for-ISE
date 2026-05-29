package com.ise.platform.modules.kb.rag;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class KbIndexerService {

    private final JdbcTemplate jdbcTemplate;
    private final RagTextChunker chunker;
    private final EmbeddingClient embeddingClient;
    private final QdrantClient qdrantClient;
    private final KbRagProperties properties;

    public KbIndexerService(JdbcTemplate jdbcTemplate,
                            RagTextChunker chunker,
                            EmbeddingClient embeddingClient,
                            QdrantClient qdrantClient,
                            KbRagProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.chunker = chunker;
        this.embeddingClient = embeddingClient;
        this.qdrantClient = qdrantClient;
        this.properties = properties;
    }

    public int reindexPublishedArticles() {
        List<KbArticleDocument> articles = jdbcTemplate.query(
            """
                select id, title, summary, category_label, publish_status, version_no, standard_answer,
                       source_file_name, source_file_id, keywords_text,
                       formatdatetime(updated_at, 'yyyy-MM-dd HH:mm:ss') as updated_at
                  from kb_article
                 where is_deleted = 0 and publish_status = 'published'
                 order by id asc
                """,
            (rs, rowNum) -> new KbArticleDocument(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("summary"),
                rs.getString("category_label"),
                rs.getString("publish_status"),
                rs.getString("version_no"),
                rs.getString("standard_answer"),
                rs.getString("source_file_name"),
                rs.getObject("source_file_id", Long.class),
                rs.getString("keywords_text"),
                rs.getString("updated_at")
            )
        );

        qdrantClient.ensureCollection();

        List<RagChunk> buffer = new ArrayList<>();
        int indexed = 0;
        for (KbArticleDocument article : articles) {
            List<String> chunks = chunker.split(buildIndexText(article));
            if (chunks.isEmpty()) {
                continue;
            }
            for (int i = 0; i < chunks.size(); i++) {
                String chunkText = chunks.get(i);
                List<Double> vector = embeddingClient.embed(chunkText);
                if (vector.isEmpty()) {
                    continue;
                }
                buffer.add(new RagChunk(
                    article.articleId() + "-" + i,
                    article.articleId(),
                    article.title(),
                    article.sourceFileName(),
                    article.sourceFileId(),
                    article.categoryLabel(),
                    article.publishStatus(),
                    article.updatedAt(),
                    chunkText,
                    vector,
                    0
                ));

                if (buffer.size() >= properties.getIndexBatchSize()) {
                    qdrantClient.upsert(buffer);
                    indexed += buffer.size();
                    buffer.clear();
                }
            }
        }

        if (!buffer.isEmpty()) {
            qdrantClient.upsert(buffer);
            indexed += buffer.size();
        }

        return indexed;
    }

    private String buildIndexText(KbArticleDocument article) {
        StringBuilder builder = new StringBuilder();
        append(builder, "标题", article.title());
        append(builder, "摘要", article.summary());
        append(builder, "关键词", article.keywords());
        append(builder, "正文", article.standardAnswer());
        return builder.toString();
    }

    private void append(StringBuilder builder, String label, String value) {
        if (StringUtils.hasText(value)) {
            builder.append(label).append("：").append(value.trim()).append("\n");
        }
    }
}

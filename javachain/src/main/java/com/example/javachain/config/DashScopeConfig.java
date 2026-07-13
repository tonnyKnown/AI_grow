
package com.example.javachain.config;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 阿里云 DashScope Embedding 配置（使用 langchain4j）
 */
@Configuration
@Slf4j
public class DashScopeConfig {

    @Value("${dashscope.api.key}")
    private String apiKey;

    @Value("${dashscope.api.embedding-model}")
    private String embeddingModel;

    /**
     * 阿里云 DashScope Embedding 模型
     * 用于文本向量化，支持 RAG 检索增强
     */
    @Bean
    public EmbeddingModel dashScopeEmbeddingModel() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("DashScope API key is not configured. Set DASHSCOPE_API_KEY in environment variables or project .env.");
            return new MissingApiKeyEmbeddingModel();
        }
        log.info("DashScope embedding model enabled. model={}", embeddingModel);
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .modelName(embeddingModel)
                .build();
    }

    private static class MissingApiKeyEmbeddingModel implements EmbeddingModel {

        private static final int DIMENSION = 1536;

        @Override
        public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
            List<Embedding> embeddings = textSegments.stream()
                    .map(segment -> Embedding.from(new float[DIMENSION]))
                    .toList();
            return Response.from(embeddings);
        }

        @Override
        public int dimension() {
            return DIMENSION;
        }
    }
}

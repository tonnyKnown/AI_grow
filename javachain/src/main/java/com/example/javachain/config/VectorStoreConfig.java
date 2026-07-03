package com.example.javachain.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 向量存储配置 - 使用内存向量库
 * 无需外部 ChromaDB 服务器，数据存储在内存中
 */
@Configuration
public class VectorStoreConfig {

    /**
     * 内存向量存储（主实现）
     * 使用 LangChain4j 的 InMemoryEmbeddingStore
     */
    @Bean
    @Primary
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }
}

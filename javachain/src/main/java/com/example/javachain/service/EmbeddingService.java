package com.example.javachain.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 嵌入服务 - 使用阿里云 DashScope 进行文本向量化
 * DashScope text-embedding-v1 输入长度限制: 1-2048 字符
 */
@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    
    /**
     * DashScope 嵌入模型最大输入长度
     */
    private static final int MAX_INPUT_LENGTH = 2048;
    
    /**
     * 文本分块大小（保留一定余量）
     */
    private static final int CHUNK_SIZE = 500;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * 对文本进行向量化
     * 自动处理超过最大长度的文本（截断处理）
     */
    public Embedding embed(String text) {
        if (text == null || text.isEmpty()) {
            text = " "; // 至少需要一个字符
        }
        
        // 如果文本超过最大长度，进行截断
        if (text.length() > MAX_INPUT_LENGTH) {
            text = text.substring(0, MAX_INPUT_LENGTH);
        }
        
        return embeddingModel.embed(text).content();
    }

    /**
     * 对文本进行向量化（分块平均策略）
     * 对于长文本，分成多个块分别向量化，然后取平均向量
     */
    public Embedding embedWithChunking(String text) {
        if (text == null || text.isEmpty()) {
            text = " ";
        }
        
        // 如果文本长度在限制范围内，直接向量化
//        if (text.length() <= MAX_INPUT_LENGTH) {
//            return embeddingModel.embed(text).content();
//        }
        
        // 将文本分块
        List<String> chunks = splitTextIntoChunks(text);
        
        // 对每个块向量化
        List<Embedding> embeddings = new ArrayList<>();
        for (String chunk : chunks) {
            if (!chunk.trim().isEmpty()) {
                embeddings.add(embeddingModel.embed(chunk).content());
            }
        }
        
        // 计算平均向量
        return averageEmbeddings(embeddings);
    }

    /**
     * 将文本按标记分割（Marker-based Chunking）
     * 支持的标记：
     * 1. Markdown 标题：#、##、###...
     * 2. 分隔线：---、===、***
     * 3. HTML 标签：<h1>-<h6>、<div>、<section>等
     */
    private List<String> splitTextIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {
            // 检查是否是标题标记（Markdown）
            if (line.matches("^#{1,6}\\s+.+")) {
                // 如果当前块不为空，先保存
                if (currentChunk.length() > 0) {
                    String chunk = currentChunk.toString().trim();
                    if (!chunk.isEmpty()) {
                        chunks.addAll(processChunk(chunk));
                    }
                    currentChunk = new StringBuilder();
                }
                // 添加标题作为新块的开始
                currentChunk.append(line).append("\n");
            }
            // 检查是否是分隔线标记
            else if (line.matches("^-{3,}$|^={3,}$|^\\*{3,}$")) {
                // 保存当前块
                if (currentChunk.length() > 0) {
                    String chunk = currentChunk.toString().trim();
                    if (!chunk.isEmpty()) {
                        chunks.addAll(processChunk(chunk));
                    }
                    currentChunk = new StringBuilder();
                }
            }
            // 检查是否是 HTML 标题标签
            else if (line.matches("^<h[1-6][^>]*>.*</h[1-6]>")) {
                if (currentChunk.length() > 0) {
                    String chunk = currentChunk.toString().trim();
                    if (!chunk.isEmpty()) {
                        chunks.addAll(processChunk(chunk));
                    }
                    currentChunk = new StringBuilder();
                }
                currentChunk.append(line).append("\n");
            }
            else {
                // 普通内容，添加到当前块
                currentChunk.append(line).append("\n");
            }
        }

        // 处理最后一个块
        if (currentChunk.length() > 0) {
            String chunk = currentChunk.toString().trim();
            if (!chunk.isEmpty()) {
                chunks.addAll(processChunk(chunk));
            }
        }

        return chunks;
    }

    /**
     * 处理单个块（检查长度并进行二次分割）
     */
    private List<String> processChunk(String chunk) {
        List<String> result = new ArrayList<>();

        if (chunk.length() <= MAX_INPUT_LENGTH) {
            result.add(chunk);
        } else {
            // 超长块需要二次分割（保持句子边界）
            result.addAll(splitLongChunk(chunk));
        }

        return result;
    }

    /**
     * 分割超长块（保持句子完整性）
     */
    private List<String> splitLongChunk(String chunk) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        int length = chunk.length();

        while (start < length) {
            int end = Math.min(start + CHUNK_SIZE, length);
            // 尽量在句子边界处分割
            if (end < length) {
                int lastPeriod = chunk.lastIndexOf('.', end);
                int lastNewline = chunk.lastIndexOf('\n', end);
                int splitPoint = Math.max(lastPeriod, lastNewline);
                if (splitPoint > start + CHUNK_SIZE / 2) {
                    end = splitPoint + 1;
                }
            }
            String subChunk = chunk.substring(start, end).trim();
            if (!subChunk.isEmpty()) {
                chunks.add(subChunk);
            }
            start = end;
        }

        return chunks;
    }
    /**
     * 计算多个向量的平均值
     */
    private Embedding averageEmbeddings(List<Embedding> embeddings) {
        if (embeddings.isEmpty()) {
            return embeddingModel.embed(" ").content();
        }
        
        int dimension = embeddings.get(0).vector().length;
        float[] avgVector = new float[dimension];
        
        for (Embedding embedding : embeddings) {
            float[] vector = embedding.vector();
            for (int i = 0; i < dimension; i++) {
                avgVector[i] += vector[i];
            }
        }
        
        for (int i = 0; i < dimension; i++) {
            avgVector[i] /= embeddings.size();
        }
        
        return Embedding.from(avgVector);
    }

    /**
     * 对多个文本进行向量化
     */
    public List<Embedding> embedAll(List<String> texts) {
        List<Embedding> embeddings = new ArrayList<>();
        for (String text : texts) {
            embeddings.add(embed(text));
        }
        return embeddings;
    }

    /**
     * 获取嵌入维度
     */
    public int getEmbeddingDimension() {
        // DashScope text-embedding-v1 返回 384 维向量
        return 384;
    }

    /**
     * 获取最大输入长度限制
     */
    public int getMaxInputLength() {
        return MAX_INPUT_LENGTH;
    }
}

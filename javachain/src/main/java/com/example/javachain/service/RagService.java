package com.example.javachain.service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * RAG 服务 - 企业级检索增强生成
 * 
 * 特性：
 * 1. 混合检索（向量 + BM25）
 * 2. 并行检索优化
 * 3. LLM 重排序
 * 4. 查询改写扩展
 * 5. 引用溯源
 */
@Slf4j
@Service
public class RagService {

    private static final int MAX_RESULTS = 15;
    private static final double MIN_SCORE = 0.4;
    private static final int FINAL_TOP_K = 5;
    private static final int PARALLEL_THREADS = 4;

    private final ChatLanguageModel chatLanguageModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingService embeddingService;
    private final KeywordExtractionService keywordExtractionService;
    private final BM25Retriever bm25Retriever;

    private final AtomicInteger documentCount = new AtomicInteger(0);
    private final Map<String, TextSegment> documentStore = new ConcurrentHashMap<>();
    private final Map<String, String> documentTitles = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(PARALLEL_THREADS);

    public RagService(ChatLanguageModel chatLanguageModel,
                      EmbeddingStore<TextSegment> embeddingStore,
                      EmbeddingService embeddingService,
                      KeywordExtractionService keywordExtractionService,
                      BM25Retriever bm25Retriever) {
        this.chatLanguageModel = chatLanguageModel;
        this.embeddingStore = embeddingStore;
        this.embeddingService = embeddingService;
        this.keywordExtractionService = keywordExtractionService;
        this.bm25Retriever = bm25Retriever;
    }

    /**
     * 查询问答 - 企业级 RAG 流程
     */
    public String query(String question) {
        log.info("收到问题: {}", question);

        // 1. 查询改写扩展
        List<String> expandedQueries = expandQuery(question);
        log.info("查询扩展: {} -> {}", question, expandedQueries);

        // 2. 并行混合检索
        List<RetrievalResult> candidates = parallelHybridRetrieval(expandedQueries);
        log.info("混合检索召回: {} 个候选", candidates.size());

        // 3. LLM 精排（当候选数足够时）
        List<RetrievalResult> rankedResults = candidates.size() > 3 
            ? llmRerank(question, candidates) 
            : rerank(question, candidates);
        log.info("重排序后: {} 个结果", rankedResults.size());

        // 4. 生成回答
        String answer;
        if (rankedResults.isEmpty()) {
            answer = handleEmptyKnowledgeBase(question);
        } else {
            answer = answerWithContext(question, rankedResults);
        }

        return answer;
    }

    /**
     * 查询改写扩展
     */
    private List<String> expandQuery(String question) {
        List<String> queries = new ArrayList<>();
        queries.add(question);

        // 提取关键词
        List<String> keywords = extractKeywords(question);
        if (!keywords.isEmpty()) {
            String keywordQuery = String.join(" ", keywords);
            if (!keywordQuery.equals(question)) {
                queries.add(keywordQuery);
            }
        }

        // 生成同义改写
        String synonymQuery = generateSynonymQuery(question);
        if (synonymQuery != null && !synonymQuery.equals(question)) {
            queries.add(synonymQuery);
        }

        return queries.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 提取关键词（使用 HanLP）
     */
    private List<String> extractKeywords(String text) {
        return keywordExtractionService.extract(text, 10);
    }

    /**
     * 生成同义改写
     */
    private String generateSynonymQuery(String question) {
        Map<String, String> synonyms = Map.of(
            "怎么", "如何", "如何", "怎么", "为什么", "原因",
            "什么是", "定义", "哪些", "什么", "使用", "应用"
        );
        
        for (Map.Entry<String, String> entry : synonyms.entrySet()) {
            if (question.contains(entry.getKey())) {
                return question.replace(entry.getKey(), entry.getValue());
            }
        }
        
        return null;
    }

    /**
     * 并行混合检索
     */
    private List<RetrievalResult> parallelHybridRetrieval(List<String> queries) {
        List<CompletableFuture<List<RetrievalResult>>> futures = new ArrayList<>();

        for (String query : queries) {
            // 并行执行向量检索
            String finalQuery = query;
            CompletableFuture<List<RetrievalResult>> vectorFuture = CompletableFuture.supplyAsync(
                () -> vectorSearch(finalQuery), executorService);

            // 并行执行 BM25 检索
            CompletableFuture<List<RetrievalResult>> bm25Future = CompletableFuture.supplyAsync(
                () -> bm25Search(finalQuery), executorService);

            futures.add(vectorFuture);
            futures.add(bm25Future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 合并结果（去重并综合得分）
        Map<String, RetrievalResult> resultMap = new ConcurrentHashMap<>();
        for (CompletableFuture<List<RetrievalResult>> future : futures) {
            try {
                for (RetrievalResult result : future.get()) {
                    String key = result.segment.text();
                    resultMap.merge(key, result, (old, fresh) -> {
                        old.score = Math.max(old.score, fresh.score);
                        old.sourceCount++;
                        return old;
                    });
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("检索结果合并失败", e);
            }
        }

        return new ArrayList<>(resultMap.values());
    }

    /**
     * 向量检索
     */
    private List<RetrievalResult> vectorSearch(String query) {
        try {
            Embedding queryEmbedding = embeddingService.embed(query);

            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(
                    EmbeddingSearchRequest.builder()
                            .queryEmbedding(queryEmbedding)
                            .maxResults(MAX_RESULTS)
                            .minScore(MIN_SCORE)
                            .build()
            ).matches();

            return matches.stream()
                    .map(match -> new RetrievalResult(match.embedded(), match.score(), "vector"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("向量检索失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * BM25 检索
     */
    private List<RetrievalResult> bm25Search(String query) {
        try {
            List<BM25Retriever.BM25Result> results = bm25Retriever.search(query, MAX_RESULTS);
            return results.stream()
                    .map(r -> new RetrievalResult(r.getDocument(), r.getScore(), "bm25"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("BM25 检索失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 规则重排序
     */
    private List<RetrievalResult> rerank(String question, List<RetrievalResult> candidates) {
        if (candidates.isEmpty()) {
            return candidates;
        }

        // 计算综合得分
        for (RetrievalResult result : candidates) {
            double vectorScore = "vector".equals(result.source) ? result.score : 0;
            double bm25Score = "bm25".equals(result.source) ? result.score : 0;
            double sourceBonus = result.sourceCount * 0.1;

            // 综合得分 = 向量得分 * 0.5 + BM25 得分 * 0.4 + 来源奖励 * 0.1
            result.finalScore = vectorScore * 0.5 + bm25Score * 0.4 + sourceBonus;
        }

        // 排序并取 Top-K
        candidates.sort((a, b) -> Double.compare(b.finalScore, a.finalScore));
        return candidates.stream().limit(FINAL_TOP_K).collect(Collectors.toList());
    }

    /**
     * LLM 重排序
     */
    private List<RetrievalResult> llmRerank(String question, List<RetrievalResult> candidates) {
        if (candidates.size() <= 3) {
            return rerank(question, candidates);
        }

        try {
            // 构建重排序提示词
            StringBuilder sb = new StringBuilder();
            sb.append("请根据问题对以下候选文档按相关性从高到低排序：\n\n");
            sb.append("问题：").append(question).append("\n\n");
            sb.append("候选文档（共").append(candidates.size()).append("个）：\n");

            for (int i = 0; i < candidates.size(); i++) {
                String text = truncate(candidates.get(i).segment.text(), 250);
                String title = getMetadataString(candidates.get(i).segment.metadata(), "title", "未知");
                sb.append(String.format("%d. [%s] %s\n", i + 1, title, text));
            }

            sb.append("\n请只返回排序后的序号，用英文逗号分隔，不要解释：");

            // 调用 LLM 进行重排序
            String response = chatLanguageModel.generate(sb.toString()).trim();
            log.info("LLM 重排序结果: {}", response);

            // 解析排序结果
            List<Integer> rankings = parseRanking(response, candidates.size());

            // 根据排序结果重新排列
            List<RetrievalResult> reranked = new ArrayList<>();
            for (int idx : rankings) {
                if (idx > 0 && idx <= candidates.size()) {
                    RetrievalResult result = candidates.get(idx - 1);
                    result.finalScore = candidates.size() - idx + 1; // 位置越高分数越高
                    reranked.add(result);
                }
            }

            // 如果 LLM 返回无效结果，降级到规则排序
            if (reranked.isEmpty()) {
                log.warn("LLM 重排序失败，使用规则排序");
                return rerank(question, candidates);
            }

            return reranked.stream().limit(FINAL_TOP_K).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("LLM 重排序失败，降级到规则排序", e);
            return rerank(question, candidates);
        }
    }

    /**
     * 解析排序结果
     */
    private List<Integer> parseRanking(String response, int maxIndex) {
        List<Integer> rankings = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();

        try {
            String[] parts = response.split("[,，\\s]+");
            for (String part : parts) {
                part = part.trim();
                if (part.matches("\\d+")) {
                    int idx = Integer.parseInt(part);
                    if (idx > 0 && idx <= maxIndex && !seen.contains(idx)) {
                        rankings.add(idx);
                        seen.add(idx);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("排序结果解析失败: {}", response);
        }

        return rankings;
    }

    /**
     * 截断文本
     */
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    /**
     * 处理知识库为空的情况
     */
    private String handleEmptyKnowledgeBase(String question) {
        String prompt = """
                请基于你的自身知识回答以下问题：
                
                问题：
                %s
                
                注意：
                1. 本回答基于你的训练知识，不保证百分百准确
                2. 如果不确定答案，请明确说明
                3. 对于事实性问题，如果没有十足把握，请建议用户查阅权威来源
                
                请用简洁明了的语言回答。
                """.formatted(question);
        
        String rawAnswer = chatLanguageModel.generate(prompt);
        return "[信息来源：大模型训练知识，仅供参考]\n\n" + rawAnswer;
    }

    /**
     * 基于知识库上下文回答（带引用溯源）
     */
    private String answerWithContext(String question, List<RetrievalResult> results) {
        StringBuilder contextBuilder = new StringBuilder();
        List<String> citations = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            RetrievalResult result = results.get(i);
            String title = getMetadataString(result.segment.metadata(), "title", "未知文档");
            String text = result.segment.text();

            contextBuilder.append(String.format("[文档%d] %s\n%s\n\n", i + 1, title, text));
            citations.add(String.format("- %s (相关度: %.2f)", title, result.finalScore));
        }

        String context = contextBuilder.toString();
        log.info("构建上下文长度: {} 字符", context.length());

        String prompt = """
                根据以下上下文信息回答问题：
                
                上下文：
                %s
                
                问题：
                %s
                
                要求：
                1. 必须基于提供的上下文信息回答
                2. 如果上下文没有相关信息，请明确说明"知识库中未找到相关信息"
                3. 不要编造信息，不要使用上下文以外的知识
                4. 回答要简洁明了，直接针对问题
                5. 在回答中标注引用来源，格式：[文档X]
                
                请给出你的回答：
                """.formatted(context, question);

        String rawAnswer = chatLanguageModel.generate(prompt);
        
        StringBuilder answer = new StringBuilder();
        answer.append("[信息来源：知识库]\n\n");
        answer.append(rawAnswer);
        answer.append("\n\n---\n**引用来源：**\n");
        for (String citation : citations) {
            answer.append(citation).append("\n");
        }

        return answer.toString();
    }

    /**
     * 加载文本作为文档
     */
    public void loadTextAsDocument(String text, String title) {
        Embedding embedding = embeddingService.embedWithChunking(text);
        TextSegment segment = TextSegment.from(text, Metadata.from("title", title));
        
        embeddingStore.add(embedding, segment);
        documentStore.put(title, segment);
        documentTitles.put(title, text);
        bm25Retriever.addDocument(segment);
        documentCount.incrementAndGet();

        log.info("文档已加载: {}，当前文档总数: {}", title, documentCount.get());
    }

    /**
     * 获取知识库大小
     */
    public int getKnowledgeBaseSize() {
        return documentCount.get();
    }

    /**
     * 获取知识库统计信息
     */
    public String getKnowledgeBaseStats() {
        return String.format("知识库统计：文档总数 = %d", documentCount.get());
    }

    /**
     * 清空知识库
     */
    public void clearKnowledgeBase() {
        documentCount.set(0);
        documentStore.clear();
        documentTitles.clear();
        bm25Retriever.clear();
        log.info("知识库已清空");
    }

    /**
     * 获取元数据字符串（兼容不同版本的 langchain4j）
     */
    private String getMetadataString(Metadata metadata, String key, String defaultValue) {
        if (metadata == null) {
            return defaultValue;
        }
        try {
            String value = metadata.getString(key);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 检索结果内部类
     */
    private static class RetrievalResult {
        TextSegment segment;
        double score;
        String source;
        int sourceCount = 1;
        double finalScore;

        RetrievalResult(TextSegment segment, double score, String source) {
            this.segment = segment;
            this.score = score;
            this.source = source;
            this.finalScore = score;
        }
    }
}
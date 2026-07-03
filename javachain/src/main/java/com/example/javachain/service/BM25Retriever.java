package com.example.javachain.service;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import dev.langchain4j.data.segment.TextSegment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * BM25 检索器 - 实现 BM25F 算法
 * 
 * BM25 是一种基于概率检索模型的排序算法，相比简单词频匹配有显著优势：
 * 1. 考虑词频饱和（TF 饱和）
 * 2. 考虑文档长度归一化
 * 3. 使用 IDF（逆文档频率）
 * 
 * 参数说明：
 * - k1: 词频饱和系数，通常取 1.2-2.0
 * - b: 文档长度归一化系数，通常取 0.75
 */
@Slf4j
@Service
public class BM25Retriever {

    /**
     * BM25 参数
     */
    private static final double K1 = 1.5;    // 词频饱和系数
    private static final double B = 0.75;     // 文档长度归一化系数
    private static final int MAX_RESULTS = 10;

    /**
     * 停用词集合
     */
    private static final Set<String> STOP_WORDS = Set.of(
        "的", "了", "是", "在", "有", "和", "与", "或", "这", "那", "什么", "怎么", "如何", "为什么",
        "一个", "一些", "所有", "每个", "任何", "许多", "几个", "这种", "那种", "各种", "其他",
        "可以", "可能", "应该", "必须", "需要", "会", "能", "要", "不", "没", "很", "非常", "比较",
        "就", "都", "也", "还", "又", "再", "更", "最", "太", "越", "只", "才", "已经", "正在"
    );

    private final Map<String, List<TextSegment>> documentIndex = new ConcurrentHashMap<>();
    private final Map<String, Integer> documentFrequency = new ConcurrentHashMap<>();
    private final Map<String, Integer> documentLengths = new ConcurrentHashMap<>();
    private volatile int totalDocuments = 0;
    private volatile double avgDocumentLength = 0;

    /**
     * 添加文档到索引
     */
    public void addDocument(TextSegment document) {
        String docId = getMetadataString(document.metadata(), "title", "unknown");
        
        // 更新文档长度
        int length = document.text().length();
        documentLengths.put(docId, length);
        
        // 更新词频索引
        List<String> terms = tokenize(document.text());
        for (String term : terms) {
            documentIndex.computeIfAbsent(term, k -> new ArrayList<>()).add(document);
            documentFrequency.merge(term, 1, Integer::sum);
        }
        
        // 更新统计信息
        totalDocuments++;
        avgDocumentLength = documentLengths.values().stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0);
    }

    /**
     * 批量添加文档
     */
    public void addDocuments(List<TextSegment> documents) {
        for (TextSegment doc : documents) {
            addDocument(doc);
        }
    }

    /**
     * BM25 检索
     */
    public List<BM25Result> search(String query, int topK) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyList();
        }

        // 分词
        List<String> queryTerms = tokenize(query);
        if (queryTerms.isEmpty()) {
            return Collections.emptyList();
        }

        // 收集候选文档（避免重复）
        Set<TextSegment> candidateDocs = new HashSet<>();
        for (String term : queryTerms) {
            List<TextSegment> docs = documentIndex.getOrDefault(term, Collections.emptyList());
            candidateDocs.addAll(docs);
        }

        // 如果没有候选文档，尝试模糊匹配
        if (candidateDocs.isEmpty()) {
            candidateDocs = fuzzyMatch(queryTerms);
        }

        // 计算 BM25 分数
        List<BM25Result> results = new ArrayList<>();
        for (TextSegment doc : candidateDocs) {
            double score = computeBM25(queryTerms, doc);
            if (score > 0) {
                results.add(new BM25Result(doc, score));
            }
        }

        // 按分数排序并取 Top-K
        results.sort((a, b) -> Double.compare(b.score, a.score));
        return results.stream().limit(Math.min(topK, MAX_RESULTS)).collect(Collectors.toList());
    }

    /**
     * 简化的检索方法
     */
    public List<BM25Result> search(String query) {
        return search(query, MAX_RESULTS);
    }

    /**
     * 计算 BM25 分数
     */
    private double computeBM25(List<String> queryTerms, TextSegment document) {
        double score = 0;
        String docText = document.text();
        int docLength = docText.length();

        // 计算文档内词频
        Map<String, Integer> termFreq = new HashMap<>();
        List<String> docTerms = tokenize(docText);
        for (String term : docTerms) {
            termFreq.merge(term, 1, Integer::sum);
        }

        // 对每个查询词计算分数
        for (String term : queryTerms) {
            int tf = termFreq.getOrDefault(term, 0);
            int df = documentFrequency.getOrDefault(term, 0);

            if (tf == 0) continue;

            // IDF 计算（平滑处理）
            double idf = computeIDF(df);

            // BM25 公式
            double numerator = tf * (K1 + 1);
            double denominator = tf + K1 * (1 - B + B * docLength / avgDocumentLength);
            score += idf * numerator / denominator;
        }

        return score;
    }

    /**
     * 计算 IDF（逆文档频率）
     */
    private double computeIDF(int docFreq) {
        if (docFreq == 0) {
            // 未出现的词，给予默认 IDF
            return Math.log((totalDocuments + 1) / 1.0);
        }
        // 平滑 IDF 公式
        return Math.log((totalDocuments - docFreq + 0.5) / (docFreq + 0.5));
    }

    /**
     * 分词（使用 HanLP）
     */
    private List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<String> tokens = new ArrayList<>();
            List<Term> terms = HanLP.segment(text);

            for (Term term : terms) {
                String word = term.word.trim().toLowerCase();
                // 过滤条件
                if (word.length() < 2) continue;
                if (STOP_WORDS.contains(word)) continue;
                tokens.add(word);
            }

            return tokens;
        } catch (Exception e) {
            log.warn("HanLP 分词失败，使用简单分词", e);
            return simpleTokenize(text);
        }
    }

    /**
     * 简单分词（降级方案）
     */
    private List<String> simpleTokenize(String text) {
        List<String> tokens = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[\\u4e00-\\u9fa5]{2,}|[a-zA-Z]{2,}");
        java.util.regex.Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String word = matcher.group().toLowerCase();
            if (!STOP_WORDS.contains(word)) {
                tokens.add(word);
            }
        }

        return tokens;
    }

    /**
     * 模糊匹配（当精确匹配无结果时）
     */
    private Set<TextSegment> fuzzyMatch(List<String> queryTerms) {
        Set<TextSegment> candidates = new HashSet<>();

        // 尝试前缀匹配
        for (String term : queryTerms) {
            for (String indexedTerm : documentIndex.keySet()) {
                if (indexedTerm.startsWith(term) || term.startsWith(indexedTerm)) {
                    candidates.addAll(documentIndex.get(indexedTerm));
                }
            }
        }

        return candidates;
    }

    /**
     * 清空索引
     */
    public void clear() {
        documentIndex.clear();
        documentFrequency.clear();
        documentLengths.clear();
        totalDocuments = 0;
        avgDocumentLength = 0;
    }

    /**
     * 获取索引统计信息
     */
    public Map<String, Object> getStats() {
        return Map.of(
            "totalDocuments", totalDocuments,
            "avgDocumentLength", avgDocumentLength,
            "uniqueTerms", documentIndex.size()
        );
    }

    /**
     * BM25 检索结果
     */
    @Data
    @AllArgsConstructor
    public static class BM25Result {
        private TextSegment document;
        private double score;
    }

    /**
     * 获取元数据字符串（兼容不同版本的 langchain4j）
     */
    private String getMetadataString(dev.langchain4j.data.document.Metadata metadata, String key, String defaultValue) {
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
}
package com.example.javachain.service;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 关键词提取服务 - 使用 HanLP 分词器
 * 
 * 特性：
 * 1. 基于 HanLP 的专业中文分词
 * 2. 支持词性过滤（名词、动词、形容词等）
 * 3. 停用词过滤
 * 4. 词频统计排序
 * 5. 命名实体识别（人名、地名、机构名）
 */
@Slf4j
@Service
public class KeywordExtractionService {
    
    /**
     * 停用词集合 - 高频无意义词汇
     */
    private static final Set<String> STOP_WORDS = Set.of(
        "的", "了", "是", "在", "有", "和", "与", "或", "这", "那", "什么", "怎么", "如何", "为什么",
        "一个", "一些", "所有", "每个", "任何", "许多", "几个", "这种", "那种", "各种", "其他", "另外",
        "可以", "可能", "应该", "必须", "需要", "会", "能", "要", "不", "没", "很", "非常", "比较",
        "就", "都", "也", "还", "又", "再", "更", "最", "太", "越", "只", "才", "已经", "正在", "曾经",
        "但是", "然而", "虽然", "因为", "所以", "如果", "既然", "尽管", "除非", "否则", "因此",
        "而", "并且", "以及", "等等", "例如", "比如", "包括", "通过", "根据", "按照", "关于",
        "对于", "至于", "说到", "提到", "认为", "觉得", "知道", "了解", "明白", "清楚",
        "问题", "情况", "事情", "方式", "方法", "过程", "结果", "原因", "影响", "作用",
        "进行", "处理", "解决", "实现", "完成", "达到", "提高", "增加", "减少", "保持",
        "系统", "功能", "模块", "接口", "数据", "信息", "内容", "文件", "文档", "项目",
        "使用", "应用", "开发", "设计", "测试", "部署", "运行", "管理", "配置", "维护"
    );
    
    /**
     * 允许的词性集合
     * n:名词, ns:地名, nr:人名, nt:机构名, nz:其他专有名词
     * v:动词, vd:趋向动词, vn:动名词
     * a:形容词, ad:副词, an:名形词
     */
    private static final Set<String> ALLOWED_POS = Set.of(
        "n", "ns", "nr", "nt", "nz", "v", "vd", "vn", "a", "ad", "an"
    );
    
    /**
     * 提取关键词
     * 
     * @param text 待处理文本
     * @param topK 返回的关键词数量
     * @return 关键词列表（按词频排序）
     */
    public List<String> extract(String text, int topK) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            // 使用 HanLP 分词
            List<Term> terms = HanLP.segment(text);
            
            // 词频统计
            Map<String, Integer> wordCount = new HashMap<>();
            
            for (Term term : terms) {
                String word = term.word.trim();
                String pos = term.nature.toString().toLowerCase();
                
                // 过滤条件
                if (word.length() < 2) continue;
                if (STOP_WORDS.contains(word)) continue;
                if (!ALLOWED_POS.contains(pos)) continue;
                
                wordCount.merge(word, 1, Integer::sum);
            }
            
            // 按词频排序，取 Top-K
            return wordCount.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("关键词提取失败", e);
            // 降级到简单提取
            return simpleExtract(text, topK);
        }
    }
    
    /**
     * 提取关键词（默认返回前10个）
     */
    public List<String> extract(String text) {
        return extract(text, 10);
    }
    
    /**
     * 提取关键词（包含命名实体优先）
     */
    public List<String> extractWithNer(String text, int topK) {
        List<String> keywords = extract(text, topK * 2);
        List<String> entities = extractEntities(text);
        
        // 合并去重，实体优先
        Set<String> merged = new LinkedHashSet<>();
        merged.addAll(entities);
        merged.addAll(keywords);
        
        return merged.stream().limit(topK).collect(Collectors.toList());
    }
    
    /**
     * 提取命名实体
     */
    private List<String> extractEntities(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            List<String> entities = new ArrayList<>();
            List<Term> terms = HanLP.segment(text);
            
            for (Term term : terms) {
                String word = term.word.trim();
                String pos = term.nature.toString().toLowerCase();
                
                // 识别命名实体
                if (pos.startsWith("nr")) {      // 人名
                    entities.add(word);
                } else if (pos.startsWith("ns")) { // 地名
                    entities.add(word);
                } else if (pos.startsWith("nt")) { // 机构名
                    entities.add(word);
                }
            }
            
            return entities.stream().distinct().collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("命名实体识别失败，跳过", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 简单提取（降级方案）
     */
    private List<String> simpleExtract(String text, int topK) {
        List<String> keywords = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        
        // 简单正则分割
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[\\u4e00-\\u9fa5]{2,}|[a-zA-Z]{2,}|[0-9]{2,}");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            String word = matcher.group();
            if (!STOP_WORDS.contains(word) && !seen.contains(word)) {
                keywords.add(word);
                seen.add(word);
            }
            if (keywords.size() >= topK) {
                break;
            }
        }
        
        return keywords;
    }
}
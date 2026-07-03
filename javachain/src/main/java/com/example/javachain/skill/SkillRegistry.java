package com.example.javachain.skill;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class SkillRegistry {
    private static final Logger log = LoggerFactory.getLogger(SkillRegistry.class);

    @Value("${skill.pattern:.trae/skills/*/SKILL.md}")
    private String skillPattern;

    private final SkillParser skillParser;
    private final Map<String, SkillDefinition> skillsByName = new HashMap<>();
    private final Map<String, List<SkillDefinition>> skillsByTrigger = new HashMap<>();

    public SkillRegistry(SkillParser skillParser) {
        this.skillParser = skillParser;
    }

    @PostConstruct
    public void init() {
        loadSkills();
    }

    public void loadSkills() {
        log.info("开始加载 Skills，pattern: {}", skillPattern);

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(skillPattern);

            log.info("找到 {} 个 Skill 资源", resources.length);

            skillsByName.clear();
            skillsByTrigger.clear();

            for (Resource resource : resources) {
                try {
                    String path = resource.getURL().getPath();
                    String skillName = extractSkillName(path);

                    try (InputStream is = resource.getInputStream()) {
                        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        SkillDefinition skill = skillParser.parse(skillName, content);
                        if (skill != null) {
                            log.info("加载 Skill: {}, 描述: {}, 触发词: {}, 步骤数: {}",
                                    skill.getName(), skill.getDescription(), skill.getTrigger(),
                                    skill.getSteps() != null ? skill.getSteps().size() : 0);
                            for (int i = 0; skill.getSteps() != null && i < skill.getSteps().size(); i++) {
                                SkillStep step = skill.getSteps().get(i);
                                log.debug("  步骤{}: {} -> {}", step.getOrder(), step.getName(), step.getToolName());
                            }
                            register(skill);
                        }
                    }
                } catch (Exception e) {
                    log.warn("加载 Skill 失败: {}", resource, e);
                }
            }

            log.info("Skills 加载完成, 总数: {}, 触发器: {}", skillsByName.size(), skillsByTrigger.keySet());
        } catch (IOException e) {
            log.error("扫描 Skill 目录失败", e);
        }
    }

    private String extractSkillName(String path) {
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("SKILL.md".equals(parts[i]) && i > 0) {
                return parts[i - 1];
            }
        }
        return "unknown";
    }

    public void register(SkillDefinition skill) {
        skillsByName.put(skill.getName(), skill);

        String trigger = skill.getTrigger();
        if (trigger != null && !trigger.isEmpty()) {
            skillsByTrigger.computeIfAbsent(trigger, k -> new ArrayList<>()).add(skill);
        }

        log.debug("注册 Skill: {}", skill.getName());
    }

    public Optional<SkillDefinition> getByName(String name) {
        return Optional.ofNullable(skillsByName.get(name));
    }

    public List<SkillDefinition> findByTrigger(String trigger) {
        List<SkillDefinition> matched = new ArrayList<>();

        for (Map.Entry<String, List<SkillDefinition>> entry : skillsByTrigger.entrySet()) {
            if (trigger.toLowerCase().contains(entry.getKey().toLowerCase())) {
                matched.addAll(entry.getValue());
            }
        }

        return matched;
    }

    public List<SkillDefinition> matchUserInput(String userInput) {
        List<SkillDefinition> matched = new ArrayList<>();

        for (SkillDefinition skill : skillsByName.values()) {
            if (skill.matches(userInput)) {
                matched.add(skill);
            }
        }

        return matched;
    }

    public List<SkillDefinition> getAll() {
        return new ArrayList<>(skillsByName.values());
    }

    public boolean isEmpty() {
        return skillsByName.isEmpty();
    }

    public int size() {
        return skillsByName.size();
    }
}

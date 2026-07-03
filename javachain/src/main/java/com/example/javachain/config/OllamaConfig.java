package com.example.javachain.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "javachain.llm.provider", havingValue = "ollama")
@Data
public class OllamaConfig {

    @Value("${javachain.llm.ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${javachain.llm.ollama.model:deepseek-r1:1.5b}")
    private String model;

    @Value("${javachain.llama.temperature:0.2}")
    private Double temperature;

    @Value("${javachain.llm.ollama.timeout:120s}")
    private String timeout;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(model)
                .temperature(temperature)
                .build();
    }
}
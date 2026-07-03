
package com.example.javachain.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "javachain.llm.provider", havingValue = "deepseek")
@Data
public class DeepSeekConfig {

    @Value("${javachain.llm.deepseek.api-key}")
    private String apiKey;

    @Value("${javachain.llm.deepseek.base-url}")
    private String baseUrl;

    @Value("${javachain.llm.deepseek.chat-model}")
    private String chatModel;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(chatModel)
                .temperature(0.2)
                .maxTokens(4096)
                .build();
    }
}


package com.example.javachain.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConditionalOnProperty(name = "javachain.llm.provider", havingValue = "deepseek")
@Data
@Slf4j
public class DeepSeekConfig {

    @Value("${javachain.llm.deepseek.api-key}")
    private String apiKey;

    @Value("${javachain.llm.deepseek.base-url}")
    private String baseUrl;

    @Value("${javachain.llm.deepseek.chat-model}")
    private String chatModel;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("DeepSeek API key is not configured. Set DEEPSEEK_API_KEY in environment variables or project .env.");
            return new MissingApiKeyChatLanguageModel();
        }
        log.info("DeepSeek chat model enabled. model={}, baseUrl={}", chatModel, baseUrl);
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(chatModel)
                .temperature(0.2)
                .maxTokens(4096)
                .build();
    }

    private static class MissingApiKeyChatLanguageModel implements ChatLanguageModel {

        private static final String MESSAGE = "DeepSeek API Key 未配置。请设置 DEEPSEEK_API_KEY 后重启 javachain 服务。";

        @Override
        public Response<AiMessage> generate(List<ChatMessage> messages) {
            return Response.from(AiMessage.from(MESSAGE));
        }
    }
}

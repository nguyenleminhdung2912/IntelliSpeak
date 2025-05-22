package com.gsu25se05.itellispeak.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class GeminiAIConfig {
    @Value("${genai.api.key}")
    private String apiKey;

    @Bean
    public String genAiApiKey() {
        return apiKey;
    }
}

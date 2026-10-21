package com.example.textblast;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/** Free option: Google Gemini via an AI Studio API key (free tier, no billing needed). */
@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "gemini", matchIfMissing = true)
public class GeminiLlmClient implements LlmClient {

    private final RestClient client;
    private final String model;
    private final String apiKey;

    public GeminiLlmClient(@Value("${gemini.api-key}") String apiKey, @Value("${gemini.model}") String model) {
        this.model = model;
        this.apiKey = apiKey;
        this.client = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
    }

    @Override
    public String complete(String system, String userPrompt) {
        if (apiKey == null || apiKey.isBlank())
            throw new GenerationException("GEMINI_API_KEY is not set. Set it in the same terminal you run mvn spring-boot:run from, then restart the backend.", null);
        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", system))),
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", userPrompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json"));
        JsonNode res = client.post().uri("/v1beta/models/{m}:generateContent", model)
                .contentType(MediaType.APPLICATION_JSON).body(body)
                .retrieve().body(JsonNode.class);
        return res.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
    }
}
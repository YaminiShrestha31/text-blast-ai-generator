package com.example.textblast;

/** One method every provider implements: send a system + user prompt, get raw text back. */
public interface LlmClient {
    String complete(String system, String userPrompt);
}

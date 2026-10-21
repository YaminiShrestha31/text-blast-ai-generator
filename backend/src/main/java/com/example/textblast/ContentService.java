package com.example.textblast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ContentService {

    private static final Logger log = LoggerFactory.getLogger(ContentService.class);
    static final int MAX_CHARS = 1600; // 9-segment cap per the spec
    private static final Pattern EMOJI =
            Pattern.compile("[\\x{1F000}-\\x{1FAFF}\\x{2600}-\\x{27BF}\\x{2B50}\\x{2B55}\\x{FE0F}\\x{200D}]");

    private final LlmClient llm;
    private final ObjectMapper mapper;

    public ContentService(LlmClient llm, ObjectMapper mapper) {
        this.llm = llm;
        this.mapper = mapper;
    }

    public List<String> generate(GenerateRequest req) {
        String system = String.join("\n",
                "You write SMS campaign messages for any kind of business.",
                "Campaign type: " + req.campaignTypeOrDefault() + ". Tone: " + req.toneOrDefault() + ".",
                req.limitOrDefault()
                        ? "Each message MUST be under " + MAX_CHARS + " characters, counting emoji and symbols."
                        : "Messages may be longer if the content needs it.",
                req.emojiOrDefault() ? "Use emoji sparingly where they fit." : "Do NOT use any emoji.",
                "Write 3 clearly different variants (different hooks/structure).",
                "Return ONLY a JSON array of exactly 3 strings. No markdown, no commentary.");
        try {
            String raw = llm.complete(system, req.prompt().trim());
            List<String> variants = mapper.readValue(extractJsonArray(raw), new TypeReference<List<String>>() {});
            if (variants.size() < 3) throw new IllegalStateException("Model returned fewer than 3 variants");
            return variants.subList(0, 3).stream()
                    .map(v -> postProcess(v, req.limitOrDefault(), req.emojiOrDefault()))
                    .toList();
        } catch (GenerationException e) {
            log.error("Generation failed: {}", e.getMessage());
            throw e;
        } catch (HttpStatusCodeException e) {
            log.error("LLM returned HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new GenerationException(messageFor(e.getStatusCode().value()), e);
        } catch (ResourceAccessException e) {
            log.error("Could not reach the LLM service", e);
            throw new GenerationException("Could not reach the LLM service. Check your internet connection (or that Ollama is running).", e);
        } catch (Exception e) {
            log.error("Unexpected error while generating or parsing variants", e);
            throw new GenerationException("The model returned an unexpected response. Please try again.", e);
        }
    }

    static String messageFor(int status) {
        return switch (status) {
            case 400 -> "The LLM rejected the request (400). Check that your API key and model name are valid.";
            case 401, 403 -> "The API key was rejected (" + status + "). Check your API key.";
            case 404 -> "Model not found (404). Check the model name (GEMINI_MODEL).";
            case 429 -> "Free-tier rate limit reached (429). Wait a minute and try again.";
            default -> "The LLM service returned an error (" + status + ").";
        };
    }

    /** Small/local models sometimes add chatter or code fences; pull out just the [ ... ] part. */
    static String extractJsonArray(String raw) {
        Matcher m = Pattern.compile("\\[.*]", Pattern.DOTALL).matcher(raw);
        return m.find() ? m.group() : raw.trim();
    }

    /** Server-side safety net: enforce the emoji and length toggles regardless of what the model did. */
    static String postProcess(String text, boolean limit, boolean emoji) {
        String out = emoji ? text : EMOJI.matcher(text).replaceAll("");
        out = out.trim();
        if (limit && out.length() > MAX_CHARS) {
            int end = MAX_CHARS;
            if (Character.isHighSurrogate(out.charAt(end - 1))) end--; // don't split an emoji
            out = out.substring(0, end);
        }
        return out;
    }
}
package com.example.textblast;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateRequest(
        @NotBlank(message = "Prompt is required.") @Size(max = 1000, message = "Prompt is too long (max 1000 characters).") String prompt,
        String campaignType,
        String tone,
        Boolean limit,
        Boolean emoji) {

    public String campaignTypeOrDefault() { return campaignType == null || campaignType.isBlank() ? "Promotional" : campaignType; }
    public String toneOrDefault() { return tone == null || tone.isBlank() ? "Friendly" : tone; }
    public boolean limitOrDefault() { return limit == null || limit; }
    public boolean emojiOrDefault() { return emoji == null || emoji; }
}

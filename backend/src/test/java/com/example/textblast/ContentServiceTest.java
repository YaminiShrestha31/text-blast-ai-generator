package com.example.textblast;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContentServiceTest {

    @Test
    void stripsEmojiWhenDisabled() {
        assertEquals("Sale today!", ContentService.postProcess("Sale today! 🎉🔥", false, false));
    }

    @Test
    void keepsEmojiWhenEnabled() {
        assertTrue(ContentService.postProcess("Sale 🎉", false, true).contains("🎉"));
    }

    @Test
    void truncatesToLimitWithoutSplittingEmoji() {
        String long1 = "a".repeat(1599) + "🎉" + "b".repeat(50);
        String out = ContentService.postProcess(long1, true, true);
        assertTrue(out.length() <= ContentService.MAX_CHARS);
        assertFalse(Character.isHighSurrogate(out.charAt(out.length() - 1)));
    }

    @Test
    void doesNotTruncateWhenLimitOff() {
        assertEquals(2000, ContentService.postProcess("a".repeat(2000), false, true).length());
    }

    @Test
    void extractsJsonArrayFromChattyOutput() {
        String raw = "Sure! Here you go:\n```json\n[\"a\",\"b\",\"c\"]\n```";
        assertEquals("[\"a\",\"b\",\"c\"]", ContentService.extractJsonArray(raw));
    }
}

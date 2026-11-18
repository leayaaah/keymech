package vn.edu.iuh.keymech.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import vn.edu.iuh.keymech.config.AppProperties;
import vn.edu.iuh.keymech.exception.RateLimitedException;

class ChatRateLimiterTest {

    @Test
    @DisplayName("TC-BOT-13: câu hỏi thứ 11 trong một phút bị chặn")
    void checkAndRecord_eleventhQuestion_throwsRateLimit() {
        AppProperties.Chatbot config = new AppProperties.Chatbot("rule", false, 8, 10, 200);
        ChatRateLimiter limiter = new ChatRateLimiter(new AppProperties(null, null, config));
        MockHttpSession session = new MockHttpSession();
        for (int i = 0; i < 10; i++) limiter.checkAndRecord(session);
        assertThrows(RateLimitedException.class, () -> limiter.checkAndRecord(session));
    }
}

package vn.edu.iuh.keymech.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.edu.iuh.keymech.ai.ChatbotService;
import vn.edu.iuh.keymech.dto.ChatResponse;
import vn.edu.iuh.keymech.exception.GlobalExceptionHandler;
import vn.edu.iuh.keymech.exception.RateLimitedException;
import vn.edu.iuh.keymech.service.ChatRateLimiter;

class ChatControllerTest {

    private final ChatbotService chatbot = org.mockito.Mockito.mock(ChatbotService.class);
    private final ChatRateLimiter limiter = org.mockito.Mockito.mock(ChatRateLimiter.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ChatController(chatbot, limiter))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    @DisplayName("TC-BOT-01: gửi câu hỏi và đọc lại lịch sử trong Session")
    void ask_success_storesSessionHistory() throws Exception {
        MockHttpSession session = new MockHttpSession();
        when(chatbot.reply("Phím 75%"))
            .thenReturn(new ChatResponse("Đây là gợi ý", List.of(), List.of(), "rule"));

        mvc.perform(post("/api/chat").session(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Phím 75%\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mode").value("rule"));
        mvc.perform(get("/api/chat/history").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].role").value("user"))
            .andExpect(jsonPath("$[1].content").value("Đây là gợi ý"));
    }

    @Test
    @DisplayName("TC-BOT-16: câu hỏi rỗng trả 400")
    void ask_blankMessage_returnsValidationError() throws Exception {
        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"   \"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        verify(chatbot, never()).reply(anyString());
    }

    @Test
    @DisplayName("TC-BOT-13: vượt giới hạn trả 429")
    void ask_rateLimited_returns429() throws Exception {
        org.mockito.Mockito.doThrow(new RateLimitedException()).when(limiter)
            .checkAndRecord(org.mockito.ArgumentMatchers.any());
        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Phím cơ\"}"))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.error").value("RATE_LIMITED"));
    }
}

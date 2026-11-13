package vn.edu.iuh.keymech.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;
import vn.edu.iuh.keymech.ai.ChatbotService;
import vn.edu.iuh.keymech.dto.ChatMessageResponse;
import vn.edu.iuh.keymech.dto.ChatRequest;
import vn.edu.iuh.keymech.dto.ChatResponse;
import vn.edu.iuh.keymech.service.ChatRateLimiter;
import vn.edu.iuh.keymech.util.SessionKeys;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatbotService chatbot;
    private final ChatRateLimiter rateLimiter;

    public ChatController(ChatbotService chatbot, ChatRateLimiter rateLimiter) {
        this.chatbot = chatbot;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> ask(@Valid @RequestBody ChatRequest request, HttpSession session) {
        synchronized (WebUtils.getSessionMutex(session)) {
            rateLimiter.checkAndRecord(session);
            ChatResponse response = chatbot.reply(request.message());
            List<ChatMessageResponse> history = new ArrayList<>(history(session));
            history.add(new ChatMessageResponse("user", request.message()));
            history.add(new ChatMessageResponse("assistant", response.answer()));
            if (history.size() > 20) history = new ArrayList<>(history.subList(history.size() - 20, history.size()));
            session.setAttribute(SessionKeys.CHAT_HISTORY, history);
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageResponse>> historyResponse(HttpSession session) {
        return ResponseEntity.ok(history(session));
    }

    @SuppressWarnings("unchecked")
    private List<ChatMessageResponse> history(HttpSession session) {
        List<ChatMessageResponse> saved = (List<ChatMessageResponse>) session.getAttribute(SessionKeys.CHAT_HISTORY);
        return saved == null ? List.of() : List.copyOf(saved);
    }
}

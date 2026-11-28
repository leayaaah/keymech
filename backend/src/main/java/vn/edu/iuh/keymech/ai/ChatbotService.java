package vn.edu.iuh.keymech.ai;

import vn.edu.iuh.keymech.dto.ChatContext;
import vn.edu.iuh.keymech.dto.ChatResponse;

public interface ChatbotService {
    default ChatResponse reply(String question) {
        return reply(question, null);
    }

    ChatResponse reply(String question, ChatContext context);
}

package vn.edu.iuh.keymech.ai;

import vn.edu.iuh.keymech.dto.ChatResponse;

public interface ChatbotService {
    ChatResponse reply(String question);
}

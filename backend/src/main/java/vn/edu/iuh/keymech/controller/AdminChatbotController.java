package vn.edu.iuh.keymech.controller;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.keymech.ai.ProductIndexer;
import vn.edu.iuh.keymech.exception.BusinessException;

@RestController
public class AdminChatbotController {

    private final ObjectProvider<ProductIndexer> indexer;

    public AdminChatbotController(ObjectProvider<ProductIndexer> indexer) {
        this.indexer = indexer;
    }

    @PostMapping("/api/admin/chatbot/reindex")
    public ReindexResponse reindex() {
        ProductIndexer service = indexer.getIfAvailable();
        if (service == null) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "CHATBOT_AI_DISABLED",
                "Chỉ mục chatbot cần chế độ AI và khóa Gemini.");
        }
        return new ReindexResponse(service.reindex());
    }

    public record ReindexResponse(int indexedDocuments) {}
}

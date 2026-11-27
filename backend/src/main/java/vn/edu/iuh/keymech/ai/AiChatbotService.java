package vn.edu.iuh.keymech.ai;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.io.ClassPathResource;
import vn.edu.iuh.keymech.dto.ChatProductResponse;
import vn.edu.iuh.keymech.dto.ChatResponse;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.repository.ChatProductRepository;

@Slf4j
public class AiChatbotService implements ChatbotService {

    private static final Pattern GREETING = Pattern.compile("(?:xin chao|chao ban|hello)[\\p{Punct} ]*");
    private static final Pattern INTERNAL_INSTRUCTIONS = Pattern.compile(
        "\\b(?:system prompt|chi dan he thong|huong dan he thong|loi nhac he thong)\\b");
    private static final Pattern OUTSIDE_CATALOG = Pattern.compile(
        "\\b(?:thoi tiet|troi mua|hom nay co mua|bong da)\\b");
    private static final Pattern PRIVATE_INPUT = Pattern.compile(
        "@|https?://|\\b(?:\\+?84|0)(?:[ .-]*\\d){8,10}\\b"
            + "|\\b(?:ma don|don hang|so dien thoai|email|dia chi|ho ten|ten toi|ten minh)\\b");
    private static final Pattern PRODUCT_TEXT = Pattern.compile("[\\p{L}\\p{N}%]+");
    private static final Pattern PRODUCT_CLAIM = Pattern.compile(
        "https?://|/products/|₫|\\b(?:con hang|het hang|ton kho|khuyen mai|giam gia|vnd)\\b"
            + "|\\b\\d{1,3}(?:[.,]\\d{3})+\\b|\\d[\\d.,]*\\s*(?:d|dong|trieu|tr|nghin|k)\\b");
    private static final Set<String> KEYBOARD_WORDS = Set.copyOf(Arrays.asList(("""
        ban phim co keyboard switch nut cong tac linear tactile clicky hot swap thay keycap cherry oem pbt abs profile
        layout kich thuoc gon nho fullsize full size tkl 100% 75% 65% 60% 40% custom tuy chinh lap rap kit plate pcb
        gaming choi game van phong hoc tap lam viec go em im on tieng am thanh nhe nang luc nhan bam nhanh
        led rgb den mau sac dep day khong wireless bluetooth usb ket noi pin sac mac windows
        co san hang het tim mua gia re dat cu the ngan sach duoi tren tam khoang toi da trieu tr nghin k dong vnd
        tu van nen chon goi y phu hop dung cho can thich loai nao gi the nao khac nhau so sanh voi va hoac
        giao van chuyen doi tra bao hanh chinh sach quy trinh yeu cau bao gia san pham phu kien chat lieu
        """).strip().split("\\s+")));

    private final RuleBasedChatbotService rule;
    private final ChatProductRepository products;
    private final VectorStore store;
    private final ChatClient chat;
    private final ExecutorService executor;
    private final int timeoutSeconds;
    private final String systemPrompt;

    AiChatbotService(RuleBasedChatbotService rule, ChatProductRepository products, VectorStore store,
                     ChatClient chat, ExecutorService executor, int timeoutSeconds) {
        this.rule = rule;
        this.products = products;
        this.store = store;
        this.chat = chat;
        this.executor = executor;
        this.timeoutSeconds = timeoutSeconds;
        try {
            this.systemPrompt = new ClassPathResource("chatbot/system-prompt.txt")
                .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ChatResponse reply(String question) {
        String normalized = RuleBasedChatbotService.normalize(question).strip();
        if (INTERNAL_INSTRUCTIONS.matcher(normalized).find() || OUTSIDE_CATALOG.matcher(normalized).find()) {
            return rule.outOfScope();
        }
        if (GREETING.matcher(normalized).matches() || PRIVATE_INPUT.matcher(normalized).find()) {
            return rule.reply(question);
        }

        Future<ChatResponse> task = null;
        try {
            task = executor.submit(() -> answer(normalized));
            ChatResponse response = task.get(timeoutSeconds, TimeUnit.SECONDS);
            if (response != null) return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.debug("Chatbot dùng câu trả lời rule ({})", e.getCause().getClass().getSimpleName());
        } catch (RejectedExecutionException | TimeoutException e) {
            log.debug("Chatbot dùng câu trả lời rule ({})", e.getClass().getSimpleName());
        } finally {
            if (task != null && !task.isDone()) task.cancel(true);
        }
        return rule.reply(question);
    }

    private ChatResponse answer(String question) {
        String layout = RuleBasedChatbotService.layout(question);
        BigDecimal budget = RuleBasedChatbotService.budget(question);
        String query = catalogQuery(question);
        if (query.isBlank()) return null;

        var filters = new FilterExpressionBuilder();
        var productFilter = filters.and(filters.eq("type", "PRODUCT"), filters.eq("status", "ACTIVE"));
        if (layout != null) productFilter = filters.and(productFilter, filters.eq("layout", layout));
        List<Long> retrieved = store.similaritySearch(SearchRequest.builder().query(query).topK(8)
                .similarityThreshold(0.5).filterExpression(productFilter.build()).build()).stream()
            .map(AiChatbotService::productId).filter(id -> id != null).distinct().toList();
        var knowledge = store.similaritySearch(SearchRequest.builder().query(query).topK(3)
            .similarityThreshold(0.5).filterExpression(filters.eq("type", "KNOWLEDGE").build()).build());
        var candidates = eligibleProducts(retrieved, layout, budget).stream()
            .sorted(Comparator.comparing(product -> product.getStockQuantity() == 0)).limit(3).toList();
        if (knowledge.isEmpty() && candidates.isEmpty()) return null;

        String catalog = candidates.stream().map(product -> "%d | %s | %s | %s".formatted(
                product.getId(), product.getName(), product.getLayout(), product.getSwitchType()))
            .collect(Collectors.joining("\n"));
        String reference = knowledge.stream().map(Document::getText).collect(Collectors.joining("\n\n"));
        ChatAnswer answer = chat.prompt().system(systemPrompt).user("""
            Từ khóa câu hỏi: %s
            Sản phẩm (ID | tên | layout | switch):
            %s
            Kiến thức tham khảo:
            %s
            """.formatted(query, catalog, reference)).call().entity(ChatAnswer.class);
        if (answer == null || answer.answer() == null || answer.answer().isBlank() || answer.productIds() == null
                || PRODUCT_CLAIM.matcher(RuleBasedChatbotService.normalize(answer.answer())).find()) {
            return null;
        }

        var allowed = candidates.stream().map(Product::getId).collect(Collectors.toSet());
        var selected = new LinkedHashSet<>(answer.productIds());
        selected.retainAll(allowed);
        var cards = eligibleProducts(List.copyOf(selected), layout, budget).stream()
            .sorted(Comparator.comparing(product -> product.getStockQuantity() == 0))
            .map(ChatProductResponse::from).toList();
        if (!selected.isEmpty() && cards.size() != selected.size()) return null;
        return new ChatResponse(answer.answer(), cards, List.of(), "ai");
    }

    // Chỉ gửi từ vựng tư vấn đã soạn và token của tên/nhãn hiệu công khai trong danh mục.
    // Không gửi câu gốc, dữ liệu tài khoản hoặc lịch sử phiên ra Gemini.
    private String catalogQuery(String question) {
        var allowed = new HashSet<>(KEYBOARD_WORDS);
        for (Product product : products.findPublicProducts()) {
            String catalogName = RuleBasedChatbotService.normalize(product.getName() + " "
                + (product.getBrand() == null ? "" : product.getBrand()));
            PRODUCT_TEXT.matcher(catalogName).results().map(result -> result.group())
                .filter(token -> token.chars().anyMatch(Character::isLetter)).forEach(allowed::add);
        }
        return PRODUCT_TEXT.matcher(question).results().map(result -> result.group())
            .filter(allowed::contains).collect(Collectors.joining(" "));
    }

    private List<Product> eligibleProducts(List<Long> ids, String layout, BigDecimal budget) {
        Map<Long, Product> found = ids.isEmpty() ? Map.of() : products.findPublicProductsByIds(ids).stream()
            .filter(product -> layout == null || layout.equalsIgnoreCase(product.getLayout()))
            .filter(product -> budget == null || product.getPrice().compareTo(budget) <= 0)
            .collect(Collectors.toMap(Product::getId, Function.identity()));
        return ids.stream().map(found::get).filter(product -> product != null).toList();
    }

    private static Long productId(Document document) {
        return document.getMetadata().get("productId") instanceof Number id ? id.longValue() : null;
    }

    public record ChatAnswer(String answer, List<Long> productIds) {}
}

package vn.edu.iuh.keymech.ai;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.io.ClassPathResource;
import vn.edu.iuh.keymech.dto.ChatContext;
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
    private static final Set<String> KEYBOARD_WORDS = words("""
        phim keyboard switch linear tactile clicky red brown blue black yellow silver
        hot swap keycap oem pbt abs profile ansi iso stem
        layout fullsize tkl 100% 75% 65% 60% 40% custom kit barebone plate pcb stabilizer
        gaming game led rgb wireless bluetooth usb silent solder lube puller
        """);
    private static final Set<String> STOP_WORDS = words("""
        a ban co khong nao gi the duoi tren toi tam khoang trieu tr nghin k dong vnd
        tu van nen chon goi y phu hop dung cho can thich loai khac nhau so sanh voi va hoac
        con hang het tim mua gia re dat cu ngan sach san pham cai gom mot bo mau day cap
        duoc roi nua minh nay do ay la may du se sao
        """);
    private static final Pattern CATALOG_PHRASE = Pattern.compile(
        "\\b(?:ban phim|phu kien|ke tay|cap usb(?:[ -]c)?|cap sac|dung cu|go em|it on|am thanh|van phong|hoc tap|khong day|mach han)\\b");
    private static final Pattern MODEL_SUFFIX = Pattern.compile(
        "(?:\\s*\\((?:goi|pack)\\s+\\d+\\)|\\s+(?:\\d{2,3}%|tkl|fullsize))$");
    private static final Pattern MODEL_CODE = Pattern.compile("\\b[a-z]+\\d+[a-z0-9]*\\b");
    // Điểm similarity của MariaDB Vector Store là 1 - cosine distance.
    private static final double PRODUCT_SIMILARITY = 0.65;
    private static final double KNOWLEDGE_SIMILARITY = 0.60;

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
    public ChatResponse reply(String question, ChatContext context) {
        String normalized = RuleBasedChatbotService.normalize(question).strip();
        if (INTERNAL_INSTRUCTIONS.matcher(normalized).find() || OUTSIDE_CATALOG.matcher(normalized).find()) {
            return rule.outOfScope();
        }
        if (GREETING.matcher(normalized).matches() || PRIVATE_INPUT.matcher(normalized).find()) {
            return rule.reply(question, context);
        }
        ChatResponse advice = rule.componentAdvice(normalized, context);
        if (advice != null) return advice;

        Future<ChatResponse> task = null;
        try {
            task = executor.submit(() -> answer(normalized));
            ChatResponse response = task.get(timeoutSeconds, TimeUnit.SECONDS);
            if (response != null) return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Chatbot dùng câu trả lời rule vì lượt tư vấn AI bị ngắt");
        } catch (ExecutionException e) {
            // Thông báo gốc từ nhà cung cấp có thể chứa dữ liệu hoặc thông tin xác thực của yêu cầu.
            Throwable cause = e.getCause();
            log.debug("Chatbot dùng câu trả lời rule vì truy xuất hoặc sinh câu trả lời AI thất bại ({})",
                cause == null ? e.getClass().getSimpleName() : cause.getClass().getSimpleName());
        } catch (RejectedExecutionException | TimeoutException e) {
            log.debug("Chatbot dùng câu trả lời rule ({})", e.getClass().getSimpleName());
        } finally {
            if (task != null && !task.isDone()) task.cancel(true);
        }
        return rule.reply(question, context);
    }

    private ChatResponse answer(String question) {
        String layout = RuleBasedChatbotService.layout(question);
        BigDecimal budget = RuleBasedChatbotService.budget(question);
        var catalog = products.findPublicProducts();
        String genericQuery = catalogQuery(question, Set.of());
        String query = catalogQuery(question, catalogTerms(catalog));
        String category = RuleBasedChatbotService.catalogCategory(question);
        if (query.isBlank()) {
            log.debug("Chatbot không dùng AI vì không còn từ khóa sau khi lọc");
            return category == null ? rule.clarify() : null;
        }

        boolean explanation = RuleBasedChatbotService.isExplanation(question);
        boolean requiresProducts = !explanation && (layout != null || budget != null || "switch".equals(category));
        var ids = explanation ? List.<Long>of() : searchProducts(query, layout, budget, category, catalog);
        if (requiresProducts && ids.isEmpty()) {
            log.debug("Chatbot dùng câu trả lời rule vì tìm kiếm vector không có sản phẩm theo điều kiện đã chọn");
            return null;
        }
        if (genericQuery.isBlank()) {
            log.debug("Chatbot không gọi model vì không có từ khóa tư vấn chung");
            return catalogResult(ids, layout, budget, category);
        }

        var filters = new FilterExpressionBuilder();
        var knowledge = store.similaritySearch(SearchRequest.builder().query(genericQuery).topK(3)
            .similarityThreshold(KNOWLEDGE_SIMILARITY)
            .filterExpression(filters.eq("type", "KNOWLEDGE").build()).build());
        if (knowledge.isEmpty()) {
            log.debug("Chatbot không gọi model vì không tìm được tài liệu kiến thức phù hợp");
            return catalogResult(ids, layout, budget, category);
        }
        String reference = knowledge.stream().map(Document::getText).collect(Collectors.joining("\n\n"));
        ChatAnswer answer = chat.prompt().system(systemPrompt).user("""
            Từ khóa tư vấn: %s
            Kiến thức tham khảo:
            %s
            """.formatted(genericQuery, reference)).call().entity(ChatAnswer.class);
        if (answer == null || answer.answer() == null || answer.answer().isBlank()) {
            log.debug("Chatbot bỏ câu trả lời AI vì nội dung rỗng");
            return catalogResult(ids, layout, budget, category);
        }
        if (PRODUCT_CLAIM.matcher(RuleBasedChatbotService.normalize(answer.answer())).find()) {
            log.debug("Chatbot bỏ câu trả lời AI vì có giá, tình trạng hàng hoặc đường dẫn");
            return catalogResult(ids, layout, budget, category);
        }
        if (mentionsProduct(answer.answer(), catalog)) {
            log.debug("Chatbot bỏ câu trả lời AI vì nhắc tên hoặc mã sản phẩm cụ thể");
            return catalogResult(ids, layout, budget, category);
        }
        var cards = eligibleProducts(ids, layout, budget, category);
        if (requiresProducts && cards.isEmpty()) {
            log.debug("Chatbot dùng câu trả lời rule vì không còn sản phẩm sau khi kiểm tra lại trong CSDL");
            return null;
        }
        return new ChatResponse(answer.answer(), cards, List.of(), "ai");
    }

    private ChatResponse catalogResult(List<Long> ids, String layout, BigDecimal budget, String category) {
        var cards = eligibleProducts(ids, layout, budget, category);
        if (cards.isEmpty()) {
            log.debug("Chatbot chuyển sang rule.reply vì không có thẻ sản phẩm hợp lệ từ tìm kiếm vector");
            return null;
        }
        return rule.catalogResults(cards);
    }

    private List<Long> searchProducts(String query, String layout, BigDecimal budget,
                                      String category, List<Product> catalog) {
        var categoryIds = catalog.stream().filter(p -> eligibleCategory(p, category))
            .map(p -> p.getCategory().getId()).distinct().toArray();
        if (categoryIds.length == 0) return List.of();

        var filters = new FilterExpressionBuilder();
        var productFilter = filters.and(filters.eq("type", "PRODUCT"), filters.eq("status", "ACTIVE"));
        productFilter = filters.and(productFilter, filters.in("categoryId", categoryIds));
        if (layout != null) productFilter = filters.and(productFilter, filters.eq("layout", layout));
        if (budget != null) productFilter = filters.and(productFilter, filters.lte("price", budget));
        return store.similaritySearch(SearchRequest.builder().query(query).topK(8)
                .similarityThreshold(PRODUCT_SIMILARITY).filterExpression(productFilter.build()).build()).stream()
            .map(AiChatbotService::productId).filter(id -> id != null).distinct().toList();
    }

    private List<ChatProductResponse> eligibleProducts(List<Long> ids, String layout, BigDecimal budget, String category) {
        if (ids.isEmpty()) return List.of();
        Map<Long, Product> found = products.findPublicProductsByIds(ids).stream()
            .filter(product -> eligibleCategory(product, category))
            .filter(product -> layout == null || layout.equalsIgnoreCase(product.getLayout()))
            .filter(product -> budget == null || product.getPrice().compareTo(budget) <= 0)
            .collect(Collectors.toMap(Product::getId, product -> product));
        return ids.stream().map(found::get).filter(product -> product != null)
            .sorted(Comparator.comparing(product -> product.getStockQuantity() == 0))
            .limit(3).map(ChatProductResponse::from).toList();
    }

    private static boolean eligibleCategory(Product product, String category) {
        String slug = product.getCategory().getSlug();
        return category == null ? !"dung-cu-phu-kien".equals(slug) : category.equals(slug);
    }

    private static Set<String> catalogTerms(List<Product> catalog) {
        var terms = new HashSet<String>();
        for (Product product : catalog) {
            String catalogName = RuleBasedChatbotService.normalize(product.getName() + " "
                + (product.getBrand() == null ? "" : product.getBrand()));
            PRODUCT_TEXT.matcher(catalogName).results().map(result -> result.group())
                .filter(token -> token.chars().anyMatch(Character::isLetter)).forEach(terms::add);
        }
        return terms;
    }

    // Chỉ dùng cụm tư vấn đã soạn và từ danh mục công khai. Chỉ cụm tư vấn được gửi vào prompt.
    private static String catalogQuery(String question, Set<String> catalogTerms) {
        var phrases = CATALOG_PHRASE.matcher(question).results().map(match -> match.group());
        var tokens = PRODUCT_TEXT.matcher(question).results().map(match -> match.group())
            .filter(token -> !STOP_WORDS.contains(token))
            .filter(token -> KEYBOARD_WORDS.contains(token) || catalogTerms.contains(token));
        return Stream.concat(phrases, tokens).distinct().collect(Collectors.joining(" "));
    }

    private static boolean mentionsProduct(String answer, List<Product> catalog) {
        String normalized = " " + nameWords(answer) + " ";
        return catalog.stream().map(Product::getName).map(RuleBasedChatbotService::normalize)
            .map(name -> MODEL_SUFFIX.matcher(name).replaceFirst(""))
            .map(AiChatbotService::nameWords).filter(name -> !name.isBlank())
            .flatMap(name -> Stream.concat(Stream.of(name), MODEL_CODE.matcher(name).results().map(match -> match.group())))
            .anyMatch(name -> normalized.contains(" " + name + " "));
    }

    private static String nameWords(String text) {
        return RuleBasedChatbotService.normalize(text).replaceAll("[^a-z0-9]+", " ").strip();
    }

    private static Set<String> words(String text) {
        return Set.of(text.strip().split("\\s+"));
    }

    private static Long productId(Document document) {
        return document.getMetadata().get("productId") instanceof Number id ? id.longValue() : null;
    }

    public record ChatAnswer(String answer) {}
}

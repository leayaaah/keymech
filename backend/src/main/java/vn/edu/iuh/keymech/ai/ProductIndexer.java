package vn.edu.iuh.keymech.ai;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;
import vn.edu.iuh.keymech.event.ProductChangedEvent;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.repository.ChatProductRepository;

@Slf4j
public class ProductIndexer {

    private final ChatProductRepository products;
    private final VectorStore store;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final ReentrantLock indexLock = new ReentrantLock();

    ProductIndexer(ChatProductRepository products, VectorStore store, JdbcTemplate jdbc,
                   PlatformTransactionManager transactions) {
        this.products = products;
        this.store = store;
        this.jdbc = jdbc;
        this.transactions = new TransactionTemplate(transactions);
    }

    @Async("chatbotIndexExecutor")
    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        indexLock.lock();
        try {
            if (jdbc.queryForList("SELECT id FROM vector_store WHERE JSON_VALUE(metadata, '$.type') = 'KNOWLEDGE' LIMIT 1",
                    String.class).isEmpty()) {
                rebuild();
            }
        } catch (RuntimeException e) {
            log.warn("Không khởi tạo được chỉ mục chatbot ({})", e.getClass().getSimpleName());
        } finally {
            indexLock.unlock();
        }
    }

    @Async("chatbotIndexExecutor")
    @TransactionalEventListener
    public void onProductChanged(ProductChangedEvent event) {
        indexLock.lock();
        try {
            long id = event.getProductId();
            products.findPublicProduct(id).ifPresentOrElse(
                product -> store.add(List.of(productDocument(product))),
                () -> store.delete(List.of(documentId("product:" + id))));
        } catch (RuntimeException e) {
            log.warn("Không đồng bộ được vector sản phẩm {} ({})", event.getProductId(), e.getClass().getSimpleName());
        } finally {
            indexLock.unlock();
        }
    }

    public int reindex() {
        if (!indexLock.tryLock()) {
            throw new BusinessException("CHATBOT_INDEX_BUSY", "Chỉ mục chatbot đang được cập nhật. Vui lòng thử lại.");
        }
        try {
            return rebuild();
        } catch (RuntimeException e) {
            log.warn("Không dựng lại được chỉ mục chatbot ({})", e.getClass().getSimpleName());
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "CHATBOT_INDEX_FAILED",
                "Không thể dựng lại chỉ mục chatbot. Vui lòng thử lại sau.");
        } finally {
            indexLock.unlock();
        }
    }

    private int rebuild() {
        var documents = new ArrayList<>(knowledgeDocuments());
        products.findPublicProducts().stream().map(ProductIndexer::productDocument).forEach(documents::add);
        var ids = documents.stream().map(Document::getId).collect(Collectors.toSet());
        transactions.executeWithoutResult(status -> {
            store.add(documents);
            var obsolete = jdbc.queryForList("SELECT id FROM vector_store", String.class).stream()
                .filter(id -> !ids.contains(id)).toList();
            if (!obsolete.isEmpty()) {
                store.delete(obsolete);
            }
        });
        return documents.size();
    }

    private static Document productDocument(Product product) {
        var metadata = new HashMap<String, Object>();
        metadata.put("type", "PRODUCT");
        metadata.put("productId", product.getId());
        metadata.put("status", product.getStatus().name());
        metadata.put("categoryId", product.getCategory().getId());
        metadata.put("price", product.getPrice());
        if (product.getLayout() != null) metadata.put("layout", product.getLayout());
        if (product.getSwitchType() != null) metadata.put("switchType", product.getSwitchType());
        if (product.getBrand() != null) metadata.put("brand", product.getBrand());
        String text = Stream.of(product.getName(), product.getBrand(), product.getCategory().getName(),
                product.getLayout(), product.getSwitchType(), product.getDescription(), product.getSpecs())
            .filter(value -> value != null && !value.isBlank()).collect(Collectors.joining("\n"));
        int end = text.offsetByCodePoints(0, Math.min(6_000, text.codePointCount(0, text.length())));
        return new Document(documentId("product:" + product.getId()), text.substring(0, end), metadata);
    }

    private static List<Document> knowledgeDocuments() {
        var resolver = new PathMatchingResourcePatternResolver();
        try {
            var files = resolver.getResources("classpath*:chatbot/knowledge/*.md");
            var documents = new ArrayList<Document>();
            for (var file : files) {
                String markdown = file.getContentAsString(StandardCharsets.UTF_8);
                Arrays.stream(markdown.split("(?m)(?=^## )")).map(String::strip)
                    .filter(section -> section.startsWith("## ")).forEach(section -> {
                        String title = section.lines().findFirst().orElseThrow();
                        documents.add(new Document(documentId("knowledge:" + file.getFilename() + ":" + title),
                            section, Map.of("type", "KNOWLEDGE")));
                    });
            }
            return documents;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String documentId(String key) {
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
    }
}

package vn.edu.iuh.keymech.ai;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.ai.google.genai.embedding.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mariadb.MariaDBVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;
import vn.edu.iuh.keymech.repository.ChatProductRepository;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "app.chatbot.mode", havingValue = "ai")
@Conditional(ChatbotAiConfig.GeminiKeyConfigured.class)
class ChatbotAiConfig {

    static class GeminiKeyConfigured implements org.springframework.context.annotation.Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return StringUtils.hasText(context.getEnvironment().getProperty("spring.ai.google.genai.api-key"));
        }
    }

    @Bean
    Client geminiClient(@Value("${spring.ai.google.genai.api-key}") String key,
                        @Value("${app.chatbot.timeout-seconds}") int timeoutSeconds) {
        return Client.builder().apiKey(key).vertexAI(false)
            .httpOptions(HttpOptions.builder().timeout(Math.multiplyExact(timeoutSeconds, 1_000))
                .retryOptions(HttpRetryOptions.builder().attempts(1)).build())
            .build();
    }

    @Bean
    ChatClient chatbotChatClient(Client geminiClient,
                                @Value("${spring.ai.google.genai.chat.model}") String model,
                                @Value("${spring.ai.google.genai.chat.temperature}") double temperature) {
        var options = GoogleGenAiChatOptions.builder().model(model).temperature(temperature)
            .useCachedContent(false).maxOutputTokens(320).responseMimeType("application/json");
        if (model.startsWith("gemini-3") && model.contains("-flash")) {
            options.thinkingLevel(GoogleGenAiThinkingLevel.MINIMAL);
        }
        return ChatClient.create(GoogleGenAiChatModel.builder().genAiClient(geminiClient)
            .options(options.build()).retryTemplate(noRetry()).build());
    }

    @Bean
    VectorStore chatbotVectorStore(JdbcTemplate jdbc, Client geminiClient,
                                   @Value("${spring.ai.google.genai.embedding.text.model}") String model,
                                   @Value("${spring.ai.google.genai.embedding.text.dimensions}") int dimensions) {
        var connection = GoogleGenAiEmbeddingConnectionDetails.builder().genAiClient(geminiClient).build();
        var options = GoogleGenAiTextEmbeddingOptions.builder().model(model).dimensions(dimensions).build();
        var embedding = new GoogleGenAiTextEmbeddingModel(connection, options, noRetry());
        return MariaDBVectorStore.builder(jdbc, embedding).dimensions(dimensions)
            .distanceType(MariaDBVectorStore.MariaDBDistanceType.COSINE).initializeSchema(false).build();
    }

    private RetryTemplate noRetry() {
        return new RetryTemplate(RetryPolicy.builder().maxRetries(0).build());
    }

    @Bean(destroyMethod = "shutdownNow", defaultCandidate = false)
    ExecutorService chatbotAiExecutor() {
        return new ThreadPoolExecutor(0, 4, 30, TimeUnit.SECONDS, new SynchronousQueue<>(),
            Thread.ofPlatform().name("chatbot-ai-", 1).factory());
    }

    @Bean(destroyMethod = "shutdownNow", defaultCandidate = false)
    ExecutorService chatbotIndexExecutor() {
        return Executors.newSingleThreadExecutor(Thread.ofPlatform().name("chatbot-index").factory());
    }

    @Bean
    @Primary
    AiChatbotService aiChatbotService(RuleBasedChatbotService rule, ChatProductRepository products,
                                      VectorStore store, ChatClient chatClient,
                                      @Qualifier("chatbotAiExecutor") ExecutorService executor,
                                      @Value("${app.chatbot.timeout-seconds}") int timeoutSeconds) {
        return new AiChatbotService(rule, products, store, chatClient, executor, timeoutSeconds);
    }

    @Bean
    ProductIndexer productIndexer(ChatProductRepository products, VectorStore store, JdbcTemplate jdbc,
                                  PlatformTransactionManager transactions) {
        return new ProductIndexer(products, store, jdbc, transactions);
    }
}

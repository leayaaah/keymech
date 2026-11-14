package vn.edu.iuh.keymech.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String corsOrigin, String uploadDir, Chatbot chatbot) {
    public record Chatbot(String mode, boolean personalization, int timeoutSeconds,
                          int rateLimitPerMinute, int rateLimitPerHourGlobal) {}
}

package vn.edu.iuh.keymech.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("loginUrl", "/login");
            context.setVariable("shopUrl", "/");
            context.setVariable("customUrl", "/custom");

            String html = templateEngine.process("mail/welcome", context);
            sendHtmlEmail(toEmail, "Chào mừng bạn đến với cửa hàng bàn phím cơ", html);
        } catch (Exception e) {
            log.warn("Không thể gửi email chào mừng tới {}: {}", maskEmail(toEmail),
                e.getClass().getSimpleName());
        }
    }

    @Async
    public void sendResetPasswordEmail(String toEmail, String code) {
        try {
            Context context = new Context();
            context.setVariable("code", code);
            context.setVariable("expiryMinutes", 10);

            String html = templateEngine.process("mail/reset-code", context);
            sendHtmlEmail(toEmail, "Mã xác thực đặt lại mật khẩu", html);
        } catch (Exception e) {
            log.warn("Không thể gửi email mã xác thực tới {}: {}", maskEmail(toEmail),
                e.getClass().getSimpleName());
        }
    }

    @Async
    public void sendOrderConfirmationEmail(String toEmail, OrderConfirmationData data) {
        try {
            Context context = new Context();
            context.setVariable("orderCode", data.orderCode());
            context.setVariable("orderDate", data.orderDate());
            context.setVariable("receiverName", data.receiverName());
            context.setVariable("receiverPhone", data.receiverPhone());
            context.setVariable("shippingAddress", data.shippingAddress());
            context.setVariable("paymentMethod", data.paymentMethod());
            context.setVariable("items", data.items());
            context.setVariable("totalAmount", data.totalAmount());

            String html = templateEngine.process("mail/order", context);
            sendHtmlEmail(toEmail, "Xác nhận đơn hàng " + data.orderCode(), html);
        } catch (Exception e) {
            log.warn("Không thể gửi email xác nhận đơn hàng tới {}: {}", maskEmail(toEmail),
                e.getClass().getSimpleName());
        }
    }

    @Async
    public void sendCustomStatusEmail(String toEmail, CustomStatusData data) {
        try {
            Context context = new Context();
            context.setVariable("requestCode", data.requestCode());
            context.setVariable("newStatus", data.newStatus());
            context.setVariable("note", data.note());

            String html = templateEngine.process("mail/custom-status", context);
            sendHtmlEmail(toEmail, "Cập nhật yêu cầu Custom " + data.requestCode(), html);
        } catch (Exception e) {
            log.warn("Không thể gửi email cập nhật Custom tới {}: {}", maskEmail(toEmail),
                e.getClass().getSimpleName());
        }
    }

    private void sendHtmlEmail(String to, String subject, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 2) {
            return local.charAt(0) + "**@" + domain;
        }
        return local.substring(0, 2) + "***" + local.substring(local.length() - 1) + "@" + domain;
    }

    public record OrderConfirmationData(
        String orderCode,
        String orderDate,
        String receiverName,
        String receiverPhone,
        String shippingAddress,
        String paymentMethod,
        java.util.List<OrderItemData> items,
        String totalAmount
    ) {}

    public record OrderItemData(
        String productName,
        int quantity,
        String unitPrice,
        String lineTotal
    ) {}

    public record CustomStatusData(
        String requestCode,
        String newStatus,
        String note
    ) {}
}
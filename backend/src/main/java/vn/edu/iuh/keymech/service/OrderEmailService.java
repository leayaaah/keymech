package vn.edu.iuh.keymech.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
public class OrderEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templates;
    private final String from;

    public OrderEmailService(JavaMailSender mailSender, TemplateEngine templates,
                             @Value("${spring.mail.username:}") String username) {
        this.mailSender = mailSender;
        this.templates = templates;
        this.from = username.isBlank() ? "noreply@keymech.local" : username;
    }

    @Async
    public void sendConfirmation(OrderService.PlacedOrder order) {
        try {
            Context context = new Context();
            context.setVariable("order", order);
            String html = templates.process("mail/order-confirmation", context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(order.toEmail());
            helper.setSubject("Xác nhận đơn hàng " + order.response().orderCode());
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Không thể gửi email xác nhận đơn {}", order.response().orderCode(), e);
        }
    }
}

package com.tuktak.inventory.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String brevoApiKey;

    @Value("${mail.from}")
    private String fromEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendOrderPlacedEmail(String toEmail, String customerName, String orderNumber, double totalAmount) {
        try {
            Map<String, Object> body = buildEmailBody(
                    toEmail, customerName,
                    "Order Placed Successfully - " + orderNumber,
                    buildOrderPlacedHtml(customerName, orderNumber, totalAmount)
            );
            sendEmail(body);
            log.info("Order placed email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send order placed email: {}", e.getMessage());
        }
    }

    @Async
    public void sendOrderConfirmedEmail(String toEmail, String customerName, String orderNumber) {
        try {
            Map<String, Object> body = buildEmailBody(
                    toEmail, customerName,
                    "Order Confirmed - " + orderNumber,
                    buildOrderConfirmedHtml(customerName, orderNumber)
            );
            sendEmail(body);
            log.info("Order confirmed email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send order confirmed email: {}", e.getMessage());
        }
    }

    private void sendEmail(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", request, String.class);
    }

    private Map<String, Object> buildEmailBody(String toEmail, String toName, String subject, String htmlContent) {
        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("name", "TukTak Deal", "email", fromEmail));
        body.put("to", List.of(Map.of("email", toEmail, "name", toName)));
        body.put("subject", subject);
        body.put("htmlContent", htmlContent);
        return body;
    }

    private String buildOrderPlacedHtml(String name, String orderNumber, double total) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #3d2314; padding: 20px; text-align: center;">
                    <h1 style="color: white; margin: 0;">TukTak Deal</h1>
                </div>
                <div style="padding: 30px; background-color: #f9f9f9;">
                    <h2 style="color: #3d2314;">Order Placed Successfully! 🎉</h2>
                    <p>Dear <strong>%s</strong>,</p>
                    <p>Thank you for your order! We have received your order and will process it shortly.</p>
                    <div style="background-color: white; padding: 20px; border-radius: 10px; margin: 20px 0;">
                        <p><strong>Order Number:</strong> %s</p>
                        <p><strong>Total Amount:</strong> ৳%.0f</p>
                        <p><strong>Status:</strong> PENDING</p>
                    </div>
                    <p>We will notify you once your order is confirmed.</p>
                    <p style="color: #666;">Thank you for shopping with TukTak Deal!</p>
                </div>
            </div>
            """.formatted(name, orderNumber, total);
    }

    private String buildOrderConfirmedHtml(String name, String orderNumber) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #3d2314; padding: 20px; text-align: center;">
                    <h1 style="color: white; margin: 0;">TukTak Deal</h1>
                </div>
                <div style="padding: 30px; background-color: #f9f9f9;">
                    <h2 style="color: #28a745;">Order Confirmed! ✅</h2>
                    <p>Dear <strong>%s</strong>,</p>
                    <p>Great news! Your order has been confirmed and is being processed.</p>
                    <div style="background-color: white; padding: 20px; border-radius: 10px; margin: 20px 0;">
                        <p><strong>Order Number:</strong> %s</p>
                        <p><strong>Status:</strong> CONFIRMED</p>
                    </div>
                    <p>We will contact you shortly for delivery details.</p>
                    <p style="color: #666;">Thank you for shopping with TukTak Deal!</p>
                </div>
            </div>
            """.formatted(name, orderNumber);
    }
}
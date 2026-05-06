package com.tuktak.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOrderPlacedEmail(String toEmail, String customerName, String orderNumber, double totalAmount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Order Placed Successfully - " + orderNumber);
            helper.setText(buildOrderPlacedHtml(customerName, orderNumber, totalAmount), true);
            mailSender.send(message);
            log.info("Order placed email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send order placed email: {}", e.getMessage());
        }
    }

    @Async
    public void sendOrderConfirmedEmail(String toEmail, String customerName, String orderNumber) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Order Confirmed - " + orderNumber);
            helper.setText(buildOrderConfirmedHtml(customerName, orderNumber), true);
            mailSender.send(message);
            log.info("Order confirmed email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send order confirmed email: {}", e.getMessage());
        }
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
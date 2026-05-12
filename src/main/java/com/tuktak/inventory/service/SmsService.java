package com.tuktak.inventory.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SmsService {

    @Value("${rtcom.api-url}")
    private String apiUrl;

    @Value("${rtcom.acode}")
    private String acode;

    @Value("${rtcom.api-key}")
    private String apiKey;

    @Value("${rtcom.sender-id}")
    private String senderId;

    @Value("${admin.phone}")
    private String adminPhone;

    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ Called when order is PLACED (PENDING)
    public void sendOrderPlacedSms(String customerPhone, String customerName,
                                   String orderNumber, double totalAmount) {

        String customerMsg = String.format(
                "Dear %s, your TukTak Deal order %s has been placed successfully! " +
                        "Total: %.0f BDT. We will confirm it shortly. Thank you!",
                customerName, orderNumber, totalAmount
        );

        String adminMsg = String.format(
                "New Order Alert! Order %s placed by %s. Phone: %s. Total: %.0f BDT. Please confirm.",
                orderNumber, customerName, customerPhone, totalAmount
        );

        sendSms(customerPhone, customerMsg);
        sendSms(adminPhone, adminMsg);
    }

    // ✅ Called when order is CONFIRMED
    public void sendOrderConfirmedSms(String customerPhone, String customerName,
                                      String orderNumber) {

        String customerMsg = String.format(
                "Dear %s, great news! Your TukTak Deal order %s has been CONFIRMED. " +
                        "We are preparing your jewelry. Thank you for shopping with us!",
                customerName, orderNumber
        );

        sendSms(customerPhone, customerMsg);
    }

    // ✅ Internal method — sends SMS via RT Communications API
    private void sendSms(String toPhone, String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("acode", acode);
            body.put("api_key", apiKey);
            body.put("senderid", senderId);
            body.put("type", "text");
            body.put("msg", message);
            body.put("contacts", toPhone);
            body.put("transactionType", "T");
            body.put("contentID", "");

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl, request, String.class
            );

            log.info("✅ SMS sent to {} | Response: {}", toPhone, response.getBody());

        } catch (Exception e) {
            log.error("❌ SMS failed to {}: {}", toPhone, e.getMessage());
        }
    }
}
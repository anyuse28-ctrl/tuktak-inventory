package com.tuktak.inventory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuktak.inventory.dto.ChatMessage;
import com.tuktak.inventory.dto.ChatResponse;
import com.tuktak.inventory.entity.Product;
import com.tuktak.inventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class ChatService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ChatResponse chat(ChatMessage chatMessage) {
        try {
            // 1. Get products from database
            List<Product> products = productRepository.findAll();

            StringBuilder productList = new StringBuilder();
            for (Product p : products) {
                productList.append("- ").append(p.getName())
                        .append(", Price: ").append(p.getPrice())
                        .append(", Stock: ").append(p.getStock() != null ? p.getStock().getQuantityAvailable() : 0)
                        .append("\n");
            }

            // 2. Build messages
            List<Map<String, String>> messages = new ArrayList<>();

            // System message
            messages.add(Map.of(
                    "role", "system",
                    "content", "You are a helpful shopping assistant for TukTak store. " +
                            "Help customers find products and answer questions. Be friendly and concise.\n\n" +
                            "DELIVERY CHARGES:\n" +
                            "- Inside Dhaka: ৳80\n" +
                            "- Outside Dhaka: ৳150\n\n" +
                            "PAYMENT: Cash on delivery only.\n\n" +
                            "Our products:\n" + productList
            ));

            // Add history
            if (chatMessage.getHistory() != null) {
                for (ChatMessage.ChatHistory h : chatMessage.getHistory()) {
                    messages.add(Map.of(
                            "role", "user".equals(h.getRole()) ? "user" : "assistant",
                            "content", h.getContent()
                    ));
                }
            }

            // Add current message
            messages.add(Map.of(
                    "role", "user",
                    "content", chatMessage.getMessage()
            ));

            // 3. Build request body
            Map<String, Object> requestBody = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", messages,
                    "max_tokens", 1024
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 4. Call Groq API
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + groqApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            System.out.println("GROQ RESPONSE: " + responseBody);

            // 5. Parse response
            Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
            List<?> choices = (List<?>) responseMap.get("choices");
            Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
            String reply = (String) message.get("content");

            return new ChatResponse(reply, true);

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("Error: " + e.getMessage(), false);
        }
    }
}
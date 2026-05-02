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

    @Value("${gemini.api.key}")
    private String geminiApiKey;

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

            // 2. Build conversation history
            List<Map<String, Object>> contents = new ArrayList<>();

            // Add history
            if (chatMessage.getHistory() != null) {
                for (ChatMessage.ChatHistory h : chatMessage.getHistory()) {
                    contents.add(Map.of(
                            "role", h.getRole(),
                            "parts", List.of(Map.of("text", h.getContent()))
                    ));
                }
            }

            // Add system context + current message
            String fullMessage = "You are a helpful shopping assistant for TukTak store. " +
                    "Help customers find products and answer questions. Be friendly and concise.\n" +
                    "Our products:\n" + productList + "\n" +
                    "Customer question: " + chatMessage.getMessage();

            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", fullMessage))
            ));

            // 3. Build request body
            Map<String, Object> requestBody = Map.of("contents", contents);
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 4. Call Gemini API directly
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            // 5. Parse response

            System.out.println("GEMINI RESPONSE: " + responseBody);
            Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
            List<?> candidates = (List<?>) responseMap.get("candidates");
            Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
            String reply = (String) firstPart.get("text");

            return new ChatResponse(reply, true);

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("Error: " + e.getMessage(), false);
        }
    }
}
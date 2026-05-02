package com.tuktak.inventory.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatMessage {
    private String message;
    private List<ChatHistory> history;

    @Data
    public static class ChatHistory {
        private String role;    // "user" or "model"
        private String content;
    }
}
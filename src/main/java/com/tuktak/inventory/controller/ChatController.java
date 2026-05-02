package com.tuktak.inventory.controller;

import com.tuktak.inventory.dto.ChatMessage;
import com.tuktak.inventory.dto.ChatResponse;
import com.tuktak.inventory.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatMessage chatMessage) {
        ChatResponse response = chatService.chat(chatMessage);
        return ResponseEntity.ok(response);
    }
}
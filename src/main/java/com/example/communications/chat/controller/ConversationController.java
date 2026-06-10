package com.example.communications.chat.controller;

import com.example.communications.chat.dto.ConversationResponse;
import com.example.communications.chat.service.ConversationService;
import com.example.communications.common.security.UserPrincipal;
import com.example.communications.message.dto.MessageResponse;
import com.example.communications.message.dto.SendMessageRequest;
import com.example.communications.message.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    public ConversationController(
            ConversationService conversationService,
            MessageService messageService
    ) {
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    @PostMapping("/private/{otherUserId}")
    public ConversationResponse getOrCreatePrivateConversation(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long otherUserId
    ) {
        return conversationService.getOrCreatePrivateConversation(
                currentUser.id(),
                otherUserId
        );
    }

    @PostMapping("/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse sendMessage(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        return messageService.sendMessage(
                currentUser.id(),
                conversationId,
                request
        );
    }

    @GetMapping("/{conversationId}/messages")
    public List<MessageResponse> history(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long conversationId
    ) {
        return messageService.getHistory(
                currentUser.id(),
                conversationId
        );
    }
}
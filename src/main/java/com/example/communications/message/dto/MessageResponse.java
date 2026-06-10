package com.example.communications.message.dto;

import com.example.communications.message.model.Message;
import com.example.communications.user.dto.UserResponse;

import java.time.Instant;

public record MessageResponse(
        Long id,
        Long conversationId,
        UserResponse sender,
        String content,
        Instant sentAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                UserResponse.from(message.getSender()),
                message.getContent(),
                message.getSentAt()
        );
    }
}
package com.example.communications.chat.dto;

import com.example.communications.chat.model.Conversation;
import com.example.communications.chat.model.ConversationType;
import com.example.communications.contact.dto.ContactResponse;

import java.time.Instant;

public record ConversationResponse(
        Long id,
        ConversationType type,
        ContactResponse otherUser,
        Instant createdAt
) {
    public static ConversationResponse from(Conversation conversation, Long currentUserId) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                ContactResponse.from(conversation.getOtherParticipant(currentUserId)),
                conversation.getCreatedAt()
        );
    }
}
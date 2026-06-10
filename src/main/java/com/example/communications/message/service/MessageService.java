package com.example.communications.message.service;

import com.example.communications.chat.model.Conversation;
import com.example.communications.chat.service.ConversationService;
import com.example.communications.message.dto.MessageResponse;
import com.example.communications.message.dto.SendMessageRequest;
import com.example.communications.message.model.Message;
import com.example.communications.message.repository.MessageRepository;
import com.example.communications.user.model.User;
import com.example.communications.user.repository.UserRepository;
import com.example.communications.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationService conversationService;

    public MessageService(
            MessageRepository messageRepository,
            UserRepository userRepository,
            ConversationService conversationService
    ) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.conversationService = conversationService;
    }

    @Transactional
    public MessageResponse sendMessage(
            Long currentUserId,
            Long conversationId,
            SendMessageRequest request
    ) {
        Conversation conversation = conversationService.findConversationForUser(
                conversationId,
                currentUserId
        );

        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Message message = new Message(
                conversation,
                sender,
                request.content().trim()
        );

        Message savedMessage = messageRepository.save(message);

        return MessageResponse.from(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getHistory(Long currentUserId, Long conversationId) {
        conversationService.findConversationForUser(conversationId, currentUserId);

        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId)
                .stream()
                .map(MessageResponse::from)
                .toList();
    }
}
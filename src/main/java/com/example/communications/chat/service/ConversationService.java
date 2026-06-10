package com.example.communications.chat.service;

import com.example.communications.chat.dto.ConversationResponse;
import com.example.communications.chat.model.Conversation;
import com.example.communications.chat.model.ConversationType;
import com.example.communications.chat.repository.ConversationRepository;
import com.example.communications.common.exception.ResourceNotFoundException;
import com.example.communications.contact.repository.ContactRequestRepository;
import com.example.communications.message.exception.MessageAccessException;
import com.example.communications.user.model.User;
import com.example.communications.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ContactRequestRepository contactRequestRepository;

    public ConversationService(
            ConversationRepository conversationRepository,
            UserRepository userRepository,
            ContactRequestRepository contactRequestRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.contactRequestRepository = contactRequestRepository;
    }

    @Transactional
    public ConversationResponse getOrCreatePrivateConversation(Long currentUserId, Long otherUserId) {
        if (currentUserId.equals(otherUserId)) {
            throw new MessageAccessException("You cannot create a conversation with yourself");
        }

        User currentUser = findUser(currentUserId);
        User otherUser = findUser(otherUserId);

        if (!contactRequestRepository.areAcceptedContacts(currentUserId, otherUserId)) {
            throw new MessageAccessException("You can only message accepted contacts");
        }

        Long firstUserId = Math.min(currentUserId, otherUserId);
        Long secondUserId = Math.max(currentUserId, otherUserId);

        Conversation conversation = conversationRepository
                .findByFirstUserIdAndSecondUserIdAndType(
                        firstUserId,
                        secondUserId,
                        ConversationType.PRIVATE
                )
                .orElseGet(() -> conversationRepository.save(
                        Conversation.privateConversation(currentUser, otherUser)
                ));

        return ConversationResponse.from(conversation, currentUserId);
    }

    @Transactional(readOnly = true)
    public Conversation findConversationForUser(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.hasParticipant(userId)) {
            throw new MessageAccessException("You do not have access to this conversation");
        }

        return conversation;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
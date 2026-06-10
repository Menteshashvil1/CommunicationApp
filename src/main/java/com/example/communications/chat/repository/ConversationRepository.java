package com.example.communications.chat.repository;

import com.example.communications.chat.model.Conversation;
import com.example.communications.chat.model.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByFirstUserIdAndSecondUserIdAndType(
            Long firstUserId,
            Long secondUserId,
            ConversationType type
    );
}
package com.example.communications.chat.model;

import com.example.communications.user.model.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "conversations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_private_conversation_users",
                        columnNames = {"first_user_id", "second_user_id", "type"}
                )
        }
)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationType type;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "first_user_id", nullable = false)
    private User firstUser;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "second_user_id", nullable = false)
    private User secondUser;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Conversation() {
    }

    private Conversation(User firstUser, User secondUser) {
        this.type = ConversationType.PRIVATE;
        this.firstUser = firstUser;
        this.secondUser = secondUser;
        this.createdAt = Instant.now();
    }

    public static Conversation privateConversation(User currentUser, User otherUser) {
        if (currentUser.getId() < otherUser.getId()) {
            return new Conversation(currentUser, otherUser);
        }

        return new Conversation(otherUser, currentUser);
    }

    public Long getId() {
        return id;
    }

    public ConversationType getType() {
        return type;
    }

    public User getFirstUser() {
        return firstUser;
    }

    public User getSecondUser() {
        return secondUser;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean hasParticipant(Long userId) {
        return firstUser.getId().equals(userId) || secondUser.getId().equals(userId);
    }

    public User getOtherParticipant(Long userId) {
        if (firstUser.getId().equals(userId)) {
            return secondUser;
        }

        return firstUser;
    }
}
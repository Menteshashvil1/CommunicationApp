package com.example.communications.contact.model;

import com.example.communications.user.model.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "contact_requests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_contact_request_sender_receiver",
                        columnNames = {"sender_id", "receiver_id"}
                )
        }
)
public class ContactRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContactRequestStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected ContactRequest() {
    }

    public ContactRequest(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = ContactRequestStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public ContactRequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void accept() {
        this.status = ContactRequestStatus.ACCEPTED;
    }

    public void reject() {
        this.status = ContactRequestStatus.REJECTED;
    }
}

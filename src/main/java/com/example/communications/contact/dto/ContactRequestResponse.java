package com.example.communications.contact.dto;

import com.example.communications.contact.model.ContactRequest;
import com.example.communications.contact.model.ContactRequestStatus;
import com.example.communications.user.dto.UserResponse;

import java.time.Instant;

public record ContactRequestResponse(
        Long id,
        UserResponse sender,
        UserResponse receiver,
        ContactRequestStatus status,
        Instant createdAt
) {
    public static ContactRequestResponse from(ContactRequest contactRequest) {
        return new ContactRequestResponse(
                contactRequest.getId(),
                UserResponse.from(contactRequest.getSender()),
                UserResponse.from(contactRequest.getReceiver()),
                contactRequest.getStatus(),
                contactRequest.getCreatedAt()
        );
    }
}

package com.example.communications.contact.service;

import com.example.communications.common.exception.ResourceNotFoundException;
import com.example.communications.contact.dto.ContactRequestResponse;
import com.example.communications.contact.dto.ContactResponse;
import com.example.communications.contact.dto.SendContactRequestRequest;
import com.example.communications.contact.exception.ContactRequestException;
import com.example.communications.contact.model.ContactRequest;
import com.example.communications.contact.model.ContactRequestStatus;
import com.example.communications.contact.repository.ContactRequestRepository;
import com.example.communications.user.model.User;
import com.example.communications.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactService {

    private final ContactRequestRepository contactRequestRepository;
    private final UserRepository userRepository;

    public ContactService(
            ContactRequestRepository contactRequestRepository,
            UserRepository userRepository
    ) {
        this.contactRequestRepository = contactRequestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ContactRequestResponse sendRequest(
            Long senderId,
            SendContactRequestRequest request
    ) {
        User sender = findUser(senderId);
        String receiverEmail = request.receiverEmail().trim().toLowerCase();
        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + receiverEmail));

        if (sender.getId().equals(receiver.getId())) {
            throw new ContactRequestException("You cannot send a contact request to yourself");
        }

        if (contactRequestRepository.existsBetweenUsers(sender.getId(), receiver.getId())) {
            throw new ContactRequestException("A contact request already exists between these users");
        }

        ContactRequest contactRequest = new ContactRequest(sender, receiver);
        ContactRequest savedContactRequest = contactRequestRepository.save(contactRequest);

        return ContactRequestResponse.from(savedContactRequest);
    }

    @Transactional(readOnly = true)
    public List<ContactRequestResponse> getIncomingRequests(Long receiverId) {
        return contactRequestRepository
                .findByReceiverIdAndStatusOrderByCreatedAtDesc(receiverId, ContactRequestStatus.PENDING)
                .stream()
                .map(ContactRequestResponse::from)
                .toList();
    }

    @Transactional
    public ContactRequestResponse acceptRequest(Long receiverId, Long requestId) {
        ContactRequest contactRequest = contactRequestRepository
                .findByIdAndReceiverIdAndStatus(requestId, receiverId, ContactRequestStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Pending contact request not found"));

        contactRequest.accept();

        return ContactRequestResponse.from(contactRequest);
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getContacts(Long userId) {
        return contactRequestRepository.findAcceptedContactsForUser(userId)
                .stream()
                .map(contactRequest -> getOtherUser(contactRequest, userId))
                .map(ContactResponse::from)
                .toList();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private User getOtherUser(ContactRequest contactRequest, Long userId) {
        if (contactRequest.getSender().getId().equals(userId)) {
            return contactRequest.getReceiver();
        }

        return contactRequest.getSender();
    }
}

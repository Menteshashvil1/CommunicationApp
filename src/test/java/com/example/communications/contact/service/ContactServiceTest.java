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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContactServiceTest {

    private final ContactRequestRepository contactRequestRepository = mock(ContactRequestRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ContactService contactService = new ContactService(contactRequestRepository, userRepository);

    @Test
    void sendRequestCreatesPendingRequest() {
        User sender = user(1L, "sender@example.com", "Sender");
        User receiver = user(2L, "receiver@example.com", "Receiver");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiver));
        when(contactRequestRepository.existsBetweenUsers(1L, 2L)).thenReturn(false);
        when(contactRequestRepository.save(any(ContactRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ContactRequestResponse response = contactService.sendRequest(
                1L,
                new SendContactRequestRequest("Receiver@Example.com")
        );

        assertEquals("sender@example.com", response.sender().email());
        assertEquals("receiver@example.com", response.receiver().email());
        assertEquals(ContactRequestStatus.PENDING, response.status());

        verify(contactRequestRepository).save(any(ContactRequest.class));
    }

    @Test
    void sendRequestRejectsSelfRequest() {
        User sender = user(1L, "student@example.com", "Student");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(sender));

        assertThrows(
                ContactRequestException.class,
                () -> contactService.sendRequest(1L, new SendContactRequestRequest("student@example.com"))
        );

        verify(contactRequestRepository, never()).save(any(ContactRequest.class));
    }

    @Test
    void sendRequestRejectsDuplicateRelationship() {
        User sender = user(1L, "sender@example.com", "Sender");
        User receiver = user(2L, "receiver@example.com", "Receiver");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiver));
        when(contactRequestRepository.existsBetweenUsers(1L, 2L)).thenReturn(true);

        assertThrows(
                ContactRequestException.class,
                () -> contactService.sendRequest(1L, new SendContactRequestRequest("receiver@example.com"))
        );

        verify(contactRequestRepository, never()).save(any(ContactRequest.class));
    }

    @Test
    void acceptRequestChangesStatusToAccepted() {
        User sender = user(1L, "sender@example.com", "Sender");
        User receiver = user(2L, "receiver@example.com", "Receiver");
        ContactRequest contactRequest = new ContactRequest(sender, receiver);

        when(contactRequestRepository.findByIdAndReceiverIdAndStatus(
                10L,
                2L,
                ContactRequestStatus.PENDING
        )).thenReturn(Optional.of(contactRequest));

        ContactRequestResponse response = contactService.acceptRequest(2L, 10L);

        assertEquals(ContactRequestStatus.ACCEPTED, response.status());
        assertEquals(ContactRequestStatus.ACCEPTED, contactRequest.getStatus());
    }

    @Test
    void acceptRequestRejectsMissingPendingRequest() {
        when(contactRequestRepository.findByIdAndReceiverIdAndStatus(
                10L,
                2L,
                ContactRequestStatus.PENDING
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> contactService.acceptRequest(2L, 10L)
        );
    }

    @Test
    void getContactsReturnsOtherUserFromAcceptedRequests() {
        User currentUser = user(1L, "student@example.com", "Student");
        User friend = user(2L, "friend@example.com", "Friend");

        when(contactRequestRepository.findAcceptedContactsForUser(1L))
                .thenReturn(List.of(new ContactRequest(currentUser, friend)));

        List<ContactResponse> contacts = contactService.getContacts(1L);

        assertEquals(1, contacts.size());
        assertEquals("friend@example.com", contacts.getFirst().email());
    }

    private User user(Long id, String email, String displayName) {
        User user = new User(email, "password-hash", displayName);
        user.setId(id);

        return user;
    }
}

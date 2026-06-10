package com.example.communications.contact.controller;

import com.example.communications.common.security.UserPrincipal;
import com.example.communications.contact.dto.ContactRequestResponse;
import com.example.communications.contact.dto.ContactResponse;
import com.example.communications.contact.dto.SendContactRequestRequest;
import com.example.communications.contact.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ContactRequestResponse sendRequest(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SendContactRequestRequest request
    ) {
        return contactService.sendRequest(currentUser.id(), request);
    }

    @GetMapping("/requests/incoming")
    public List<ContactRequestResponse> incomingRequests(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return contactService.getIncomingRequests(currentUser.id());
    }

    @PostMapping("/requests/{requestId}/accept")
    public ContactRequestResponse acceptRequest(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long requestId
    ) {
        return contactService.acceptRequest(currentUser.id(), requestId);
    }

    @GetMapping
    public List<ContactResponse> contacts(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return contactService.getContacts(currentUser.id());
    }
}

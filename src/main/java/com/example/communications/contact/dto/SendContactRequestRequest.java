package com.example.communications.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendContactRequestRequest(
        @NotBlank
        @Email
        @Size(max = 320)
        String receiverEmail
) {
}

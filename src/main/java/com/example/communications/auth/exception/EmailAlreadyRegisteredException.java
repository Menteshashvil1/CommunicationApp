package com.example.communications.auth.exception;


public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException(String email) {
        super("Email is already registered: " + email);
    }
}

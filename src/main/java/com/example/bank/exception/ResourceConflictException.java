package com.example.bank.exception;

public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }
}
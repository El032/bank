package com.example.bank.exception;

public class InvalidAccountStatusTransitionException extends RuntimeException {

    public InvalidAccountStatusTransitionException(String message) {
        super(message);
    }
}
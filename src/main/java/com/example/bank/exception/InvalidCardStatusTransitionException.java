package com.example.bank.exception;

public class InvalidCardStatusTransitionException
        extends RuntimeException {

    public InvalidCardStatusTransitionException(String message) {
        super(message);
    }
}
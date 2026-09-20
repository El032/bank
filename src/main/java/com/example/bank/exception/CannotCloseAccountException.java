package com.example.bank.exception;

public class CannotCloseAccountException extends RuntimeException {

    public CannotCloseAccountException(String message) {
        super(message);
    }
}
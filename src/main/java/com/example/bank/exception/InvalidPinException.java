package com.example.bank.exception;

public class InvalidPinException extends RuntimeException {

    public InvalidPinException() {
        super("Неверный PIN-код");
    }
}
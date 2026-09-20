package com.example.bank.exception;


public class TransferException extends RuntimeException {
    public TransferException(String message) {
        super(message);
    }
}

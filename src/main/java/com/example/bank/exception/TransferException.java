package com.example.bank.exception;

import java.math.BigDecimal;

public class TransferException extends RuntimeException {
    public TransferException(String message) {
        super(message);
    }
}

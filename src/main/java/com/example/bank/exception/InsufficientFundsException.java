package com.example.bank.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(BigDecimal required, BigDecimal available) {
        super("Недостаточно средств. Нужно: " + required +
                ", на счёте: " + available);
    }
}
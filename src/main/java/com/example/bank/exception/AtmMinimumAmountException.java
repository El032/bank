package com.example.bank.exception;

import java.math.BigDecimal;

public class AtmMinimumAmountException extends RuntimeException {

    public AtmMinimumAmountException(BigDecimal minimumAmount) {
        super(
                "Минимальная сумма операции через ATM: "
                        + minimumAmount
                        + " ₽"
        );
    }
}
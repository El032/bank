package com.example.bank.exception;

import java.math.BigDecimal;

public class TransferMinimumAmountException extends RuntimeException {

    public TransferMinimumAmountException(BigDecimal minimumAmount) {
        super(
                "Минимальная сумма перевода: "
                        + minimumAmount
                        + " ₽"
        );
    }
}
package com.example.bank.exception;

import java.math.BigDecimal;

public class CreditLimitExceededException extends RuntimeException {

    public CreditLimitExceededException(
            BigDecimal requestedAmount,
            BigDecimal currentBalance,
            BigDecimal creditLimit
    ) {
        super(
                "Превышен кредитный лимит. " +
                        "Запрошено: " + requestedAmount +
                        ", текущая задолженность: " + currentBalance +
                        ", кредитный лимит: " + creditLimit
        );
    }
}
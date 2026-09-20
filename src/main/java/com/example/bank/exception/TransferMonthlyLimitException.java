package com.example.bank.exception;

import java.math.BigDecimal;

public class TransferMonthlyLimitException extends RuntimeException {

    public TransferMonthlyLimitException(
            BigDecimal monthlyLimit,
            BigDecimal transferredThisMonth,
            BigDecimal requestedAmount
    ) {
        super(
                "Превышен месячный лимит переводов. "
                        + "Лимит: " + monthlyLimit
                        + ", отправлено в этом месяце: " + transferredThisMonth
                        + ", запрошено: " + requestedAmount
        );
    }
}
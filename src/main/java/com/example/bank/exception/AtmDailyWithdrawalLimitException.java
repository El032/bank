package com.example.bank.exception;

import java.math.BigDecimal;

public class AtmDailyWithdrawalLimitException extends RuntimeException {

    public AtmDailyWithdrawalLimitException(
            BigDecimal dailyLimit,
            BigDecimal withdrawnToday,
            BigDecimal requestedAmount
    ) {
        super(
                "Превышен суточный лимит ATM. "
                        + "Лимит: " + dailyLimit
                        + ", снято сегодня: " + withdrawnToday
                        + ", запрошено: " + requestedAmount
        );
    }
}
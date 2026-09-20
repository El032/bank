package com.example.bank.exception;

import java.math.BigDecimal;

public class CreditPaymentExceededException extends RuntimeException {

    public CreditPaymentExceededException(
            BigDecimal paymentAmount,
            BigDecimal currentDebt
    ) {
        super(
                "Сумма платежа " + paymentAmount +
                        " превышает текущую задолженность " + currentDebt
        );
    }
}
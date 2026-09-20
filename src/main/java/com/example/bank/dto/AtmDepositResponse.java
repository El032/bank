package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(
        description = "Результат успешного пополнения счёта через ATM"
)
public class AtmDepositResponse {

    @Schema(
            description = "Сообщение о результате операции",
            example = "Счёт успешно пополнен"
    )
    private String message;

    @Schema(
            description = "Сумма, на которую был пополнен счёт",
            example = "5000.00",
            type = "number"
    )
    private BigDecimal depositedAmount;

    public AtmDepositResponse() {
    }

    public AtmDepositResponse(
            String message,
            BigDecimal depositedAmount
    ) {
        this.message = message;
        this.depositedAmount = depositedAmount;
    }

    public String getMessage() {
        return message;
    }

    public BigDecimal getDepositedAmount() {
        return depositedAmount;
    }
}

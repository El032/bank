package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(
        description = "Запрос на пополнение банковского счёта через ATM"
)
public class AtmDepositRequest {

    @Schema(
            description = "Сумма пополнения. Значение должно быть больше 0. " +
                    "Минимальная сумма операции через ATM — 100 ₽.",
            example = "5000.00",
            type = "number"
    )
    @NotNull(message = "Сумма пополнения обязательна")
    @DecimalMin(
            value = "0.01",
            message = "Сумма пополнения должна быть больше 0"
    )
    public BigDecimal amount;

    public AtmDepositRequest() {
    }

    public AtmDepositRequest(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }
}


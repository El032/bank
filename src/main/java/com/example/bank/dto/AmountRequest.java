package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Запрос на пополнение или снятие средств со счёта")
public class AmountRequest {

    @Schema(
            description = "Сумма операции. Должна быть больше нуля.",
            example = "1000.00",
            minimum = "0.01"
    )
    @Positive(message = "Сумма операции должна быть положительной!")
    @NotNull(message = "Сумма операции обязательна")
    private BigDecimal amount;

    public AmountRequest() {}

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
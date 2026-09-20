package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(
        description = "Запрос на снятие средств через ATM"
)
public class AtmWithdrawRequest {

    @Schema(
            description = "Сумма снятия. Значение должно быть больше 0. " +
                    "Минимальная сумма операции через ATM — 100 ₽.",
            example = "5000.00",
            type = "number",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Сумма снятия обязательна")
    @DecimalMin(
            value = "0.01",
            message = "Сумма снятия должна быть больше 0"
    )
    private BigDecimal amount;

    public AtmWithdrawRequest() {
    }

    public AtmWithdrawRequest(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}

package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(
        description = "Текущий баланс банковского счёта, доступный через ATM"
)
public class AtmBalanceResponse {

    @Schema(
            description = "Текущий баланс счёта",
            example = "15000.00",
            type = "number"
    )
    private BigDecimal balance;

    public AtmBalanceResponse() {
    }

    public AtmBalanceResponse(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}

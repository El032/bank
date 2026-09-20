package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Запрос на перевод денежных средств между банковскими счетами")
public class TransferRequest {

    @Schema(
            description = "Идентификатор счёта отправителя",
            example = "1001"
    )
    @NotNull(message = "ID счёта-отправителя обязателен")
    private Long fromAccountId;

    @Schema(
            description = "Идентификатор счёта получателя",
            example = "1002"
    )
    @NotNull(message = "ID счёта-получателя обязателен")
    private Long toAccountId;

    @Schema(
            description = "Сумма перевода. Должна быть положительной",
            example = "1500.50",
            minimum = "0"
    )
    @NotNull
    @Positive(message = "Сумма перевода должна быть положительной")
    private BigDecimal amount;

    public TransferRequest() {}

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

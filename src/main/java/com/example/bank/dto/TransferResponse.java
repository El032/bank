package com.example.bank.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Информация о переводе денежных средств")
public class TransferResponse {

    @Schema(
            description = "Уникальный идентификатор перевода",
            example = "5001"
    )
    private Long id;

    @Schema(
            description = "Идентификатор счёта отправителя",
            example = "1001"
    )
    private Long fromAccountId;

    @Schema(
            description = "Идентификатор счёта получателя",
            example = "1002"
    )
    private Long toAccountId;

    @Schema(
            description = "Сумма перевода",
            example = "1500.50"
    )
    private BigDecimal amount;

    @Schema(
            description = "Статус перевода",
            example = "COMPLETED"
    )
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(
            description = "Дата и время создания перевода",
            example = "2026-08-17 15:30:45"
    )
    private LocalDateTime createdAt;

    public TransferResponse() {}

    public TransferResponse(
            Long id,
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount,
            String status,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

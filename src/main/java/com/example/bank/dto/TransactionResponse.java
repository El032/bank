package com.example.bank.dto;

import com.example.bank.model.TransactionSource;
import com.example.bank.model.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Информация о банковской транзакции")
public class TransactionResponse {

    @Schema(
            description = "Уникальный идентификатор транзакции",
            example = "1001"
    )
    private Long id;

    @Schema(
            description = "Сумма транзакции",
            example = "1500.50"
    )
    private BigDecimal amount;

    @Schema(
            description = "Тип транзакции",
            example = "DEPOSIT"
    )
    private TransactionType type;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(
            description = "Дата и время создания транзакции",
            example = "2026-08-17 15:30:45"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Источник транзакции",
            example = "ATM"
    )
    private TransactionSource source;

    public TransactionResponse(
            Long id,
            BigDecimal amount,
            TransactionType type,
            TransactionSource source,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.source = source;
        this.createdAt = createdAt;
    }

    public TransactionResponse() {}

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public TransactionSource getSource() { return source; }
}

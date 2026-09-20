package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Выписка по банковскому счету")
public class StatementResponse {

    @Schema(
            description = "Уникальный идентификатор банковского счета",
            example = "1001"
    )
    private Long accountId;

    @Schema(
            description = "Имя владельца банковского счета",
            example = "Иван Иванов"
    )
    private String owner;

    @Schema(
            description = "Текущий баланс банковского счета",
            example = "12500.50"
    )
    private BigDecimal balance;

    @Schema(
            description = "Признак активности банковского счета",
            example = "true"
    )
    private boolean active;

    @Schema(
            description = "Общее количество транзакций по счету",
            example = "25",
            minimum = "0"
    )
    private long transactionCount;

    @Schema(
            description = "Количество входящих транзакций",
            example = "15",
            minimum = "0"
    )
    private int incomingTransactionCount;

    @Schema(
            description = "Количество исходящих транзакций",
            example = "10",
            minimum = "0"
    )
    private int outgoingTransactionCount;

    public StatementResponse() {}

    public StatementResponse(
            Long accountId,
            String owner,
            BigDecimal balance,
            boolean active,
            long transactionCount,
            int incomingTransactionCount,
            int outgoingTransactionCount
    ) {
        this.accountId = accountId;
        this.owner = owner;
        this.balance = balance;
        this.active = active;
        this.transactionCount = transactionCount;
        this.incomingTransactionCount = incomingTransactionCount;
        this.outgoingTransactionCount = outgoingTransactionCount;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public boolean isActive() {
        return active;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public int getIncomingTransactionCount() {
        return incomingTransactionCount;
    }

    public int getOutgoingTransactionCount() {
        return outgoingTransactionCount;
    }
}

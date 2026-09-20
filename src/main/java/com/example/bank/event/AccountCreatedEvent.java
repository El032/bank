package com.example.bank.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountCreatedEvent {

    private Long accountId;
    private String owner;
    private BigDecimal initialBalance;
    private LocalDateTime occurredAt;

    public AccountCreatedEvent() {}

    public AccountCreatedEvent(Long accountId, String owner,
                               BigDecimal initialBalance) {
        this.accountId = accountId;
        this.owner = owner;
        this.initialBalance = initialBalance;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public BigDecimal getInitialBalance() { return initialBalance; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime t) { this.occurredAt = t; }
}
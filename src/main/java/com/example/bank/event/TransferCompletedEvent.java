package com.example.bank.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferCompletedEvent {

    private Long transferId;
    private Long fromAccountId;
    private Long toAccountId;
    private String fromOwner;
    private String toOwner;
    private BigDecimal amount;
    private LocalDateTime occurredAt;

    public TransferCompletedEvent() {}

    public TransferCompletedEvent(Long transferId, Long fromAccountId,
                                  Long toAccountId, String fromOwner,
                                  String toOwner, BigDecimal amount) {
        this.transferId = transferId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.fromOwner = fromOwner;
        this.toOwner = toOwner;
        this.amount = amount;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getTransferId() { return transferId; }
    public void setTransferId(Long transferId) { this.transferId = transferId; }
    public Long getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(Long id) { this.fromAccountId = id; }
    public Long getToAccountId() { return toAccountId; }
    public void setToAccountId(Long id) { this.toAccountId = id; }
    public String getFromOwner() { return fromOwner; }
    public void setFromOwner(String fromOwner) { this.fromOwner = fromOwner; }
    public String getToOwner() { return toOwner; }
    public void setToOwner(String toOwner) { this.toOwner = toOwner; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime t) { this.occurredAt = t; }

    @Override
    public String toString() {
        return "TransferCompletedEvent{transferId=" + transferId +
                ", amount=" + amount + ", from=" + fromOwner +
                ", to=" + toOwner + "}";
    }
}
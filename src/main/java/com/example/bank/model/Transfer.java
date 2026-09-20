package com.example.bank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id", nullable = false)
    private BankAccount fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id", nullable = false)
    private BankAccount toAccount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;


    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Transfer() {}

    public Transfer(BankAccount from, BankAccount to,
                    BigDecimal amount) {
        this.fromAccount = from;
        this.toAccount = to;
        this.amount = amount;
        this.status = "COMPLETED";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public BankAccount getFromAccount() { return fromAccount; }
    public BankAccount getToAccount() { return toAccount; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
   public String toString() {
        return "Id" + getId() +
                " from" + getFromAccount() +
                " to" + getToAccount() +
                " amount" + getAmount() +
                " status" + getStatus();
    }
}
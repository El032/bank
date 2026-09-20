package com.example.bank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // много транзакций → один счёт
    @JoinColumn(name = "account_id", nullable = false)
    private BankAccount account;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING) // хранить как строку "DEPOSIT"/"WITHDRAW", не как число
    private TransactionType type;

    @Column(name = "source", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionSource source;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;



    public Transaction() {}

    public Transaction(BankAccount account,
                       BigDecimal amount,
                       TransactionType type) {
        this.account = account;
        this.amount = amount;
        this.type = type;
        this.source = TransactionSource.API;
        this.createdAt = LocalDateTime.now();

    }

    public Transaction(BankAccount account,
                       BigDecimal amount,
                       TransactionType type,
                       TransactionSource source) {
        this.account = account;
        this.amount = amount;
        this.type = type;
        this.source = source;
        this.createdAt = LocalDateTime.now();

    }

    public Long getId() { return id; }
    public BankAccount getAccount() { return account; }
    public void setAccount(BankAccount account) { this.account = account; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public TransactionSource getSource() { return source; }
    public void setSource(TransactionSource source) { this.source = source; }
}
package com.example.bank.model;

import com.example.bank.exception.InvalidCardStatusTransitionException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bank_cards")
public class BankCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, unique = true, length = 19)
    private String cardNumber;

    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "cvv_encrypted", nullable = false)
    private String cvvEncrypted;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CardStatus status = CardStatus.ACTIVE;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private BankAccount account;

    protected BankCard() {
    }

    public BankCard(
            String cardNumber,
            String pinHash,
            LocalDate expiryDate,
            String cvvEncrypted,
            BankAccount account
    ) {
        this.cardNumber = cardNumber;
        this.pinHash = pinHash;
        this.expiryDate = expiryDate;
        this.cvvEncrypted = cvvEncrypted;
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public BankAccount getAccount() {
        return account;
    }

    public BigDecimal getAccountBalance() {
        return account.getBalance();
    }

    public LocalDate getExpiryDate() {
        return expiryDate;}

    public String getCvvEncrypted() {
        return cvvEncrypted;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public void setAccount(BankAccount account) {
        this.account = account;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void block(Long id) {
        if (status == CardStatus.CLOSED) {
            throw new InvalidCardStatusTransitionException(
                    "Карта с id = " + id +
                            ": Нельзя заблокировать закрытую карту"
            );
        }

        status = CardStatus.BLOCKED;
    }


    public void activate(Long id) {
        if (status == CardStatus.CLOSED) {
            throw new InvalidCardStatusTransitionException(
                    "Карта с id = " + id +
                            ": Нельзя активировать закрытую карту"
            );
        }

        status = CardStatus.ACTIVE;
    }

    public void close(Long id) {
        if (status == CardStatus.CLOSED) {
            throw new InvalidCardStatusTransitionException(
                    "Карта с id = " + id +
                            ": Карта уже закрыта"
            );
        }

        status = CardStatus.CLOSED;
    }

    public boolean isActive() {
        return status == CardStatus.ACTIVE;
    }
}
package com.example.bank.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "atm_sessions")
public class AtmSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private BankCard card;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "pin_verified", nullable = false)
    private boolean pinVerified = false;

    @Column(name = "card_returned", nullable = false)
    private boolean cardReturned = false;

    protected AtmSession() {
    }

    public AtmSession(BankCard card) {
        this.card = card;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public BankCard getCard() {
        return card;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPinVerified() {
        return pinVerified;
    }

    public boolean isCardReturned() {
        return cardReturned;
    }

    public void returnCard() {
        this.cardReturned = true;
    }


    public void verifyPin() {
        this.pinVerified = true;
    }

    public void close() {
        this.active = false;
    }
}
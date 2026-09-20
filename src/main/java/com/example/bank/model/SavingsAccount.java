package com.example.bank.model;

import com.example.bank.exception.InactiveAccountException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;



@Entity
@DiscriminatorValue("SAVINGS")
public class SavingsAccount extends BankAccount {
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "opened_at")
    private LocalDate openedAt;

    @Column(name = "last_interest")
    private LocalDate lastInterest;
    public SavingsAccount(String owner, BigDecimal initialBalance, BigDecimal interestRate) {
        super(owner, initialBalance);
        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException();
        }
        this.interestRate = interestRate;
        this.openedAt = LocalDate.now();
        this.lastInterest = openedAt;

    }
    protected SavingsAccount() {}

    public BigDecimal getInterestRate() {
        return interestRate;
    }
    public LocalDate getOpenedAt() {
        return openedAt;
    }
    public LocalDate getLastInterest() {
        return lastInterest;
    }

// Временно раскомментировать для тестирования
// public void setLastInterest(LocalDate lastInterest) {
//     this.lastInterest = lastInterest;
// }

    public Transaction accrueInterest() {
        LocalDate today = LocalDate.now();
        LocalDate nextInterest = lastInterest.plusMonths(1);

        if (today.isBefore(nextInterest)) {
            return null;
        }

        BigDecimal monthlyRate = interestRate
                .divide(new BigDecimal("100"), 16, RoundingMode.HALF_UP)
                .divide(new BigDecimal("12"), 16, RoundingMode.HALF_UP);

        BigDecimal interest = getBalance()
                .multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);

        applyInterest(interest);

        lastInterest = nextInterest;
        return new Transaction(this,interest,TransactionType.INTEREST);


    }

    @Override
    public void deposit(BigDecimal amount) {

        if (!isActive()) {
            throw new InactiveAccountException(getId(), getStatus());
        }

        throw new UnsupportedOperationException(
                "SavingsAccount нельзя пополнять через обычный deposit"
        );
    }



}

package com.example.bank.model;

import com.example.bank.exception.*;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("CREDIT")
public class CreditAccount extends BankAccount {

    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "first_transaction_date")
    private LocalDate firstTransactionDate;

    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;

    public CreditAccount(
            String owner,
            BigDecimal creditLimit,
            BigDecimal interestRate
    ) {
        super(owner);

        if (creditLimit == null || creditLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Кредитный лимит должен быть больше 0");
        }

        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Процентная ставка должна быть больше 0");
        }

        this.creditLimit = creditLimit;
        this.interestRate = interestRate;
    }

    protected CreditAccount() {
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public LocalDate getFirstTransactionDate() {
        return firstTransactionDate;
    }

    public LocalDate getNextPaymentDate() {
        return nextPaymentDate;
    }

    @Override
    public void deposit(BigDecimal amount) {
        if(!isActive()){
            throw new InactiveAccountException(getId(), getStatus());
        }
        if(amount == null ||amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException();
        }
        if(getBalance().compareTo(amount) < 0){
            throw new CreditPaymentExceededException(amount,getBalance());
        }
        decreaseBalance(amount);
    }

    @Override
    public void withdraw(BigDecimal amount) {
        if(!isActive()) {
            throw new InactiveAccountException(getId(), getStatus());
        }
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }
        if (getBalance().add(amount).compareTo(creditLimit) > 0) {
            throw new CreditLimitExceededException(
                    amount,
                    getBalance(),
                    creditLimit);
        }

        increaseBalance(amount);

        if (firstTransactionDate == null) {
            firstTransactionDate = LocalDate.now();
            nextPaymentDate = firstTransactionDate.plusMonths(1);
        }

    }
    public Transaction accrueInterest() {

        if (nextPaymentDate == null) {
            return null;
        }

        if (LocalDate.now().isBefore(nextPaymentDate)) {
            return null;
        }

        BigDecimal monthlyRate = interestRate
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal interest = getBalance()
                .multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);

        if (interest.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        increaseBalance(interest);

        Transaction transaction = new Transaction(
                this,
                interest,
                TransactionType.INTEREST
        );

        nextPaymentDate = nextPaymentDate.plusMonths(1);

        return transaction;
    }


    }

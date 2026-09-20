package com.example.bank.dto;

import com.example.bank.model.AccountStatus;
import com.example.bank.model.AccountType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Информация о банковском счёте")
public class AccountResponse {

    @Schema(
            description = "Уникальный идентификатор счёта",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Имя владельца счёта",
            example = "eldar"
    )
    private String owner;

    @Schema(
            description = "Текущий баланс счёта",
            example = "15000.50"
    )

    private BigDecimal balance;

    @Schema(
            description = "Номер счета",
            example = "acc 1111-2222-3333-4444"
    )
    private String accountNumber;

    @Schema(
            description = "Статус банковского счёта",
            example = "ACTIVE"
    )
    private AccountStatus status;

    @Schema(
            description = "Тип счета",
            example = "SAVING"
    )
    private AccountType accountType;

    @Schema(
            description = "Процентная ставка",
            example = "12%"
    )
    private BigDecimal interestRate;

    @Schema(
            description = "Кредитный лимит",
            example = "10000.00"
    )
    private BigDecimal creditLimit;

    @Schema(
            description = "Дата первой операции по кредиту",
            example = "2026-08-27"
    )
    private LocalDate firstTransactionDate;

    @Schema(
            description = "Дата следующего обязательного платежа",
            example = "2026-09-27"
    )
    private LocalDate nextPaymentDate;



    public AccountResponse() {}

    public AccountResponse(
            Long id,
            String owner,
            BigDecimal balance,
            String accountNumber,
            AccountStatus status,
            AccountType accountType,
            BigDecimal interestRate,
            BigDecimal creditLimit,
            LocalDate firstTransactionDate,
            LocalDate nextPaymentDate
    ) {
        this.id = id;
        this.owner = owner;
        this.balance = balance;
        this.accountNumber = accountNumber;
        this.status=status;
        this.accountType = accountType;
        this.interestRate = interestRate;
        this.creditLimit = creditLimit;
        this.firstTransactionDate = firstTransactionDate;
        this.nextPaymentDate = nextPaymentDate;
    }

    public Long getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public AccountType getAccountType() { return accountType;  }

    public BigDecimal getInterestRate() { return interestRate;}

    public BigDecimal getCreditLimit() { return creditLimit; }

    public LocalDate getFirstTransactionDate() { return firstTransactionDate; }

    public LocalDate getNextPaymentDate() { return nextPaymentDate; }

    public String getAccountNumber() { return accountNumber; }
}
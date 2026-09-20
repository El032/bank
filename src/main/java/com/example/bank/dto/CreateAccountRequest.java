package com.example.bank.dto;

import com.example.bank.model.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

@Schema(description = "Запрос на создание банковского счёта")
public class CreateAccountRequest {

    @Schema(
            description = "Имя владельца банковского счёта",
            example = "eldar",
            minLength = 2,
            maxLength = 32
    )
    @NotBlank(message = "Имя владельца не может быть пустым!")
    @Size(
            min = 2,
            max = 32,
            message = "Имя должно быть от 2 до 32 символов!"
    )
    private String owner;

    @Schema(
            description = "Тип банковского счета",
            example = "SAVINGS")
    @NotNull(message = "Тип счёта не может быть пустым")
    private AccountType accountType;

    @Schema(description = "баланс для сберигательного счета")
    @DecimalMin(value = "0.00", message = "Начальный баланс не может быть отрицательным")
    private BigDecimal initialBalance;

    @Schema(description = "Процентная ставка для сберегательного счета")
    @DecimalMin(value = "0.01", message = "Процентная ставка должна быть больше 0")
    private BigDecimal interestRate;

    @Schema(description = "Кредитный лимит")
    @DecimalMin(
            value = "0.01",
            message = "Кредитный лимит должен быть больше 0"
    )
    private BigDecimal creditLimit;


    public CreateAccountRequest() {
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    @JsonIgnore
    @AssertTrue(message = "Для SAVINGS необходимо указать initialBalance и interestRate, для CREDIT — creditLimit и interestRate")
    public boolean isAccountDataValid() {

        if (accountType == AccountType.SAVINGS) {
            return initialBalance != null
                    && interestRate != null;
        }

        if (accountType == AccountType.CREDIT) {
            return creditLimit != null
                    && interestRate != null;
        }

        return true;
    }
}

package com.example.bank.exception;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException() {
        super("Сумма операции должна быть Положительная!");
    }
}

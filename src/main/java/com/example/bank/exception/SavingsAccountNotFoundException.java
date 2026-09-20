package com.example.bank.exception;

public class SavingsAccountNotFoundException extends RuntimeException {
    public SavingsAccountNotFoundException(Long id) {
        super("Счёт с id " + id + " не является сберегательным");
    }
}

package com.example.bank.exception;

public class CreditAccountNotFoundException extends RuntimeException {

    public CreditAccountNotFoundException(Long id) {
        super("Кредитный счёт не найден: " + id);
    }
}
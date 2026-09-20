package com.example.bank.exception;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(Long accountId) {
        super("Карта для счёта " + accountId + " не найдена");
    }
}
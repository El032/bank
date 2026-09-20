package com.example.bank.exception;

public class AccountNotFoundException extends RuntimeException {
    private final Long id;
    public AccountNotFoundException(Long id) {
        super("Счёт не найден: " + id);
        this.id = id;
    }

    public Long getId() { return id; }
}

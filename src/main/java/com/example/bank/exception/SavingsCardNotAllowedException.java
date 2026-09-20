package com.example.bank.exception;

public class SavingsCardNotAllowedException extends RuntimeException {

    public SavingsCardNotAllowedException(Long accountId) {
        super("Для сберегательного счёта " + accountId + " карта не создаётся");
    }
}
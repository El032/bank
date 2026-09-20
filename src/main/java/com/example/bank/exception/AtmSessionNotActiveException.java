package com.example.bank.exception;

public class AtmSessionNotActiveException extends RuntimeException {

    public AtmSessionNotActiveException(Long sessionId) {
        super("ATM-сессия " + sessionId + " не существует или уже закрыта");
    }
}
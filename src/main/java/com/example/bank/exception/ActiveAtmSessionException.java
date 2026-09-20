package com.example.bank.exception;

public class ActiveAtmSessionException extends RuntimeException {

    public ActiveAtmSessionException(Long cardId) {
        super("Для карты " + cardId + " уже существует активная ATM-сессия");
    }
}
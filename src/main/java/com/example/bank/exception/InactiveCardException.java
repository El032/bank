package com.example.bank.exception;

public class InactiveCardException extends RuntimeException {

    public InactiveCardException(Long cardId) {
        super("Карта с id = " + cardId + " неактивна");
    }
}
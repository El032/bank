package com.example.bank.exception;

public class AtmPinRequiredException extends RuntimeException {

    public AtmPinRequiredException() {
        super("Сначала необходимо ввести PIN");
    }
}
package com.example.bank.exception;

public class UserInactiveException extends RuntimeException {

    public UserInactiveException() {
        super("Пользователь заблокирован");
    }

    public UserInactiveException(String username) {
        super("Пользователь " + username + " заблокирован");
    }
}
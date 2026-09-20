package com.example.bank.exception;

import com.example.bank.model.AccountStatus;

public class InactiveAccountException extends RuntimeException{

    public InactiveAccountException() {
        super("Счёт заблокирован!");
    }

    public InactiveAccountException(Long id) {
        super("Счёт с id = " + id + " заблокирован!");
    }

    public InactiveAccountException(Long id, AccountStatus status) {

        super(
                status == AccountStatus.CLOSED
                        ? "Счёт с id = " + id + " закрыт!"
                        : "Счёт с id = " + id + " заблокирован!"
        );
    }

}


package com.example.bank.dto;

import com.example.bank.model.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на обновление данных банковского счета")
public class UpdateAccountRequest {

    @Schema(
            description = "Имя владельца банковского счета. От 2 до 32 символов",
            example = "Иван Иванов",
            minLength = 2,
            maxLength = 32
    )
    @Size(
            min = 2,
            max = 32,
            message = "Имя должно быть от 2 до 32 символов!"
    )
    private String owner;

    @Schema(
            description = "Электронная почта владельца счета",
            example = "ivan@example.com",
            format = "email"
    )
    @Email
    private String email;

    @Schema(
            description = "Статус банковского счёта",
            example = "ACTIVE"
    )
    private AccountStatus status;

    public UpdateAccountRequest() {}

    public String getOwner() {

        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}


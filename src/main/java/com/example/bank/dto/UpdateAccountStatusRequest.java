package com.example.bank.dto;

import com.example.bank.model.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Запрос на изменение статуса банковского счёта")
public class UpdateAccountStatusRequest {

    @Schema(
            description = "Статус банковского счёта",
            example = "ACTIVE"
    )
    @NotNull(message = "Статус счёта обязателен")
    private AccountStatus status;

    public UpdateAccountStatusRequest() {}

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}

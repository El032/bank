package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Запрос на изменение статуса активности пользователя")
public class UpdateUserActiveRequest {

    @Schema(
            description = "Статус активности пользователя",
            example = "true"
    )
    @NotNull(message = "Статус активности обязателен")
    private Boolean active;

    public UpdateUserActiveRequest() {}

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}

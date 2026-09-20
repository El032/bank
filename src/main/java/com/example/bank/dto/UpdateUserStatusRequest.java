package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        description = "Запрос на изменение активности пользователя"
)
public class UpdateUserStatusRequest {

    @Schema(
            description = """
                    Новое состояние пользователя.

                    true  - пользователь активирован.
                    false - пользователь деактивирован.

                    Деактивация не удаляет пользователя из системы.
                    Пользователь может быть восстановлен обратно.
                    """,
            example = "false",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    private Boolean active;


    public UpdateUserStatusRequest() {
    }


    public Boolean getActive() {
        return active;
    }


    public void setActive(Boolean active) {
        this.active = active;
    }
}
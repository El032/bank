package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        description = "Запрос на ввод PIN-кода банковской карты в ATM"
)
public class AtmPinRequest {

    @Schema(
            description = "PIN-код банковской карты",
            example = "1234",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Pin обязателен")
    private String pin;

    public AtmPinRequest() {
    }

    public AtmPinRequest(String pin) {
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }
}

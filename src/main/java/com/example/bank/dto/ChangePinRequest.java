package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(
        description = "Запрос на изменение PIN-кода банковской карты"
)
public class ChangePinRequest {

    @Schema(
            description = "Текущий PIN-код карты. Для обычного пользователя должен совпадать с текущим PIN. Для администратора значение не проверяется.",
            example = "1234",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Старый PIN не должен быть пустым")
    @Pattern(
            regexp = "\\d{4}",
            message = "Старый PIN должен состоять ровно из 4 цифр"
    )
    private String oldPin;

    @Schema(
            description = "Новый PIN-код карты. Должен состоять ровно из 4 цифр.",
            example = "5678",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Новый PIN не должен быть пустым")
    @Pattern(
            regexp = "\\d{4}",
            message = "Новый PIN должен состоять ровно из 4 цифр"
    )
    private String newPin;

    public ChangePinRequest() {
    }

    public ChangePinRequest(String oldPin, String newPin) {
        this.oldPin = oldPin;
        this.newPin = newPin;
    }

    public String getOldPin() {
        return oldPin;
    }

    public String getNewPin() {
        return newPin;
    }
}

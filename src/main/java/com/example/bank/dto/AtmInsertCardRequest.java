package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        description = "Запрос на вставку банковской карты в ATM"
)
public class AtmInsertCardRequest {

    @Schema(
            description = "Уникальный идентификатор банковской карты",
            example = "7",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "ID карты обязателен")
    private Long cardId;

    public AtmInsertCardRequest() {
    }

    public AtmInsertCardRequest(Long cardId) {
        this.cardId = cardId;
    }

    public Long getCardId() {
        return cardId;
    }
}

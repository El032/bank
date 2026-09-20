package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Информация о созданной ATM-сессии"
)
public class AtmSessionResponse {

    @Schema(
            description = "Уникальный идентификатор ATM-сессии",
            example = "25"
    )
    private Long sessionId;

    @Schema(
            description = "Номер банковской карты, вставленной в ATM",
            example = "2200-1234-5678-9012"
    )
    private String cardNumber;

    @Schema(
            description = "Признак успешной аутентификации по PIN-коду",
            example = "false"
    )
    private boolean authenticated;

    public AtmSessionResponse(
            Long sessionId,
            String cardNumber,
            boolean authenticated
    ) {
        this.sessionId = sessionId;
        this.cardNumber = cardNumber;
        this.authenticated = authenticated;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}

package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Данные, возвращаемые при создании банковской карты")
public class CardCreatedResponse {

    @Schema(
            description = "Номер банковской карты",
            example = "4169-7833-7223-4685"
    )
    private String cardNumber;

    @Schema(
            description = "Срок действия карты",
            example = "08/31"
    )
    private String expiryDate;

    @Schema(
            description = "PIN-код карты. Отображается только при создании карты.",
            example = "5821",
            format = "password"
    )
    private String pin;

    @Schema(
            description = "CVV-код карты. Отображается только при создании карты.",
            example = "347",
            format = "password"
    )
    private String cvv;

    public CardCreatedResponse(
            String cardNumber,
            String expiryDate,
            String pin,
            String cvv
    ) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.pin = pin;
        this.cvv = cvv;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getPin() {
        return pin;
    }

    public String getCvv() {
        return cvv;
    }
}
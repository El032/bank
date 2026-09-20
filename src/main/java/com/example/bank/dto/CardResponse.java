package com.example.bank.dto;

import com.example.bank.model.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация о банковской карте")
public class CardResponse {

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
            description = "Статус банковской карты",
            example = "ACTIVE"
    )
    private CardStatus status;

    public CardResponse(
            String cardNumber,
            String expiryDate,
            CardStatus status
    ) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public CardStatus getStatus() {
        return status;
    }
}
package com.example.bank.dto;

import com.example.bank.model.CardStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCardStatusRequest {

    @NotNull(message = "Статус карты обязателен")
    private CardStatus status;
}
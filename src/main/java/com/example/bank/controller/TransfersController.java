package com.example.bank.controller;

import com.example.bank.dto.*;
import com.example.bank.model.Transfer;
import com.example.bank.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/transfers")
@Tag(
        name = "Переводы",
        description = "Управление банковскими переводами между счетами"
)
@SecurityRequirement(name = "Bearer Authentication")
public class TransfersController {

    private final TransferService transferService;

    public TransfersController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @accountSecurityService.isOwner(#request.fromAccountId, authentication.name)")
    @Operation(
            summary = "Создать перевод",
            description = "Переводит указанную сумму с одного банковского счёта на другой. " +
                    "Доступно администратору или владельцу счёта, с которого выполняется перевод."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Перевод успешно создан",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = TransferResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён. Пользователь не является владельцем счёта отправителя.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Счёт отправителя или получателя не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Операция невозможна: недостаточно средств или счёт заблокирован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request) {

        Transfer transfer = transferService.transfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount());

        URI location = URI.create("/transfers/" + transfer.getId());

        return ResponseEntity
                .created(location)
                .body(toResponse(transfer));
    }

    private TransferResponse toResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getFromAccount().getId(),
                transfer.getToAccount().getId(),
                transfer.getAmount(),
                transfer.getStatus(),
                transfer.getCreatedAt()
        );
    }
}
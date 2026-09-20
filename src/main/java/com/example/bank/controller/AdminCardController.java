package com.example.bank.controller;

import com.example.bank.dto.CardResponse;
import com.example.bank.dto.ErrorResponse;
import com.example.bank.dto.UpdateCardStatusRequest;
import com.example.bank.model.BankCard;
import com.example.bank.service.BankCardService;
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

@RestController
@RequestMapping("/admin/cards")
@Tag(
        name = "ADMIN - Cards",
        description = "Административные операции с банковскими картами"
)
@SecurityRequirement(name = "Bearer Authentication")
public class AdminCardController {

    private final BankCardService bankCardService;


    public AdminCardController(
            BankCardService bankCardService
    ) {
        this.bankCardService = bankCardService;
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Изменить статус банковской карты",
            description = """
                    Изменяет статус банковской карты.

                    Доступно только пользователям с ролью ADMIN.

                    Возможные статусы:
                    - ACTIVE — активная карта
                    - BLOCKED — заблокированная карта
                    - CLOSED — закрытая карта

                    Недопустимые переходы статусов будут отклонены.
                    """
    )
    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Статус карты успешно изменён",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = BankCard.class
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный запрос",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав: требуется роль ADMIN",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "422",
                    description = "Недопустимый переход статуса карты",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<CardResponse> updateStatus(
            @PathVariable Long id,

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новый статус банковской карты",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = UpdateCardStatusRequest.class
                            )
                    )
            )
            @RequestBody UpdateCardStatusRequest request
    ) {

        BankCard card = bankCardService.updateStatus(
                id,
                request.getStatus()
        );

        String expiryDate = card.getExpiryDate()
                .format(java.time.format.DateTimeFormatter.ofPattern("MM/yy"));

        CardResponse response = new CardResponse(
                card.getCardNumber(),
                expiryDate,
                card.getStatus()
        );

        return ResponseEntity.ok(response);
    }
}
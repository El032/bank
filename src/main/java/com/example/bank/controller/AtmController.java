package com.example.bank.controller;

import com.example.bank.dto.*;
import com.example.bank.model.AtmSession;
import com.example.bank.service.AtmSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/atm")
@Tag(
        name = "Банкомат",
        description = "Операции с банковской картой и счётом через ATM"
)
@SecurityRequirement(
        name = "Bearer Authentication"
)
public class AtmController {

    private final AtmSessionService atmSessionService;

    public AtmController(AtmSessionService atmSessionService) {
        this.atmSessionService = atmSessionService;
    }

    @PostMapping("/cards")
    @Operation(
            summary = "Вставить карту в банкомат",
            description = """ 
                    Создаёт новую ATM-сессию для указанной банковской карты.
                    Одна карта не может одновременно иметь несколько активных ATM-сессий. 
                    После создания сессии PIN ещё не подтверждён. """
    )
    @ApiResponses(
            { @ApiResponse( responseCode = "201",
                    description = "Карта вставлена, ATM-сессия успешно создана",
                    content = @Content( mediaType = "application/json",
                            schema = @Schema(implementation = AtmSessionResponse.class)
                    )
            ),
                    @ApiResponse( responseCode = "400",
                            description = "Некорректный идентификатор карты",
                            content = @Content( mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content( mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "404",
                            description = "Банковская карта не найдена",
                            content = @Content( mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "409",
                            description = "Для карты уже существует активная ATM-сессия",
                            content = @Content( mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            })
    public ResponseEntity<AtmSessionResponse> insertCard(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Идентификатор банковской карты",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AtmInsertCardRequest.class)
                    )
            )
            @RequestBody AtmInsertCardRequest request) {

        AtmSession session =
                atmSessionService.insertCard(request.getCardId());

        AtmSessionResponse response = new AtmSessionResponse(
                session.getId(),
                session.getCard().getCardNumber(),
                session.isPinVerified()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/sessions/{sessionId}/pin")
    @Operation(
            summary = "Ввести PIN карты",
            description = """ 
                    Проверяет PIN-код карты и авторизует текущую ATM-сессию.
                    До успешной проверки PIN операции с балансом недоступны.
                    """
    )
    @ApiResponses(
            { @ApiResponse( responseCode = "204",
                    description = "PIN успешно подтверждён" ),
                    @ApiResponse( responseCode = "400",
                            description = "Некорректный PIN",
                            content = @Content( mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content( mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "409",
                            description = "ATM-сессия не найдена или уже закрыта",
                            content = @Content( mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "422",
                            description = "Неверный PIN",
                            content = @Content( mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            })
    public ResponseEntity<Void> verifyPin(
            @Parameter(
                    description = "Идентификатор ATM-сессии",
                    example = "13",
                    required = true
            )
            @PathVariable Long sessionId,
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "PIN-код карты",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AtmPinRequest.class)
                    )
            )
            @RequestBody AtmPinRequest request) {

        atmSessionService.verifyPin(
                sessionId,
                request.getPin()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions/{sessionId}/balance")
    @Operation(
            summary = "Получить баланс",
            description = """ 
                    Возвращает текущий баланс счёта карты. 
                    Для выполнения операции PIN должен быть успешно подтверждён.
                    """ )
    @ApiResponses(
            { @ApiResponse( responseCode = "200",
                    description = "Баланс успешно получен",
                    content = @Content( mediaType = "application/json",
                            schema = @Schema(implementation = AtmBalanceResponse.class)
                    )
            ),
                    @ApiResponse( responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "409",
                            description = "ATM-сессия неактивна или карта уже возвращена",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "422",
                            description = "PIN ещё не подтверждён",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            })
    public ResponseEntity<AtmBalanceResponse> getBalance(
            @Parameter(
                    description = "Идентификатор ATM-сессии",
                    example = "13",
                    required = true
            )
            @PathVariable Long sessionId) {

        BigDecimal balance = atmSessionService.getBalance(sessionId);

        AtmBalanceResponse response =
                new AtmBalanceResponse(balance);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{sessionId}/close")
    @Operation(
            summary = "Закрыть ATM-сессию",
            description = """
                    Завершает текущую ATM-сессию.
                    Сессия становится неактивной, 
                    а карта помечается как возвращённая. 
                    После закрытия использовать эту ATM-сессию повторно нельзя. 
                    """
    )
    @ApiResponses(
            { @ApiResponse( responseCode = "204",
                    description = "ATM-сессия успешно закрыта, карта возвращена" ),
                    @ApiResponse( responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "404",
                            description = "ATM-сессия не найдена или уже закрыта",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "409",
                            description = "ATM-сессия уже неактивна",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            })
    public ResponseEntity<Void> closeSession(
            @Parameter(
                    description = "Идентификатор ATM-сессии",
                    example = "13",
                    required = true
            )
            @PathVariable Long sessionId) {

        atmSessionService.closeSession(sessionId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sessions/{sessionId}/deposit")
    @Operation(
            summary = "Пополнить счёт через ATM",
            description = """
                    Пополняет счёт, связанный с картой текущей ATM-сессии.
                    PIN должен быть подтверждён. Минимальная сумма пополнения — 100 ₽. 
                    Операция записывается в историю транзакций с источником ATM. 
                    """
    )
    @ApiResponses(
            { @ApiResponse( responseCode = "200",
                    description = "Счёт успешно пополнен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AtmDepositResponse.class)
                    )
            ),
                    @ApiResponse( responseCode = "400",
                            description = "Некорректная сумма или формат запроса",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "404",
                            description = "ATM-сессия не найдена или закрыта",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "409",
                            description = "ATM-сессия неактивна",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "422",
                            description = "PIN не подтверждён или операция запрещена бизнес-правилами счёта",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            })
    public ResponseEntity<AtmDepositResponse> deposit(
            @Parameter(
                    description = "Идентификатор ATM-сессии",
                    example = "13",
                    required = true
            )
            @PathVariable Long sessionId,
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Сумма пополнения. Минимальная сумма — 100 ₽.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AtmDepositRequest.class)
                    )
            )
            @RequestBody AtmDepositRequest request) {
        atmSessionService.deposit(
                sessionId,
                request.getAmount()
        );

        AtmDepositResponse response =
                new AtmDepositResponse(
                        "Счёт успешно пополнен",
                        request.getAmount()
                );

        return ResponseEntity.ok(response);
    }


    @PostMapping("/sessions/{sessionId}/withdraw")
    @Operation(
            summary = "Снять деньги через ATM",
            description = """
                    Снимает деньги со счёта, связанного с картой текущей ATM-сессии.
                    PIN должен быть подтверждён. 
                    Минимальная сумма снятия — 100 ₽. 
                    Суточный лимит снятия через ATM — 50 000 ₽. 
                    Операция записывается в историю транзакций с источником ATM. 
                    """
    )
    @ApiResponses(
            { @ApiResponse( responseCode = "204",
                    description = "Деньги успешно сняты" ),
                    @ApiResponse( responseCode = "400",
                            description = "Некорректная сумма или формат запроса",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "404",
                            description = "ATM-сессия не найдена или закрыта",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "409",
                            description = "ATM-сессия неактивна",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse( responseCode = "422",
                            description = "PIN не подтверждён, недостаточно средств или превышен суточный лимит ATM",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            })
    public ResponseEntity<Void> withdraw(
            @Parameter(
                    description = "Идентификатор ATM-сессии",
                    example = "13",
                    required = true
            )
            @PathVariable Long sessionId,
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Сумма снятия. Минимальная сумма — 100 ₽. Суточный лимит — 50 000 ₽.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AtmWithdrawRequest.class)
                    )
            )
             @RequestBody AtmWithdrawRequest request) {

            atmSessionService.withdraw(
                    sessionId,
                    request.getAmount()
            );

            return ResponseEntity.noContent().build();
    }
}

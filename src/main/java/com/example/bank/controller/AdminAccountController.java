package com.example.bank.controller;

import com.example.bank.dto.AccountResponse;
import com.example.bank.dto.AmountRequest;
import com.example.bank.dto.ErrorResponse;
import com.example.bank.dto.UpdateAccountStatusRequest;
import com.example.bank.model.AccountType;
import com.example.bank.model.BankAccount;
import com.example.bank.model.CreditAccount;
import com.example.bank.model.SavingsAccount;
import com.example.bank.service.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/accounts")
@Tag(
        name = "ADMIN - Accounts",
        description = "Административное управление банковскими счетами"
)
@SecurityRequirement(name = "Bearer Authentication")
public class AdminAccountController {

    private final BankAccountService service;

    public AdminAccountController(BankAccountService service) {
        this.service = service;
    }

    @PostMapping("/{id}/deposit")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Административное пополнение счёта",
            description = """
                    Выполняет административное пополнение банковского счёта.
                    Endpoint доступен только пользователям с ролью ADMIN.

                    Операция предназначена для административных корректировок
                    и не является обычным пользовательским способом пополнения счёта.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Счёт успешно пополнен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = AccountResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректная сумма или формат запроса",
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
                    description = "Счёт не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Операция запрещена бизнес-правилами счёта",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<AccountResponse> deposit(
            @Parameter(
                    description = "Идентификатор банковского счёта",
                    example = "15",
                    required = true
            )
            @PathVariable Long id,

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Сумма административного пополнения",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = AmountRequest.class
                            )
                    )
            )
            @RequestBody AmountRequest request) {

        service.deposit(id, request.getAmount());

        return ResponseEntity.ok(
                toResponseAccount(service.findById(id))
        );
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Административное снятие средств",
            description = """
                    Выполняет административное снятие средств с банковского счёта.
                    Endpoint доступен только пользователям с ролью ADMIN.

                    Операция предназначена для административных корректировок
                    и не является обычным пользовательским способом снятия средств.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Средства успешно сняты",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = AccountResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректная сумма или формат запроса",
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
                    description = "Счёт не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Недостаточно средств или операция запрещена бизнес-правилами счёта",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<AccountResponse> withdraw(
            @Parameter(
                    description = "Идентификатор банковского счёта",
                    example = "15",
                    required = true
            )
            @PathVariable Long id,

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Сумма административного снятия",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = AmountRequest.class
                            )
                    )
            )
            @RequestBody AmountRequest request) {

        service.withdraw(id, request.getAmount());

        return ResponseEntity.ok(
                toResponseAccount(service.findById(id))
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Изменить статус счёта",
            description = "Изменение статуса банковского счёта доступно только администраторам"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус счёта успешно изменён",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = AccountResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный статус"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав: требуется роль ADMIN"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Счёт не найден"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Недопустимый переход статуса"
            )
    })
    public ResponseEntity<AccountResponse> updateStatus(
            @Parameter(
                    description = "Идентификатор банковского счёта",
                    example = "15",
                    required = true
            )
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountStatusRequest request) {

        BankAccount account =
                service.updateStatus(id, request.getStatus());

        return ResponseEntity.ok(
                toResponseAccount(account)
        );
    }


    private AccountResponse toResponseAccount(BankAccount account) {

        AccountType accountType;
        BigDecimal interestRate = null;
        BigDecimal creditLimit = null;
        LocalDate firstTransactionDate = null;
        LocalDate nextPaymentDate = null;

        if (account instanceof SavingsAccount savingsAccount) {

            accountType = AccountType.SAVINGS;
            interestRate = savingsAccount.getInterestRate();

        } else if (account instanceof CreditAccount creditAccount) {

            accountType = AccountType.CREDIT;
            interestRate = creditAccount.getInterestRate();
            creditLimit = creditAccount.getCreditLimit();
            firstTransactionDate = creditAccount.getFirstTransactionDate();
            nextPaymentDate = creditAccount.getNextPaymentDate();

        } else if (account.getClass() == BankAccount.class) {

            accountType = AccountType.DEBIT;

        } else {

            throw new IllegalArgumentException(
                    "Неизвестный тип банковского счёта: "
                            + account.getClass().getSimpleName()
            );
        }

        return new AccountResponse(
                account.getId(),
                account.getOwner(),
                account.getBalance(),
                account.getAccountNumber(),
                account.getStatus(),
                accountType,
                interestRate,
                creditLimit,
                firstTransactionDate,
                nextPaymentDate
        );
    }
}


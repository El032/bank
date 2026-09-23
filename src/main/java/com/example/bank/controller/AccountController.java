package com.example.bank.controller;

import com.example.bank.dto.*;
import com.example.bank.model.*;
import com.example.bank.service.BankAccountService;
import com.example.bank.service.BankCardService;
import com.example.bank.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Счета", description = "Управление банковскими счетами")
@SecurityRequirement(name = "Bearer Authentication")
public class AccountController {

    private final BankAccountService service;
    private final TransferService transferService;
    private final BankCardService bankCardService;

    public AccountController(BankAccountService service,
                             TransferService transferService,
                             BankCardService bankCardService) {
        this.service = service;
        this.transferService = transferService;
        this.bankCardService = bankCardService;
    }

    @GetMapping
    @Operation(
            summary = "Получить все счета",
            description = """
            Возвращает постраничный список банковских счетов.
            Администратор получает все счета.
            Обычный пользователь получает только собственные счета.
            Поддерживается фильтрация по статусу и сортировка.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список счетов успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<PageResponse<AccountResponse>> getAll(
            @ParameterObject
            @PageableDefault(
                    size = 20,
                    sort = "id",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable,

            @RequestParam(required = false)
            AccountStatus status,

            Authentication authentication) {

        Page<BankAccount> page;

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

            if (status != null) {
                page = service.getAccountsPaged(status, pageable);
            } else {
                page = service.getAllAccountsPaged(pageable);
            }

        } else {

            if (status != null) {
                page = service.getAccountsPagedByUsername(
                        status,
                        authentication.getName(),
                        pageable
                );
            } else {
                page = service.getAccountsPagedByUsername(
                        authentication.getName(),
                        pageable
                );
            }
        }

        return ResponseEntity.ok(
                PageResponse.of(page, this::toResponseAccount)
        );
    }

    //GET Выписка по счёту
    @GetMapping("/{id}/statement")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurityService.isOwner(#id, authentication.name)")
    @Operation(summary = "Получить выписку по счету",description = "Доступно только администраторам и самому владельцу счета")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Выписка по счёту",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StatementResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "404", description = "Счёт не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")

    })

    public ResponseEntity<StatementResponse> getStatement(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.getStatement(id));
    }

    //GET история операций входящих и исходящих
    @GetMapping("/{id}/transfers")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurityService.isOwner(#id, authentication.name)")
    @Operation(summary = "Получить историю операций по счету",description = "Доступно только администраторам и самому владельцу счета")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "История переводов",
            content = @Content(
                    array = @ArraySchema(
                            schema = @Schema(implementation = TransferResponse.class)
                    )
            )),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "404", description = "Счёт не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<List<TransferResponse>> getTransfers(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                toResponseTransfer(transferService.getTransferHistory(id)));
    }

    // GET /accounts/{id}/transactions?page=0&size=2&sort=createdAt,desc
    @GetMapping("/{id}/transactions")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurityService.isOwner(#id, authentication.name)")
    @Operation(
            summary = "Получить историю транзакций",
            description = """
                Возвращает постраничную историю транзакций счёта.
                Доступно только администраторам и владельцу счёта.

                Поддерживаются параметры:
                page — номер страницы;
                size — количество записей на странице;
                sort — поле и направление сортировки,
                например createdAt,desc.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "История транзакций успешно получена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Счёт не найден"
            )
    })
    public ResponseEntity<PageResponse<TransactionResponse>> getTransactions(
            @PathVariable Long id,

            @ParameterObject
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        Page<Transaction> page =
                service.getTransactionHistory(id, pageable);

        PageResponse<TransactionResponse> response =
                PageResponse.of(page, this::toResponseTransaction);

        return ResponseEntity.ok(response);
    }

    // GET /accounts/search?name=eldar&page=0&size=5&sort=owner,asc
    @GetMapping("/search")
    @Operation(
            summary = "Поиск счетов по имени владельца",
            description = """
                ADMIN выполняет поиск среди всех счетов.
                Обычный пользователь выполняет поиск только среди собственных счетов.

                Поддерживаются параметры:
                page — номер страницы;
                size — количество записей на странице;
                sort — поле и направление сортировки,
                например owner,asc.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Счета успешно найдены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры поиска",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<PageResponse<AccountResponse>> search(
            @RequestParam String name,

            @ParameterObject
            @PageableDefault(
                    size = 10,
                    sort = "owner",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable,

            Authentication authentication) {

        Page<BankAccount> page;

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            page = service.searchByName(name, pageable);
        } else {
            page = service.searchByNameForUser(
                    name,
                    authentication.getName(),
                    pageable
            );
        }

        return ResponseEntity.ok(
                PageResponse.of(page, this::toResponseAccount)
        );
    }

    // Пользователь может видеть только свои счета (кастомное условие)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurityService.isOwner(#id, authentication.name)")
    @Operation(summary = "Получить счёт по ID", description = "доступно только администраторам и самому владельцу счета")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Счёт найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Счёт не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })

    public ResponseEntity<AccountResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponseAccount(service.findById(id)));
    }

    @PostMapping("/{id}/cards")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurityService.isOwner(#id, authentication.name)")
    @Operation(
            summary = "Создание карты к счету",
            description = "Доступно администраторам и владельцу счета"
    )
    public ResponseEntity<CardCreatedResponse> createCard(
            @PathVariable Long id)  {

        CardCreatedResponse card = bankCardService.createCard(id);

        URI location = URI.create("/accounts/" + id + "/cards");

        return ResponseEntity
                .created(location)
                .body(card);
    }

    @GetMapping("/{id}/cards")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurityService.isOwner(#id, authentication.name)")
    @Operation(
            summary = "Получить карту счёта",
            description = "Доступно администраторам и владельцу счёта"
    )
    public ResponseEntity<CardResponse> getCard(
            @PathVariable Long id) {

        BankCard card = bankCardService.getCardByAccountId(id);

        String expiryDate = card.getExpiryDate()
                .format(java.time.format.DateTimeFormatter.ofPattern("MM/yy"));

        CardResponse response = new CardResponse(
                card.getCardNumber(),
                expiryDate,
                card.getStatus()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/cards/cvv")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurityService.isOwner(#id, authentication.name)")
    @Operation(
            summary = "Получить CVV карты",
            description = "Доступно администратору и владельцу счёта"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CVV получен"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            @ApiResponse(responseCode = "404", description = "Счёт или карта не найдены"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public ResponseEntity<Map<String, String>> getCvv(
            @PathVariable Long id) {

        String cvv = bankCardService.getCvv(id);

        return ResponseEntity.ok(
                Map.of("cvv", cvv)
        );
    }

    @PatchMapping("/{id}/cards/pin")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurityService.isOwner(#id, authentication.name)")
    @Operation(
            summary = "Изменить PIN карты",
            description = "Владелец карты обязан указать старый PIN. Администратор сможет изменить PIN без старого PIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "PIN успешно изменён"),
            @ApiResponse(responseCode = "400", description = "Некорректный PIN"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            @ApiResponse(responseCode = "404", description = "Счёт или карта не найдены"),
            @ApiResponse(responseCode = "422", description = "Неверный старый PIN")
    })
    public ResponseEntity<Void> changePin(
            @PathVariable Long id,
            @Valid @RequestBody ChangePinRequest request,
            Authentication authentication)  {

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        bankCardService.changePin(
                id,
                request.getOldPin(),
                request.getNewPin(),
                admin
        );

        return ResponseEntity.noContent().build();
    }

    // PATCH
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountSecurityService.isOwner(#id, authentication.name)")
    @Operation(summary = "Изменение счета", description = "Доступно только администраторам и самому владельцу счета")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Счёт успешно изменён",
                    content = @Content(
                            schema = @Schema(implementation = AccountResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            @ApiResponse(responseCode = "404", description = "Счёт не найден")
    })
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long id,
            @Valid    @RequestBody UpdateAccountRequest request) {
        BankAccount updated = service.updateAccount(
                id,
                request.getOwner(),
                request.getStatus()
        );
        return ResponseEntity.ok(toResponseAccount(updated));
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

    private List<AccountResponse> toResponseAccount(List<BankAccount> accounts) {
        return accounts.stream()
                .map(this::toResponseAccount)
                .toList();
    }
    private TransactionResponse toResponseTransaction(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getSource(),
                transaction.getCreatedAt()
        );
    }
    private List<TransactionResponse> toResponseTransaction(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::toResponseTransaction)
                .toList();
    }

    private TransferResponse toResponseTransfer(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getFromAccount().getId(),
                transfer.getToAccount().getId(),
                transfer.getAmount(),
                transfer.getStatus(),
                transfer.getCreatedAt()
        );
    }

    private List<TransferResponse> toResponseTransfer(List<Transfer> transfers) {
        return transfers.stream()
                .map(this::toResponseTransfer)
                .toList();
    }



}
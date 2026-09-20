package com.example.bank.controller;

import com.example.bank.dto.*;
import com.example.bank.model.*;
import com.example.bank.service.UserService;
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

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;



@RestController
@RequestMapping("/users")
@Tag(
        name = "Пользователи",
        description = "Управление пользователями и их банковскими счетами"
)
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /users — создать пользователя
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Создать пользователя",
            description = "Создаёт нового пользователя. Доступно только администраторам."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно создан",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён. Требуется роль ADMIN.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(
                request.getUserName(),
                request.getEmail(),
                request.getFullName(),
                request.getPassword());
        URI location = URI.create("/users/" + user.getId());
        return ResponseEntity.created(location).body(toResponse(user));
    }

    // GET /users — все пользователи
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает список всех зарегистрированных пользователей. Доступно только администраторам."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список пользователей успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = UserResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён. Требуется роль ADMIN.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsersCached());
    }
//    public ResponseEntity<List<UserResponse>> getAllUsers() {
//        List<UserResponse> users = userService.getAllUsers().stream()
//                .map(this::toResponse)
//                .toList();
//        return ResponseEntity.ok(users);
//    }

    // GET /users/{id} — пользователь по id
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isOwner(#id, authentication.name)")
    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает информацию о пользователе. Доступно администратору или самому пользователю."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findUserResponseById(id));
    }

    // GET /users/{id}/accounts — пользователь со счетами
    @GetMapping("/{id}/accounts")
    @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isOwner(#id, authentication.name)")
    @Operation(
            summary = "Получить пользователя со счетами",
            description = "Возвращает данные пользователя вместе со списком его банковских счетов. Доступно администратору или самому пользователю."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь и его счета успешно получены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserWithAccountsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<UserWithAccountsResponse> getUserWithAccounts(
            @PathVariable Long id) {

        User user = userService.getUserWithAccounts(id);

        UserWithAccountsResponse response = new UserWithAccountsResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getFullName(),
                user.isActive(),
                user.getAccounts().stream()
                        .map(this::toAccountResponse)
                        .toList()
        );

        return ResponseEntity.ok(response);
    }
//    public ResponseEntity<UserResponse> getUserWithAccounts(@PathVariable Long id) {
//        User user = userService.getUserWithAccounts(id);
//        UserResponse response = toResponse(user);
//        response.setAccounts(
//                user.getAccounts().stream()
//                        .map(this::toAccountResponse)
//                        .toList()
//        );
//        return ResponseEntity.ok(response);
//    }

    // POST /users/{id}/accounts — создать счёт для пользователя
    @PostMapping("/{id}/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Создать банковский счёт пользователю",
            description = "Создаёт новый банковский счёт для указанного пользователя. Доступно только администраторам."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Счёт успешно создан",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Не авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён. Требуется роль ADMIN.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<AccountResponse> createAccount(
            @PathVariable Long id,
            @Valid @RequestBody CreateAccountRequest request) {
        BankAccount account = userService.createAccountForUser(id,request);
        URI location = URI.create("/accounts/" + account.getId());
        return ResponseEntity.created(location).body(toAccountResponse(account));
    }



//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(
//            summary = "Удалить пользователя",
//            description = "Удаляет пользователя. Доступно только администраторам."
//    )
//    @ApiResponses({
//            @ApiResponse(
//                    responseCode = "204",
//                    description = "Пользователь успешно удалён"
//            ),
//            @ApiResponse(
//                    responseCode = "401",
//                    description = "Не авторизован"
//            ),
//            @ApiResponse(
//                    responseCode = "403",
//                    description = "Доступ запрещён. Требуется роль ADMIN."
//            ),
//            @ApiResponse(
//                    responseCode = "404",
//                    description = "Пользователь не найден"
//            )
//    })
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        userService.deleteUser(id);
//        return ResponseEntity.noContent().build();
//    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getFullName(),
                user.isActive()
        );
    }

    private AccountResponse toAccountResponse(BankAccount account) {
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
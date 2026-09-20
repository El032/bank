package com.example.bank.controller;

import com.example.bank.dto.ErrorResponse;
import com.example.bank.dto.UpdateUserStatusRequest;
import com.example.bank.model.User;
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

@RestController
@RequestMapping("/admin/users")
@Tag(
        name = "ADMIN - Users",
        description = "Административное управление пользователями"
)
@SecurityRequirement(name = "Bearer Authentication")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Изменить активность пользователя",
            description = """
                    Изменяет состояние пользователя.

                    false - деактивирует пользователя.
                    true  - повторно активирует пользователя.

                    Пользователь не удаляется из системы.
                    После деактивации его можно восстановить.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус пользователя успешно изменён"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав: требуется роль ADMIN",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<User> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {

        User user = userService.updateStatus(
                id,
                request.getActive()
        );

        return ResponseEntity.ok(user);
    }
}
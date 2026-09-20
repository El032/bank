package com.example.bank.controller;

import com.example.bank.dto.*;
import com.example.bank.model.User;
import com.example.bank.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Аутентификация", description = "Регистрация и вход в систему")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /auth/register
    @PostMapping("/register")
    @Operation(
            summary = "Регистрация",
            description = "Создаёт нового пользователя. Пароль хешируется через BCrypt."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "409", description = "Username или email уже заняты")
    })
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.register(
                request.getUserName(),
                request.getEmail(),
                request.getPassword(),
                request.getFullName());

        URI location = URI.create("/users/" + user.getId());
        return ResponseEntity.created(location).body(Map.of(
                "message", "Пользователь зарегистрирован",
                "userId", user.getId(),
                "username", user.getUserName()
        ));
    }

    // POST /auth/login — пока возвращает заглушку, JWT добавим в следующем уроке
    @PostMapping("/login")
    @Operation(
            summary = "Вход",
            description = "Возвращает JWT токен для использования в заголовке Authorization."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход",
                    content = @Content(examples = @ExampleObject(
                            value = """
                    {
                      "token": "eyJhbGciOiJIUzI1NiJ9...",
                      "type": "Bearer"
                    }
                    """
                    ))),
            @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    })
    public ResponseEntity<Map<String,Object>> login(
            @RequestBody LoginRequest request){

        String token = authService.login(
                request.getUserName(),
                request.getPassword()
        );


        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "type", "Bearer"
                ));
    }
@GetMapping("/me")
@Operation(summary = "Текущий пользователь", description = "Требует JWT токен")
@SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Map<String,Object>> me(
            @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(Map.of(
                "username", userDetails.getUsername(),
                "authorities", userDetails.getAuthorities().toString()
                ));
}
}
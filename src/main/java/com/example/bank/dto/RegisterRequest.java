package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на регистрацию нового пользователя")
public class RegisterRequest {

    @Schema(
            description = "Имя пользователя (логин). Должно содержать от 3 до 50 символов",
            example = "ivan123",
            minLength = 3,
            maxLength = 50
    )
    @NotBlank
    @Size(min = 3, max = 50)
    private String userName;

    @Schema(
            description = "Электронная почта пользователя",
            example = "ivan@example.com",
            format = "email"
    )
    @NotBlank
    @Email
    private String email;

    @Schema(
            description = "Пароль пользователя. Минимальная длина — 6 символов",
            example = "Password123",
            minLength = 6,
            format = "password"
    )
    @NotBlank
    @Size(min = 6, message = "Пароль минимум 6 символов")
    private String password;

    @Schema(
            description = "Полное имя пользователя",
            example = "Иван Иванов"
    )
    @NotBlank(message = "Полное имя обязательно")
    private String fullName;

    public RegisterRequest() {}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}


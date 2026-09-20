package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание нового пользователя")
public class CreateUserRequest {

    @Schema(
            description = "Имя пользователя (логин)",
            example = "ivan123",
            minLength = 2,
            maxLength = 50
    )
    @NotBlank(message = "Username обязателен")
    @Size(
            min = 2,
            max = 50,
            message = "Username от 2 до 50 символов"
    )
    private String userName;

    @Schema(
            description = "Электронная почта пользователя",
            example = "ivan@example.com",
            format = "email"
    )
    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный формат email")
    private String email;

    @Schema(
            description = "Полное имя пользователя",
            example = "Иван Иванов",
            maxLength = 200
    )
    @Size(max = 200)
    private String fullName;

    @Schema(
            description = "Пароль пользователя. Минимальная длина — 6 символов",
            example = "Password123",
            minLength = 6,
            format = "password"
    )
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль минимум 6 символов")
    private String password;

    public CreateUserRequest() {}

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

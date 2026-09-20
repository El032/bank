package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Запрос на авторизацию пользователя")
public class LoginRequest {

    @Schema( description = "Имя пользователя (логин)",
            example = "ivan123" )
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    private String userName;

    @Schema( description = "Пароль пользователя",
            example = "Password123",
            format = "password" )
    @NotBlank(message = "Пароль не должен быть пустым")
    private String password;

    public LoginRequest() {}
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
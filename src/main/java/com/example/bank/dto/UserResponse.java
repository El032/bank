package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Информация о пользователе")
public class UserResponse {

    @Schema(
            description = "Уникальный идентификатор пользователя",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Имя пользователя (логин)",
            example = "ivan123"
    )
    private String userName;

    @Schema(
            description = "Электронная почта пользователя",
            example = "ivan@example.com",
            format = "email"
    )
    private String email;

    @Schema(
            description = "Полное имя пользователя",
            example = "Иван Иванов"
    )
    private String fullName;

    @Schema(
            description = "Признак активности пользователя",
            example = "true"
    )
    private boolean active;

//    @Schema(
//            description = "Список банковских счетов пользователя"
//    )
//
//    private List<AccountResponse> accounts;

    public UserResponse() {}

    public UserResponse(
            Long id,
            String userName,
            String email,
            String fullName,
            boolean active
    ) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.fullName = fullName;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

//    public List<AccountResponse> getAccounts() {
//        return accounts;
//    }
//
//    public void setAccounts(List<AccountResponse> accounts) {
//        this.accounts = accounts;
//    }
}

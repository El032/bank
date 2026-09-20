package com.example.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        description = "Информация о пользователе вместе с его банковскими счетами"
)
public class UserWithAccountsResponse {

    @Schema(
            description = "Уникальный идентификатор пользователя",
            example = "2"
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

    @Schema(
            description = "Список банковских счетов пользователя"
    )
    private List<AccountResponse> accounts;

    public UserWithAccountsResponse() {
    }

    public UserWithAccountsResponse(
            Long id,
            String userName,
            String email,
            String fullName,
            boolean active,
            List<AccountResponse> accounts) {

        this.id = id;
        this.userName = userName;
        this.email = email;
        this.fullName = fullName;
        this.active = active;
        this.accounts = accounts;
    }

    public Long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isActive() {
        return active;
    }

    public List<AccountResponse> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountResponse> accounts) {
        this.accounts = accounts;
    }
}

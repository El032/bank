package com.example.bank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String userName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "role", nullable = false, length = 20)
    private String role = "USER";

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Один пользователь → много счетов
    @JsonManagedReference
    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BankAccount> accounts = new ArrayList<>();

    public User() {}

    public User(String userName, String email, String fullName, String passwordHash) {
        this.userName = userName;
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

    // Вспомогательный метод — синхронизирует обе стороны
    public void addAccount(BankAccount account) {
        accounts.add(account);
        account.setUser(this);
    }

    public void removeAccount(BankAccount account) {
        accounts.remove(account);
        account.setUser(null);
    }

    // Геттеры и сеттеры
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<BankAccount> getAccounts() {
        return accounts;
    }

    public void deactivate() {
        active = false;
    }

    public void activate() {
        active = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        // НЕ включаем accounts — рекурсия!
        return "User{id=" + id + ", username=" + userName + ", email=" + email + "}";
    }
}
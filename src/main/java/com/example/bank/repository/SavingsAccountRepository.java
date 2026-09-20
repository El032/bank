package com.example.bank.repository;

import com.example.bank.model.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long> {
}
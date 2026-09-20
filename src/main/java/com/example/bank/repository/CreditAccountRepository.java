package com.example.bank.repository;

import com.example.bank.model.CreditAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditAccountRepository extends JpaRepository<CreditAccount, Long> {
}
package com.example.bank.repository;

import com.example.bank.model.BankCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankCardRepository extends JpaRepository<BankCard, Long> {

    Optional<BankCard> findByAccountId(Long accountId);

}
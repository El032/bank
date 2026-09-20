package com.example.bank.repository;

import com.example.bank.model.AtmSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AtmSessionRepository extends JpaRepository<AtmSession, Long> {

    Optional<AtmSession> findByIdAndActiveTrue(Long id);

    Optional<AtmSession> findByCardIdAndActiveTrue(Long cardId);

    Optional<AtmSession> findByIdAndActiveTrueAndCardReturnedFalse(Long id);


}
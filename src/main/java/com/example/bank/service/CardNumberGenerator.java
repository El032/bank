package com.example.bank.service;

import com.example.bank.model.AccountType;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class CardNumberGenerator {

    public String generate(AccountType accountType) {

        String prefix = switch (accountType) {
            case DEBIT -> "41697833";
            case CREDIT -> "22002002";
            case SAVINGS -> throw new IllegalArgumentException(
                    "Для сберегательного счёта карта не создаётся"
            );
        };

        String suffix = String.format(
                "%08d",
                ThreadLocalRandom.current().nextInt(0, 100_000_000)
        );

        return prefix.substring(0, 4) + "-"
                + prefix.substring(4, 8) + "-"
                + suffix.substring(0, 4) + "-"
                + suffix.substring(4, 8);
    }
}
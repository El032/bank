package com.example.bank.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class AccountNumberGenerator {

    private static final String PREFIX = "40817910";

    public String generate() {

        return PREFIX.substring(0, 4) + "-"
                + PREFIX.substring(4, 8) + "-"
                + randomBlock() + "-"
                + randomBlock() + "-"
                + randomBlock();
    }

    private String randomBlock() {

        return String.format(
                "%04d",
                ThreadLocalRandom.current().nextInt(0, 10000)
        );
    }
}
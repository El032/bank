package com.example.bank.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class PinGenerator {

    public String generate() {
        return String.format(
                "%04d",
                ThreadLocalRandom.current().nextInt(0, 10000)
        );
    }
}
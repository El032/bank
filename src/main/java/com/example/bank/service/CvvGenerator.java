package com.example.bank.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class CvvGenerator {

    public String generate() {
        return String.format(
                "%03d",
                ThreadLocalRandom.current().nextInt(0, 1000)
        );
    }
}
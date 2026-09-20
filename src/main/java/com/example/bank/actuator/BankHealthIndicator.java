package com.example.bank.actuator;

import com.example.bank.model.AccountStatus;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import org.springframework.boot.actuate.health.*;
import org.springframework.stereotype.Component;

@Component
public class BankHealthIndicator implements HealthIndicator {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public BankHealthIndicator(AccountRepository accountRepository,
                               UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Health health() {
        try {
            long accountCount = accountRepository.count();
            long userCount = userRepository.count();
            long activeCount = accountRepository.findByStatus(AccountStatus.ACTIVE).size();
            return Health.up()
                    .withDetail("accounts", accountCount)
                    .withDetail("users", userCount)
                    .withDetail("activeAccounts", activeCount)
                    .withDetail("status", "Банк работает")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
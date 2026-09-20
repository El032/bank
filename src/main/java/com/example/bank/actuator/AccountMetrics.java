package com.example.bank.actuator;


import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.Counter;

@Component
public class AccountMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter accountsCreated;
    private final Counter accountsCardscreated;

    public AccountMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.accountsCreated = meterRegistry.counter("bank.accounts.created");
        this .accountsCardscreated =meterRegistry.counter("bank.accounts.cards.created");
          }


    public void accountCreated() {
        accountsCreated.increment();
    }

    public void accountCardsCreated() {
        accountsCardscreated.increment();
    }
}

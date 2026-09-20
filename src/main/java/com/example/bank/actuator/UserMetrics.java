package com.example.bank.actuator;


import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.Counter;

@Component
public class UserMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter usersCreated;

    public UserMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.usersCreated = meterRegistry.counter("bank.users.created");

    }


    public void userCreated() {
        usersCreated.increment();
    }


}

package com.example.bank.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TransferMetrics {

    private final Counter successfulTransfers;
    private final Counter failedTransfers;
    private final Timer transferDuration;
    private final DistributionSummary transferAmount;
    private final AtomicInteger activeTransfers;

    public TransferMetrics(MeterRegistry meterRegistry) {

        this.successfulTransfers =
                meterRegistry.counter("bank.transfers.success");

        this.failedTransfers =
                meterRegistry.counter("bank.transfers.failed");

        this.transferDuration =
                Timer.builder("bank.transfers.duration")
                        .description("Время выполнения перевода")
                        .publishPercentileHistogram()
                        .minimumExpectedValue(java.time.Duration.ofMillis(1))
                        .maximumExpectedValue(java.time.Duration.ofSeconds(10))
                        .register(meterRegistry);

        this.transferAmount =
                DistributionSummary.builder("bank.transfers.amount")
                        .description("Суммы успешных переводов")
                        .baseUnit("currency")
                        .register(meterRegistry);

        this.activeTransfers = meterRegistry.gauge(
                "bank.transfers.active",
                new AtomicInteger(0)
        );
    }

    public void transferSucceeded() {
        successfulTransfers.increment();
    }

    public void transferFailed() {
        failedTransfers.increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start();
    }

    public void stopTimer(Timer.Sample sample) {
        sample.stop(transferDuration);
    }

    public void recordAmount(double amount) {
        transferAmount.record(amount);
    }

    public void transferStarted() {
        activeTransfers.incrementAndGet();
    }

    public void transferFinished() {
        activeTransfers.decrementAndGet();
    }
}
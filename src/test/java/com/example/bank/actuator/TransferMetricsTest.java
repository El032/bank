package com.example.bank.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferMetricsTest {

    private MeterRegistry meterRegistry;
    private TransferMetrics transferMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        transferMetrics = new TransferMetrics(meterRegistry);
    }

    @Test
    @DisplayName("Успешный перевод увеличивает счётчик")
    void transferSucceeded_shouldIncrementCounter() {

        transferMetrics.transferSucceeded();
        transferMetrics.transferSucceeded();

        Counter counter = meterRegistry.get("bank.transfers.success")
                .counter();

        assertEquals(2.0, counter.count());
    }

    @Test
    @DisplayName("Неуспешный перевод увеличивает счётчик")
    void transferFailed_shouldIncrementCounter() {

        transferMetrics.transferFailed();
        transferMetrics.transferFailed();
        transferMetrics.transferFailed();

        Counter counter = meterRegistry.get("bank.transfers.failed")
                .counter();

        assertEquals(3.0, counter.count());
    }

    @Test
    @DisplayName("startTimer возвращает Timer.Sample")
    void startTimer_shouldReturnSample() {

        Timer.Sample sample = transferMetrics.startTimer();

        assertTrue(sample != null);
    }

    @Test
    @DisplayName("stopTimer записывает время выполнения перевода")
    void stopTimer_shouldRecordDuration() throws InterruptedException {

        Timer.Sample sample = transferMetrics.startTimer();

        Thread.sleep(10);

        transferMetrics.stopTimer(sample);

        Timer timer = meterRegistry.get("bank.transfers.duration")
                .timer();

        assertEquals(1L, timer.count());
        assertTrue(timer.totalTime(java.util.concurrent.TimeUnit.NANOSECONDS) > 0);
    }

    @Test
    @DisplayName("recordAmount записывает сумму перевода")
    void recordAmount_shouldRecordAmount() {

        transferMetrics.recordAmount(100.0);
        transferMetrics.recordAmount(250.0);

        DistributionSummary summary =
                meterRegistry.get("bank.transfers.amount")
                        .summary();

        assertEquals(2L, summary.count());
        assertEquals(350.0, summary.totalAmount());
    }

    @Test
    @DisplayName("transferStarted увеличивает количество активных переводов")
    void transferStarted_shouldIncrementActiveTransfers() {

        transferMetrics.transferStarted();
        transferMetrics.transferStarted();

        assertEquals(
                2.0,
                meterRegistry.get("bank.transfers.active")
                        .gauge()
                        .value()
        );
    }

    @Test
    @DisplayName("transferFinished уменьшает количество активных переводов")
    void transferFinished_shouldDecrementActiveTransfers() {

        transferMetrics.transferStarted();
        transferMetrics.transferStarted();
        transferMetrics.transferFinished();

        assertEquals(
                1.0,
                meterRegistry.get("bank.transfers.active")
                        .gauge()
                        .value()
        );
    }
}
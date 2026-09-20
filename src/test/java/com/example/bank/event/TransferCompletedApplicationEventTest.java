package com.example.bank.event;

import com.example.bank.model.BankAccount;
import com.example.bank.model.Transfer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class TransferCompletedApplicationEventTest {

    @Test
    @DisplayName("Конструктор и accessors record корректно работают")
    void constructor_shouldSetAllFields() {

        Transfer transfer = new Transfer();
        BankAccount from = new BankAccount("user1");
        BankAccount to = new BankAccount("user2");
        BigDecimal amount = new BigDecimal("250.50");

        TransferCompletedApplicationEvent event =
                new TransferCompletedApplicationEvent(
                        transfer,
                        from,
                        to,
                        amount
                );

        assertSame(transfer, event.transfer());
        assertSame(from, event.from());
        assertSame(to, event.to());
        assertEquals(amount, event.amount());
    }
}

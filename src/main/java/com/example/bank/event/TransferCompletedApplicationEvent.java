package com.example.bank.event;

import com.example.bank.model.BankAccount;
import com.example.bank.model.Transfer;

import java.math.BigDecimal;

public record TransferCompletedApplicationEvent(
        Transfer transfer,
        BankAccount from,
        BankAccount to,
        BigDecimal amount
) {
}
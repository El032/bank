package com.example.bank.service;

import com.example.bank.actuator.AccountMetrics;
import com.example.bank.dto.CardCreatedResponse;
import com.example.bank.exception.*;
import com.example.bank.model.*;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.BankCardRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BankCardService {

    private final BankCardRepository bankCardRepository;
    private final AccountRepository accountRepository;
    private final CardNumberGenerator cardNumberGenerator;
    private final CvvGenerator cvvGenerator;
    private final PinGenerator pinGenerator;
    private final PasswordEncoder passwordEncoder;
    private final CvvEncryptionService cvvEncryptionService;
    private final AccountMetrics accountMetrics;

    public BankCardService(BankCardRepository bankCardRepository,
                           AccountRepository accountRepository,
                           CardNumberGenerator cardNumberGenerator,
                           CvvGenerator cvvGenerator,
                           PinGenerator pinGenerator,
                           PasswordEncoder passwordEncoder,
                           CvvEncryptionService cvvEncryptionService, AccountMetrics accountMetrics) {

        this.bankCardRepository = bankCardRepository;
        this.accountRepository = accountRepository;
        this.cardNumberGenerator = cardNumberGenerator;
        this.cvvGenerator = cvvGenerator;
        this.pinGenerator = pinGenerator;
        this.passwordEncoder = passwordEncoder;
        this.cvvEncryptionService = cvvEncryptionService;
        this.accountMetrics = accountMetrics;
    }

    public CardCreatedResponse createCard(Long accountId) {

        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InactiveAccountException(accountId);
        }

        AccountType accountType;

        if (account instanceof CreditAccount) {
            accountType = AccountType.CREDIT;
        } else if (account instanceof SavingsAccount) {
            accountType = AccountType.SAVINGS;
        } else {
            accountType = AccountType.DEBIT;
        }

        if (accountType == AccountType.SAVINGS) {
            throw new SavingsCardNotAllowedException(accountId);
        }

        String cardNumber =
                cardNumberGenerator.generate(accountType);

        String pin =
                pinGenerator.generate();

        String cvv =
                cvvGenerator.generate();

        LocalDate expiryDate =
                LocalDate.now().plusYears(5);

        BankCard card = new BankCard(
                cardNumber,
                passwordEncoder.encode(pin),
                expiryDate,
                cvvEncryptionService.encrypt(cvv),
                account
        );

        bankCardRepository.save(card);

        accountMetrics.accountCardsCreated();

        String formattedExpiryDate = expiryDate
                .format(java.time.format.DateTimeFormatter.ofPattern("MM/yy"));

        return new CardCreatedResponse(
                cardNumber,
                formattedExpiryDate,
                pin,
                cvv
        );
    }

    public BankCard getCardByAccountId(Long accountId) {

        accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return bankCardRepository.findByAccountId(accountId)
                .orElseThrow(() -> new CardNotFoundException(accountId));
    }

    public String getCvv(Long accountId) {

        BankCard card = getCardByAccountId(accountId);

        return cvvEncryptionService.decrypt(
                card.getCvvEncrypted()
        );
    }

    public boolean checkPin(BankCard card, String pin) {

        return passwordEncoder.matches(
                pin,
                card.getPinHash()
        );
    }



    public void changePin(
            Long accountId,
            String oldPin,
            String newPin,
            boolean admin
    ) {

        BankCard card = getCardByAccountId(accountId);

        if (!admin) {
            if (!passwordEncoder.matches(oldPin, card.getPinHash())) {
                throw new InvalidPinException();
            }
        }

        card.setPinHash(passwordEncoder.encode(newPin));

        bankCardRepository.save(card);
    }

    public BankCard updateStatus(Long id, CardStatus status) {

        BankCard card = bankCardRepository.findById(id)
                .orElseThrow(() ->
                        new CardNotFoundException(id));

        switch (status) {

            case ACTIVE -> card.activate(id);

            case BLOCKED -> card.block(id);

            case CLOSED -> card.close(id);
        }

        return bankCardRepository.save(card);
    }
}
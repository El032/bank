package com.example.bank.service;

import com.example.bank.exception.*;
import com.example.bank.model.AtmSession;
import com.example.bank.model.BankCard;
import com.example.bank.model.TransactionSource;
import com.example.bank.model.TransactionType;
import com.example.bank.repository.AtmSessionRepository;
import com.example.bank.repository.BankCardRepository;
import com.example.bank.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class AtmSessionService {

    private final AtmSessionRepository atmSessionRepository;
    private final BankCardRepository bankCardRepository;
    private final BankCardService bankCardService;
    private final BankAccountService bankAccountService;
    private final TransactionRepository transactionRepository;


    private static final BigDecimal ATM_DAILY_WITHDRAWAL_LIMIT =
            new BigDecimal("50000.00");

    private static final BigDecimal ATM_MIN_DEPOSIT =
            new BigDecimal("100.00");

    private static final BigDecimal ATM_MIN_WITHDRAWAL =
            new BigDecimal("100.00");

    public AtmSessionService(
            AtmSessionRepository atmSessionRepository,
            BankCardRepository bankCardRepository,
            BankCardService bankCardService,
            BankAccountService bankAccountService,
            TransactionRepository transactionRepository
    ) {
        this.atmSessionRepository = atmSessionRepository;
        this.bankCardRepository = bankCardRepository;
        this.bankCardService = bankCardService;
        this.bankAccountService = bankAccountService;
        this.transactionRepository = transactionRepository;

    }

    public AtmSession insertCard(Long cardId) {

        BankCard card = bankCardRepository.findById(cardId)
                .orElseThrow(() ->
                        new CardNotFoundException(cardId));

        if (!card.isActive()) {
            throw new InactiveCardException(cardId);
        }

        if (atmSessionRepository.findByCardIdAndActiveTrue(cardId).isPresent()) {
            throw new ActiveAtmSessionException(cardId);
        }

        AtmSession session = new AtmSession(card);

        return atmSessionRepository.save(session);
    }

    public AtmSession getActiveSession(Long sessionId) {

        return atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId)
                .orElseThrow(() ->
                        new AtmSessionNotActiveException(sessionId));
    }

    public void verifyPin(Long sessionId, String pin) {

        AtmSession session = getActiveSession(sessionId);

        if (session.isPinVerified()) {
            return;
        }

        boolean valid = bankCardService.checkPin(
                session.getCard(),
                pin
        );

        if (!valid) {
            throw new InvalidPinException();
        }

        session.verifyPin();

        atmSessionRepository.save(session);
    }

    public AtmSession requireAuthorizedSession(Long sessionId) {

        AtmSession session = getActiveSession(sessionId);

        if (!session.isPinVerified()) {
            throw new AtmPinRequiredException();
        }

        return session;
    }

    public void closeSession(Long sessionId) {

        AtmSession session = getActiveSession(sessionId);

        session.close();
        session.returnCard();

        atmSessionRepository.save(session);
    }

    public BigDecimal getBalance(Long sessionId) {

        AtmSession session = requireAuthorizedSession(sessionId);

        return session.getCard().getAccountBalance();
    }

    public void withdraw(Long sessionId, BigDecimal amount) {

        AtmSession session = requireAuthorizedSession(sessionId);

        if (amount.compareTo(ATM_MIN_WITHDRAWAL) < 0) {
            throw new AtmMinimumAmountException(ATM_MIN_WITHDRAWAL);
        }

        Long accountId = session.getCard()
                .getAccount()
                .getId();

        LocalDateTime startOfDay = LocalDateTime.of(
                LocalDate.now(),
                LocalTime.MIDNIGHT
        );

        BigDecimal withdrawnToday = transactionRepository.sumAmount(
                accountId,
                TransactionType.WITHDRAW,
                TransactionSource.ATM,
                startOfDay
        );

        BigDecimal totalAfterWithdrawal =
                withdrawnToday.add(amount);

        if (totalAfterWithdrawal.compareTo(ATM_DAILY_WITHDRAWAL_LIMIT) > 0) {
            throw new AtmDailyWithdrawalLimitException(
                    ATM_DAILY_WITHDRAWAL_LIMIT,
                    withdrawnToday,
                    amount
            );
        }

        bankAccountService.withdraw(
                accountId,
                amount,
                TransactionSource.ATM
        );
    }

    public  void deposit(Long sessionId, BigDecimal amount) {

        AtmSession session = requireAuthorizedSession(sessionId);

        if (amount.compareTo(ATM_MIN_DEPOSIT) < 0) {
            throw new AtmMinimumAmountException(ATM_MIN_DEPOSIT);
        }

        Long accountId = session.getCard()
                .getAccount()
                .getId();

        bankAccountService.deposit(
                accountId,
                amount,
                TransactionSource.ATM);
    }
}
package com.example.bank.service;

import com.example.bank.exception.ActiveAtmSessionException;
import com.example.bank.exception.AtmDailyWithdrawalLimitException;
import com.example.bank.exception.AtmMinimumAmountException;
import com.example.bank.exception.AtmPinRequiredException;
import com.example.bank.exception.AtmSessionNotActiveException;
import com.example.bank.exception.CardNotFoundException;
import com.example.bank.exception.InactiveCardException;
import com.example.bank.exception.InvalidPinException;
import com.example.bank.model.AtmSession;
import com.example.bank.model.BankAccount;
import com.example.bank.model.BankCard;
import com.example.bank.model.TransactionSource;
import com.example.bank.model.TransactionType;
import com.example.bank.repository.AtmSessionRepository;
import com.example.bank.repository.BankCardRepository;
import com.example.bank.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtmSessionServiceTest {

    @Mock
    private AtmSessionRepository atmSessionRepository;

    @Mock
    private BankCardRepository bankCardRepository;

    @Mock
    private BankCardService bankCardService;

    @Mock
    private BankAccountService bankAccountService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AtmSessionService atmSessionService;


    // =========================================================
    // insertCard()
    // =========================================================

    @Test
    void insertCard_shouldCreateSessionSuccessfully() {

        Long cardId = 1L;

        BankCard card = mock(BankCard.class);
        AtmSession session = mock(AtmSession.class);

        when(bankCardRepository.findById(cardId))
                .thenReturn(Optional.of(card));

        when(card.isActive())
                .thenReturn(true);

        when(atmSessionRepository.findByCardIdAndActiveTrue(cardId))
                .thenReturn(Optional.empty());

        when(atmSessionRepository.save(any(AtmSession.class)))
                .thenReturn(session);

        AtmSession result = atmSessionService.insertCard(cardId);

        assertSame(session, result);

        verify(bankCardRepository).findById(cardId);
        verify(card).isActive();
        verify(atmSessionRepository)
                .findByCardIdAndActiveTrue(cardId);
        verify(atmSessionRepository).save(any(AtmSession.class));
    }

    @Test
    void insertCard_shouldThrowWhenCardNotFound() {

        Long cardId = 999L;

        when(bankCardRepository.findById(cardId))
                .thenReturn(Optional.empty());

        assertThrows(
                CardNotFoundException.class,
                () -> atmSessionService.insertCard(cardId)
        );

        verify(bankCardRepository).findById(cardId);

        verify(atmSessionRepository, never())
                .findByCardIdAndActiveTrue(anyLong());

        verify(atmSessionRepository, never())
                .save(any());
    }

    @Test
    void insertCard_shouldThrowWhenCardInactive() {

        Long cardId = 1L;

        BankCard card = mock(BankCard.class);

        when(bankCardRepository.findById(cardId))
                .thenReturn(Optional.of(card));

        when(card.isActive())
                .thenReturn(false);

        assertThrows(
                InactiveCardException.class,
                () -> atmSessionService.insertCard(cardId)
        );

        verify(card).isActive();

        verify(atmSessionRepository, never())
                .findByCardIdAndActiveTrue(anyLong());

        verify(atmSessionRepository, never())
                .save(any());
    }

    @Test
    void insertCard_shouldThrowWhenActiveSessionAlreadyExists() {

        Long cardId = 1L;

        BankCard card = mock(BankCard.class);
        AtmSession existingSession = mock(AtmSession.class);

        when(bankCardRepository.findById(cardId))
                .thenReturn(Optional.of(card));

        when(card.isActive())
                .thenReturn(true);

        when(atmSessionRepository.findByCardIdAndActiveTrue(cardId))
                .thenReturn(Optional.of(existingSession));

        assertThrows(
                ActiveAtmSessionException.class,
                () -> atmSessionService.insertCard(cardId)
        );

        verify(atmSessionRepository, never())
                .save(any());
    }


    // =========================================================
    // getActiveSession()
    // =========================================================

    @Test
    void getActiveSession_shouldReturnActiveSession() {

        Long sessionId = 10L;

        AtmSession session = mock(AtmSession.class);

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.of(session));

        AtmSession result =
                atmSessionService.getActiveSession(sessionId);

        assertSame(session, result);

        verify(atmSessionRepository)
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId);
    }

    @Test
    void getActiveSession_shouldThrowWhenSessionNotActive() {

        Long sessionId = 10L;

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.empty());

        assertThrows(
                AtmSessionNotActiveException.class,
                () -> atmSessionService.getActiveSession(sessionId)
        );
    }


    // =========================================================
    // verifyPin()
    // =========================================================

    @Test
    void verifyPin_shouldVerifyCorrectPin() {

        Long sessionId = 10L;

        AtmSession session = mock(AtmSession.class);
        BankCard card = mock(BankCard.class);

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.of(session));

        when(session.isPinVerified())
                .thenReturn(false);

        when(session.getCard())
                .thenReturn(card);

        when(bankCardService.checkPin(card, "1234"))
                .thenReturn(true);

        atmSessionService.verifyPin(sessionId, "1234");

        verify(bankCardService)
                .checkPin(card, "1234");

        verify(session).verifyPin();

        verify(atmSessionRepository).save(session);
    }

    @Test
    void verifyPin_shouldDoNothingWhenPinAlreadyVerified() {

        Long sessionId = 10L;

        AtmSession session = mock(AtmSession.class);

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.of(session));

        when(session.isPinVerified())
                .thenReturn(true);

        atmSessionService.verifyPin(sessionId, "1234");

        verify(session, never()).getCard();

        verify(bankCardService, never())
                .checkPin(any(), anyString());

        verify(atmSessionRepository, never())
                .save(any());
    }

    @Test
    void verifyPin_shouldThrowWhenPinIsInvalid() {

        Long sessionId = 10L;

        AtmSession session = mock(AtmSession.class);
        BankCard card = mock(BankCard.class);

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.of(session));

        when(session.isPinVerified())
                .thenReturn(false);

        when(session.getCard())
                .thenReturn(card);

        when(bankCardService.checkPin(card, "9999"))
                .thenReturn(false);

        assertThrows(
                InvalidPinException.class,
                () -> atmSessionService.verifyPin(sessionId, "9999")
        );

        verify(session, never()).verifyPin();

        verify(atmSessionRepository, never())
                .save(any());
    }


    // =========================================================
    // requireAuthorizedSession()
    // =========================================================

    @Test
    void requireAuthorizedSession_shouldReturnSessionWhenPinVerified() {

        Long sessionId = 10L;

        AtmSession session = mock(AtmSession.class);

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.of(session));

        when(session.isPinVerified())
                .thenReturn(true);

        AtmSession result =
                atmSessionService.requireAuthorizedSession(sessionId);

        assertSame(session, result);
    }

    @Test
    void requireAuthorizedSession_shouldThrowWhenPinNotVerified() {

        Long sessionId = 10L;

        AtmSession session = mock(AtmSession.class);

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.of(session));

        when(session.isPinVerified())
                .thenReturn(false);

        assertThrows(
                AtmPinRequiredException.class,
                () -> atmSessionService.requireAuthorizedSession(sessionId)
        );
    }


    // =========================================================
    // closeSession()
    // =========================================================

    @Test
    void closeSession_shouldCloseSessionAndReturnCard() {

        Long sessionId = 10L;

        AtmSession session = mock(AtmSession.class);

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.of(session));

        atmSessionService.closeSession(sessionId);

        verify(session).close();
        verify(session).returnCard();
        verify(atmSessionRepository).save(session);
    }


    // =========================================================
    // getBalance()
    // =========================================================

    @Test
    void getBalance_shouldReturnAccountBalance() {

        Long sessionId = 10L;

        AtmSession session = mock(AtmSession.class);
        BankCard card = mock(BankCard.class);

        BigDecimal balance = new BigDecimal("15000.00");

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.of(session));

        when(session.isPinVerified())
                .thenReturn(true);

        when(session.getCard())
                .thenReturn(card);

        when(card.getAccountBalance())
                .thenReturn(balance);

        BigDecimal result =
                atmSessionService.getBalance(sessionId);

        assertEquals(0, balance.compareTo(result));
    }


    // =========================================================
    // withdraw()
    // =========================================================

    @Test
    void withdraw_shouldWithdrawSuccessfully() {

        Long sessionId = 10L;
        Long accountId = 100L;

        BigDecimal amount = new BigDecimal("5000.00");
        BigDecimal withdrawnToday = new BigDecimal("10000.00");

        AtmSession session = mock(AtmSession.class);
        BankCard card = mock(BankCard.class);
        BankAccount account = mock(BankAccount.class);

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.of(session));

        when(session.isPinVerified())
                .thenReturn(true);

        when(session.getCard())
                .thenReturn(card);

        when(card.getAccount())
                .thenReturn(account);

        when(account.getId())
                .thenReturn(accountId);

        when(transactionRepository.sumAmount(
                eq(accountId),
                eq(TransactionType.WITHDRAW),
                eq(TransactionSource.ATM),
                any(LocalDateTime.class)
        )).thenReturn(withdrawnToday);

        atmSessionService.withdraw(sessionId, amount);

        verify(bankAccountService).withdraw(
                accountId,
                amount,
                TransactionSource.ATM
        );
    }

    @Test
    void withdraw_shouldThrowWhenAmountBelowMinimum() {

        Long sessionId = 10L;

        AtmSession session = mock(AtmSession.class);

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.of(session));

        when(session.isPinVerified())
                .thenReturn(true);

        BigDecimal amount = new BigDecimal("50.00");

        assertThrows(
                AtmMinimumAmountException.class,
                () -> atmSessionService.withdraw(sessionId, amount)
        );

        verify(transactionRepository, never())
                .sumAmount(
                        anyLong(),
                        any(),
                        any(),
                        any()
                );

        verify(bankAccountService, never())
                .withdraw(anyLong(), any(), any());
    }

    @Test
    void withdraw_shouldThrowWhenDailyLimitExceeded() {

        Long sessionId = 10L;
        Long accountId = 100L;

        BigDecimal amount = new BigDecimal("10000.00");
        BigDecimal withdrawnToday = new BigDecimal("45000.00");

        AtmSession session = mock(AtmSession.class);
        BankCard card = mock(BankCard.class);
        BankAccount account = mock(BankAccount.class);

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.of(session));

        when(session.isPinVerified())
                .thenReturn(true);

        when(session.getCard())
                .thenReturn(card);

        when(card.getAccount())
                .thenReturn(account);

        when(account.getId())
                .thenReturn(accountId);

        when(transactionRepository.sumAmount(
                eq(accountId),
                eq(TransactionType.WITHDRAW),
                eq(TransactionSource.ATM),
                any(LocalDateTime.class)
        )).thenReturn(withdrawnToday);

        assertThrows(
                AtmDailyWithdrawalLimitException.class,
                () -> atmSessionService.withdraw(sessionId, amount)
        );

        verify(bankAccountService, never())
                .withdraw(anyLong(), any(), any());
    }

    @Test
    void withdraw_shouldAllowExactlyDailyLimit() {

        Long sessionId = 10L;
        Long accountId = 100L;

        BigDecimal amount = new BigDecimal("5000.00");
        BigDecimal withdrawnToday = new BigDecimal("45000.00");

        AtmSession session = mock(AtmSession.class);
        BankCard card = mock(BankCard.class);
        BankAccount account = mock(BankAccount.class);

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.of(session));

        when(session.isPinVerified())
                .thenReturn(true);

        when(session.getCard())
                .thenReturn(card);

        when(card.getAccount())
                .thenReturn(account);

        when(account.getId())
                .thenReturn(accountId);

        when(transactionRepository.sumAmount(
                eq(accountId),
                eq(TransactionType.WITHDRAW),
                eq(TransactionSource.ATM),
                any(LocalDateTime.class)
        )).thenReturn(withdrawnToday);

        atmSessionService.withdraw(sessionId, amount);

        verify(bankAccountService).withdraw(
                accountId,
                amount,
                TransactionSource.ATM
        );
    }


    // =========================================================
    // deposit()
    // =========================================================

    @Test
    void deposit_shouldDepositSuccessfully() {

        Long sessionId = 10L;
        Long accountId = 100L;

        BigDecimal amount = new BigDecimal("1000.00");

        AtmSession session = mock(AtmSession.class);
        BankCard card = mock(BankCard.class);
        BankAccount account = mock(BankAccount.class);

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.of(session));

        when(session.isPinVerified())
                .thenReturn(true);

        when(session.getCard())
                .thenReturn(card);

        when(card.getAccount())
                .thenReturn(account);

        when(account.getId())
                .thenReturn(accountId);

        atmSessionService.deposit(sessionId, amount);

        verify(bankAccountService).deposit(
                accountId,
                amount,
                TransactionSource.ATM
        );
    }

    @Test
    void deposit_shouldThrowWhenAmountBelowMinimum() {

        Long sessionId = 10L;

        AtmSession session = mock(AtmSession.class);

        when(atmSessionRepository
                .findByIdAndActiveTrueAndCardReturnedFalse(sessionId))
                .thenReturn(Optional.of(session));

        when(session.isPinVerified())
                .thenReturn(true);

        BigDecimal amount = new BigDecimal("50.00");

        assertThrows(
                AtmMinimumAmountException.class,
                () -> atmSessionService.deposit(sessionId, amount)
        );

        verify(bankAccountService, never())
                .deposit(anyLong(), any(), any());
    }
}
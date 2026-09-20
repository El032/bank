package com.example.bank.service;

import com.example.bank.actuator.AccountMetrics;
import com.example.bank.dto.CardCreatedResponse;
import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.exception.CardNotFoundException;
import com.example.bank.exception.InactiveAccountException;
import com.example.bank.exception.InvalidPinException;
import com.example.bank.exception.SavingsCardNotAllowedException;
import com.example.bank.model.AccountStatus;
import com.example.bank.model.BankAccount;
import com.example.bank.model.BankCard;
import com.example.bank.model.CreditAccount;
import com.example.bank.model.SavingsAccount;
import com.example.bank.model.CardStatus;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.BankCardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankCardServiceTest {

    @Mock
    private BankCardRepository bankCardRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @Mock
    private CvvGenerator cvvGenerator;

    @Mock
    private PinGenerator pinGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CvvEncryptionService cvvEncryptionService;

    @Mock
    private AccountMetrics accountMetrics;

    @InjectMocks
    private BankCardService bankCardService;


    // =========================================================
    // createCard()
    // =========================================================

    @Test
    void createCard_shouldCreateDebitCardSuccessfully() {

        Long accountId = 1L;

        BankAccount account = mock(BankAccount.class);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(account.getStatus())
                .thenReturn(AccountStatus.ACTIVE);

        when(cardNumberGenerator.generate(any()))
                .thenReturn("4111111111111111");

        when(pinGenerator.generate())
                .thenReturn("1234");

        when(cvvGenerator.generate())
                .thenReturn("123");

        when(passwordEncoder.encode("1234"))
                .thenReturn("hashed-pin");

        when(cvvEncryptionService.encrypt("123"))
                .thenReturn("encrypted-cvv");

        CardCreatedResponse result =
                bankCardService.createCard(accountId);

        assertNotNull(result);

        verify(accountRepository).findById(accountId);
        verify(cardNumberGenerator).generate(any());
        verify(pinGenerator).generate();
        verify(cvvGenerator).generate();
        verify(passwordEncoder).encode("1234");
        verify(cvvEncryptionService).encrypt("123");
        verify(bankCardRepository).save(any(BankCard.class));
        verify(accountMetrics).accountCardsCreated();
    }


    @Test
    void createCard_shouldThrowWhenAccountNotFound() {

        Long accountId = 999L;

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> bankCardService.createCard(accountId)
        );

        verify(bankCardRepository, never())
                .save(any());

        verify(accountMetrics, never())
                .accountCardsCreated();
    }


    @Test
    void createCard_shouldThrowWhenAccountInactive() {

        Long accountId = 1L;

        BankAccount account = mock(BankAccount.class);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(account.getStatus())
                .thenReturn(AccountStatus.BLOCKED);

        assertThrows(
                InactiveAccountException.class,
                () -> bankCardService.createCard(accountId)
        );

        verify(cardNumberGenerator, never())
                .generate(any());

        verify(bankCardRepository, never())
                .save(any());
    }


    @Test
    void createCard_shouldThrowWhenSavingsAccount() {

        Long accountId = 1L;

        SavingsAccount account = mock(SavingsAccount.class);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(account.getStatus())
                .thenReturn(AccountStatus.ACTIVE);

        assertThrows(
                SavingsCardNotAllowedException.class,
                () -> bankCardService.createCard(accountId)
        );

        verify(cardNumberGenerator, never())
                .generate(any());

        verify(bankCardRepository, never())
                .save(any());
    }


    @Test
    void createCard_shouldCreateCreditCard() {

        Long accountId = 2L;

        CreditAccount account = mock(CreditAccount.class);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(account.getStatus())
                .thenReturn(AccountStatus.ACTIVE);

        when(cardNumberGenerator.generate(any()))
                .thenReturn("5555555555554444");

        when(pinGenerator.generate())
                .thenReturn("4321");

        when(cvvGenerator.generate())
                .thenReturn("456");

        when(passwordEncoder.encode("4321"))
                .thenReturn("hashed-pin");

        when(cvvEncryptionService.encrypt("456"))
                .thenReturn("encrypted-cvv");

        CardCreatedResponse result =
                bankCardService.createCard(accountId);

        assertNotNull(result);

        verify(cardNumberGenerator).generate(any());
        verify(bankCardRepository).save(any(BankCard.class));
        verify(accountMetrics).accountCardsCreated();
    }


    // =========================================================
    // getCardByAccountId()
    // =========================================================

    @Test
    void getCardByAccountId_shouldReturnCard() {

        Long accountId = 1L;

        BankAccount account = mock(BankAccount.class);
        BankCard card = mock(BankCard.class);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(bankCardRepository.findByAccountId(accountId))
                .thenReturn(Optional.of(card));

        BankCard result =
                bankCardService.getCardByAccountId(accountId);

        assertSame(card, result);

        verify(accountRepository).findById(accountId);
        verify(bankCardRepository).findByAccountId(accountId);
    }


    @Test
    void getCardByAccountId_shouldThrowWhenAccountNotFound() {

        Long accountId = 999L;

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> bankCardService.getCardByAccountId(accountId)
        );

        verify(bankCardRepository, never())
                .findByAccountId(anyLong());
    }


    @Test
    void getCardByAccountId_shouldThrowWhenCardNotFound() {

        Long accountId = 1L;

        BankAccount account = mock(BankAccount.class);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(bankCardRepository.findByAccountId(accountId))
                .thenReturn(Optional.empty());

        assertThrows(
                CardNotFoundException.class,
                () -> bankCardService.getCardByAccountId(accountId)
        );
    }


    // =========================================================
    // getCvv()
    // =========================================================

    @Test
    void getCvv_shouldDecryptAndReturnCvv() {

        Long accountId = 1L;

        BankAccount account = mock(BankAccount.class);
        BankCard card = mock(BankCard.class);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(bankCardRepository.findByAccountId(accountId))
                .thenReturn(Optional.of(card));

        when(card.getCvvEncrypted())
                .thenReturn("encrypted-cvv");

        when(cvvEncryptionService.decrypt("encrypted-cvv"))
                .thenReturn("123");

        String result =
                bankCardService.getCvv(accountId);

        assertEquals("123", result);

        verify(cvvEncryptionService)
                .decrypt("encrypted-cvv");
    }


    // =========================================================
    // checkPin()
    // =========================================================

    @Test
    void checkPin_shouldReturnTrueForCorrectPin() {

        BankCard card = mock(BankCard.class);

        when(card.getPinHash())
                .thenReturn("hashed-pin");

        when(passwordEncoder.matches(
                "1234",
                "hashed-pin"
        )).thenReturn(true);

        boolean result =
                bankCardService.checkPin(card, "1234");

        assertTrue(result);

        verify(passwordEncoder)
                .matches("1234", "hashed-pin");
    }


    @Test
    void checkPin_shouldReturnFalseForWrongPin() {

        BankCard card = mock(BankCard.class);

        when(card.getPinHash())
                .thenReturn("hashed-pin");

        when(passwordEncoder.matches(
                "9999",
                "hashed-pin"
        )).thenReturn(false);

        boolean result =
                bankCardService.checkPin(card, "9999");

        assertFalse(result);
    }


    // =========================================================
    // changePin()
    // =========================================================

    @Test
    void changePin_shouldChangePinForUserWithCorrectOldPin() {

        Long accountId = 1L;

        BankAccount account = mock(BankAccount.class);
        BankCard card = mock(BankCard.class);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(bankCardRepository.findByAccountId(accountId))
                .thenReturn(Optional.of(card));

        when(card.getPinHash())
                .thenReturn("old-hash");

        when(passwordEncoder.matches(
                "1111",
                "old-hash"
        )).thenReturn(true);

        when(passwordEncoder.encode("2222"))
                .thenReturn("new-hash");

        bankCardService.changePin(
                accountId,
                "1111",
                "2222",
                false
        );

        verify(passwordEncoder)
                .matches("1111", "old-hash");

        verify(passwordEncoder)
                .encode("2222");

        verify(card)
                .setPinHash("new-hash");

        verify(bankCardRepository)
                .save(card);
    }


    @Test
    void changePin_shouldThrowWhenOldPinIsWrong() {

        Long accountId = 1L;

        BankAccount account = mock(BankAccount.class);
        BankCard card = mock(BankCard.class);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(bankCardRepository.findByAccountId(accountId))
                .thenReturn(Optional.of(card));

        when(card.getPinHash())
                .thenReturn("old-hash");

        when(passwordEncoder.matches(
                "9999",
                "old-hash"
        )).thenReturn(false);

        assertThrows(
                InvalidPinException.class,
                () -> bankCardService.changePin(
                        accountId,
                        "9999",
                        "2222",
                        false
                )
        );

        verify(card, never())
                .setPinHash(any());

        verify(bankCardRepository, never())
                .save(any());
    }


    @Test
    void changePin_shouldAllowAdminWithoutOldPinCheck() {

        Long accountId = 1L;

        BankAccount account = mock(BankAccount.class);
        BankCard card = mock(BankCard.class);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(bankCardRepository.findByAccountId(accountId))
                .thenReturn(Optional.of(card));

        when(passwordEncoder.encode("2222"))
                .thenReturn("new-hash");

        bankCardService.changePin(
                accountId,
                "wrong-old-pin",
                "2222",
                true
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(passwordEncoder)
                .encode("2222");

        verify(card)
                .setPinHash("new-hash");

        verify(bankCardRepository)
                .save(card);
    }


    // =========================================================
    // updateStatus()
    // =========================================================

    @Test
    void updateStatus_shouldActivateCard() {

        Long cardId = 1L;

        BankCard card = mock(BankCard.class);

        when(bankCardRepository.findById(cardId))
                .thenReturn(Optional.of(card));

        when(bankCardRepository.save(card))
                .thenReturn(card);

        BankCard result =
                bankCardService.updateStatus(
                        cardId,
                        CardStatus.ACTIVE
                );

        assertSame(card, result);

        verify(card).activate(cardId);
        verify(bankCardRepository).save(card);
    }


    @Test
    void updateStatus_shouldBlockCard() {

        Long cardId = 1L;

        BankCard card = mock(BankCard.class);

        when(bankCardRepository.findById(cardId))
                .thenReturn(Optional.of(card));

        when(bankCardRepository.save(card))
                .thenReturn(card);

        BankCard result =
                bankCardService.updateStatus(
                        cardId,
                        CardStatus.BLOCKED
                );

        assertSame(card, result);

        verify(card).block(cardId);
        verify(bankCardRepository).save(card);
    }


    @Test
    void updateStatus_shouldCloseCard() {

        Long cardId = 1L;

        BankCard card = mock(BankCard.class);

        when(bankCardRepository.findById(cardId))
                .thenReturn(Optional.of(card));

        when(bankCardRepository.save(card))
                .thenReturn(card);

        BankCard result =
                bankCardService.updateStatus(
                        cardId,
                        CardStatus.CLOSED
                );

        assertSame(card, result);

        verify(card).close(cardId);
        verify(bankCardRepository).save(card);
    }


    @Test
    void updateStatus_shouldThrowWhenCardNotFound() {

        Long cardId = 999L;

        when(bankCardRepository.findById(cardId))
                .thenReturn(Optional.empty());

        assertThrows(
                CardNotFoundException.class,
                () -> bankCardService.updateStatus(
                        cardId,
                        CardStatus.ACTIVE
                )
        );

        verify(bankCardRepository, never())
                .save(any());
    }
}


package com.example.bank.service;

import com.example.bank.model.BankAccount;
import com.example.bank.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountCacheServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AccountCacheService accountCacheService;



    @Test
    void getAccount_shouldReturnAccountFromCache() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Long accountId = 1L;
        BankAccount account = mock(BankAccount.class);

        when(valueOperations.get("account:" + accountId))
                .thenReturn(account);

        BankAccount result = accountCacheService.getAccount(accountId);

        assertSame(account, result);

        verify(valueOperations).get("account:" + accountId);
        verify(accountRepository, never()).findById(anyLong());
        verify(valueOperations, never())
                .set(anyString(), any(), any());
    }

    @Test
    void getAccount_shouldLoadFromDatabaseAndPutIntoCacheWhenCacheIsEmpty() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Long accountId = 1L;
        BankAccount account = mock(BankAccount.class);

        when(valueOperations.get("account:" + accountId))
                .thenReturn(null);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        BankAccount result = accountCacheService.getAccount(accountId);

        assertSame(account, result);

        verify(valueOperations).get("account:" + accountId);

        verify(accountRepository).findById(accountId);

        verify(valueOperations).set(
                eq("account:" + accountId),
                same(account),
                eq(java.time.Duration.ofMinutes(5))
        );
    }

    @Test
    void getAccount_shouldThrowWhenAccountNotFound() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Long accountId = 999L;

        when(valueOperations.get("account:" + accountId))
                .thenReturn(null);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> accountCacheService.getAccount(accountId)
        );

        assertEquals(
                "Счёт не найден: " + accountId,
                exception.getMessage()
        );

        verify(valueOperations).get("account:" + accountId);
        verify(accountRepository).findById(accountId);

        verify(valueOperations, never())
                .set(anyString(), any(), any());
    }

    @Test
    void getAccount_shouldGoToDatabaseWhenCachedObjectIsNotBankAccount() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Long accountId = 1L;
        BankAccount account = mock(BankAccount.class);

        Object cachedObject = "not a bank account";

        when(valueOperations.get("account:" + accountId))
                .thenReturn(cachedObject);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        BankAccount result = accountCacheService.getAccount(accountId);

        assertSame(account, result);

        verify(valueOperations).get("account:" + accountId);
        verify(accountRepository).findById(accountId);

        verify(valueOperations).set(
                eq("account:" + accountId),
                same(account),
                eq(java.time.Duration.ofMinutes(5))
        );
    }

    @Test
    void evictAccount_shouldDeleteCorrectCacheKey() {

        Long accountId = 1L;

        accountCacheService.evictAccount(accountId);

        verify(redisTemplate).delete("account:" + accountId);
    }

    @Test
    void debugCacheKeys_shouldPrintKeysWhenKeysExist() {

        Set<String> keys = new HashSet<>();
        keys.add("account:1");
        keys.add("account:2");

        when(redisTemplate.keys("account:*"))
                .thenReturn(keys);

        assertDoesNotThrow(() ->
                accountCacheService.debugCacheKeys()
        );

        verify(redisTemplate).keys("account:*");
    }

    @Test
    void debugCacheKeys_shouldDoNothingWhenKeysAreNull() {

        when(redisTemplate.keys("account:*"))
                .thenReturn(null);

        assertDoesNotThrow(() ->
                accountCacheService.debugCacheKeys()
        );

        verify(redisTemplate).keys("account:*");
    }
}
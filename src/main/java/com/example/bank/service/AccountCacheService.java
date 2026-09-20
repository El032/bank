package com.example.bank.service;

import com.example.bank.model.BankAccount;
import com.example.bank.repository.AccountRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class AccountCacheService {

    private static final String CACHE_PREFIX = "account:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final AccountRepository accountRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public AccountCacheService(AccountRepository accountRepository,
                               RedisTemplate<String, Object> redisTemplate) {
        this.accountRepository = accountRepository;
        this.redisTemplate = redisTemplate;
    }

    // Cache Aside паттерн вручную
    public BankAccount getAccount(Long id) {
        String key = CACHE_PREFIX + id;

        // 1. Смотрим в кэш
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof BankAccount account) {
            return account;
        }

        // 2. Кэш пуст — идём в БД
        BankAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Счёт не найден: " + id));

        // 3. Кладём в кэш с TTL
        redisTemplate.opsForValue().set(key, account, TTL);

        return account;
    }

    // Инвалидация при изменении
    public void evictAccount(Long id) {
        redisTemplate.delete(CACHE_PREFIX + id);
    }

    // Посмотреть все ключи (для отладки)
    public void debugCacheKeys() {
        var keys = redisTemplate.keys(CACHE_PREFIX + "*");
        if (keys != null) {
            keys.forEach(key -> System.out.println("Redis key: " + key));
        }
    }
}
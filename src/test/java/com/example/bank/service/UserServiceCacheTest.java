package com.example.bank.service;

import com.example.bank.config.TestcontainersConfig;
import com.example.bank.dto.UserResponse;
import com.example.bank.model.User;
import com.example.bank.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;


import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfig.class)
class UserServiceCacheTest {


//    static GenericContainer<?> redis =
//            new GenericContainer<>("redis:7")
//                    .withExposedPorts(6379);
//
//    static {
//        redis.start();
//    }
//
//    @DynamicPropertySource
//    static void redisProperties(DynamicPropertyRegistry registry) {
//        registry.add(
//                "spring.data.redis.host",
//                redis::getHost
//        );
//
//        registry.add(
//                "spring.data.redis.port",
//                () -> redis.getMappedPort(6379)
//        );
//    }


    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void findUserResponseById_shouldCacheResult() {

        // Создаём пользователя
        User saved = userRepository.save(
                new User(
                        "eldar",
                        "e@bank.ru",
                        "Эльдар",
                        "hashed"
                )
        );

        // Очищаем кэш перед тестом
        Cache cache = cacheManager.getCache("users");
        assertNotNull(cache);
        cache.evict(saved.getId());

        // Первый вызов — Redis ещё пуст
        UserResponse first =
                userService.findUserResponseById(saved.getId());

        assertNotNull(first);
        assertEquals(saved.getId() + 1, first.getId());

        // Проверяем, что значение появилось в кэше
        Cache.ValueWrapper cached =
                cache.get(saved.getId());

        assertNotNull(cached);
        assertNotNull(cached.get());

        // Второй вызов — значение должно прийти из кэша
        UserResponse second =
                userService.findUserResponseById(saved.getId());

        assertNotNull(second);
        assertEquals(first.getId(), second.getId());
        assertEquals(first.getUserName(), second.getUserName());
        assertEquals(first.getEmail(), second.getEmail());
    }
}
package com.example.bank.config;

import com.example.bank.dto.UserResponse;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory factory,
            ObjectMapper objectMapper) {

        // Общий serializer для кэшей,
        // где нет необходимости в конкретном типизированном JSON.
        GenericJackson2JsonRedisSerializer genericSerializer =
                new GenericJackson2JsonRedisSerializer();

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(genericSerializer)
                        )
                        .disableCachingNullValues();

        // ---------------------------------------------------------
        // Кэш списка пользователей
        // key: users-list::all
        // value: List<UserResponse>
        // ---------------------------------------------------------

        JavaType usersListType =
                objectMapper.getTypeFactory()
                        .constructCollectionType(
                                List.class,
                                UserResponse.class
                        );

        Jackson2JsonRedisSerializer<List<UserResponse>> usersListSerializer =
                new Jackson2JsonRedisSerializer<>(
                        objectMapper,
                        usersListType
                );

        RedisCacheConfiguration usersListConfig =
                defaultConfig
                        .entryTtl(Duration.ofMinutes(15))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(usersListSerializer)
                        );

        // ---------------------------------------------------------
        // Кэш одного пользователя
        // key: users::id
        // value: UserResponse
        //
        // Здесь оставляем GenericJackson2JsonRedisSerializer,
        // потому что UserResponse — конкретный объект.
        // ---------------------------------------------------------

        RedisCacheConfiguration usersConfig =
                defaultConfig
                        .entryTtl(Duration.ofMinutes(15));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)

                // Кэш счетов
                .withCacheConfiguration(
                        "accounts",
                        defaultConfig.entryTtl(Duration.ofMinutes(5))
                )

                // Один пользователь
                .withCacheConfiguration(
                        "users",
                        usersConfig
                )

                // Список пользователей
                .withCacheConfiguration(
                        "users-list",
                        usersListConfig
                )

                // Курсы валют
                .withCacheConfiguration(
                        "exchange-rates",
                        defaultConfig.entryTtl(Duration.ofHours(1))
                )

                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template =
                new RedisTemplate<>();

        template.setConnectionFactory(factory);

        template.setKeySerializer(
                new StringRedisSerializer()
        );

        template.setValueSerializer(
                new GenericJackson2JsonRedisSerializer()
        );

        template.setHashKeySerializer(
                new StringRedisSerializer()
        );

        template.setHashValueSerializer(
                new GenericJackson2JsonRedisSerializer()
        );

        template.afterPropertiesSet();

        return template;
    }
}


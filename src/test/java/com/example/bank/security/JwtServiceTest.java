package com.example.bank.security;

import com.example.bank.config.BankProperties;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@DisplayName("JwtService — тесты токенов")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        BankProperties properties = new BankProperties();
        BankProperties.Jwt jwt = new BankProperties.Jwt();
        jwt.setSecret("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        jwt.setExpirationMs(86400000L);
        properties.setJwt(jwt);

        jwtService = new JwtService(properties);
    }

    @Test
    @DisplayName("Генерация токена возвращает непустую строку")
    void generateToken_shouldReturnNonEmptyString() {
        String token = jwtService.generateToken("eldar");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("Из токена можно извлечь имя пользователя")
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateToken("eldar");
        assertEquals("eldar", jwtService.extractUsername(token));
    }



    @Test
    @DisplayName("Валидный токен проходит проверку")
    void isTokenValid_validToken_shouldReturnTrue() {
        UserDetails user = User.withUsername("eldar")
                .password("123")
                .roles("USER")
                .build();

        String token = jwtService.generateToken("eldar");

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    @DisplayName("Испорченный токен не проходит проверку")
    void isTokenValid_tamperedToken_shouldReturnFalse() {

        UserDetails user = User.withUsername("eldar")
                .password("123")
                .roles("USER")
                .build();

        String token = jwtService.generateToken("eldar");
        String tampered = token + "invalid";

        assertFalse(jwtService.isTokenValid(tampered, user));
    }

    @Test
    @DisplayName("Истёкший токен не проходит проверку")
    void isTokenValid_expiredToken_shouldReturnFalse() {
        // Создаём сервис с нулевым сроком жизни токена
        BankProperties properties = new BankProperties();
        BankProperties.Jwt jwt = new BankProperties.Jwt();

        jwt.setSecret("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        jwt.setExpirationMs(-1000L);

        properties.setJwt(jwt);

        JwtService expiredService = new JwtService(properties);

        UserDetails user = User.withUsername("eldar")
                .password("123")
                .roles("USER")
                .build();

        String token = expiredService.generateToken("eldar");

        assertFalse(expiredService.isTokenValid(token, user));
    }

    @Test
    @DisplayName("Разные пользователи получают разные токены")
    void generateToken_differentUsers_shouldGetDifferentTokens() {
        String token1 = jwtService.generateToken("eldar");
        String token2 = jwtService.generateToken("kola");
        assertNotEquals(token1, token2);
    }
}
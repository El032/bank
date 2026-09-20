package com.example.bank.security;

import com.example.bank.config.BankProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HexFormat;

@Service
public class JwtService {

    private final BankProperties properties;

    public JwtService(BankProperties properties) {
        this.properties = properties;
    }

    // Получить секретный ключ из конфига
    private SecretKey getSigningKey() {
        byte[] keyBytes = HexFormat.of()
                .parseHex(properties.getJwt().getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Создать JWT токен для пользователя
    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getJwt().getExpirationMs());

        return Jwts.builder()
                .subject(username)                    // sub — имя пользователя
                .issuedAt(now)                        // iat — время создания
                .expiration(expiry)                   // exp — время истечения
                .signWith(getSigningKey())             // подписать ключом
                .compact();                           // собрать в строку
    }

    // Достать имя пользователя из токена
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // Достать роль из токена
    // public String extractRole(String token) {
//        return parseClaims(token).get("role", String.class);
//    }

    // Проверить, не истёк ли токен
    public boolean isTokenValid(String token,UserDetails userDetails) {
        try {
            Claims claims = parseClaims(token);

            String username = claims.getSubject();

            return username.equals(userDetails.getUsername())
                    &&!claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return false; // любая ошибка парсинга — токен невалиден
        }
    }

    // Распарсить токен и получить claims
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // проверяем подпись
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
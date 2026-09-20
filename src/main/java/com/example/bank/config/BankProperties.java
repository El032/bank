package com.example.bank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bank")
public class BankProperties {

    private Jwt jwt = new Jwt();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public static class Jwt {

        private String secret;
        private long expirationMs;



        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpirationMs() {
            return expirationMs;
        }

        public void setExpirationMs(long expirationMs) {
            this.expirationMs = expirationMs;
        }

    }
}
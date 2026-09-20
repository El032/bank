package com.example.bank.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank API")
                        .description("REST API банковского приложения — учебный проект")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Эльдар")
                                .email("eldar@bank.ru"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                // Добавляем схему JWT-аутентификации в Swagger UI
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Вставь JWT токен, полученный через /auth/login")));
    }
}
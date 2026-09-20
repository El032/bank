package com.example.bank.controller;

import com.example.bank.config.TestcontainersConfig;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@DisplayName("AuthController — интеграционные тесты")
class AuthControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtService jwtService;


    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }


    @Test
    @DisplayName("POST /auth/register — успешная регистрация")
    void register_shouldReturn201() throws Exception {

        String body = """
                {
                  "userName":"eldar",
                  "email":"eldar@test.com",
                  "password":"password",
                  "fullName":"Eldar Test"
                }
                """;


        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                        .value("Пользователь зарегистрирован"))
                .andExpect(jsonPath("$.username")
                        .value("eldar"));
    }


    @Test
    @DisplayName("POST /auth/register — повторный username возвращает 409")
    void register_duplicateUsername_shouldReturn409() throws Exception {

        String body = """
                {
                  "userName":"eldar",
                  "email":"eldar@test.com",
                  "password":"password",
                  "fullName":"Eldar Test"
                }
                """;


        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "userName":"eldar",
                          "email":"new@test.com",
                          "password":"password",
                          "fullName":"Another"
                        }
                        """))
                .andExpect(status().isConflict());
    }



    @Test
    @DisplayName("POST /auth/register — плохие данные возвращают 400")
    void register_invalidData_shouldReturn400() throws Exception {

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "userName":"",
                          "email":"bad",
                          "password":"",
                          "fullName":""
                        }
                        """))
                .andExpect(status().isBadRequest());
    }



    @Test
    @DisplayName("POST /auth/login — успешный вход возвращает JWT")
    void login_shouldReturnToken() throws Exception {


        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "userName":"eldar",
                  "email":"eldar@test.com",
                  "password":"password",
                  "fullName":"Eldar"
                }
                """));


        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "userName":"eldar",
                          "password":"password"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type")
                        .value("Bearer"));
    }



    @Test
    @DisplayName("GET /auth/me — без токена возвращает 401")
    void me_withoutToken_shouldReturn401() throws Exception {

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }



    @Test
    @DisplayName("GET /auth/me — с JWT возвращает пользователя")
    void me_withToken_shouldReturn200() throws Exception {


        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
              "userName":"eldar",
              "email":"eldar@test.com",
              "password":"password",
              "fullName":"Eldar"
            }
            """))
                .andExpect(status().isCreated());


        String token = jwtService.generateToken("eldar");


        mockMvc.perform(get("/auth/me")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username")
                        .value("eldar"));
    }

}
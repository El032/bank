package com.example.bank.controller;

import com.example.bank.config.TestcontainersConfig;
import com.example.bank.model.BankAccount;
import com.example.bank.model.BankCard;
import com.example.bank.model.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.BankCardRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.JwtService;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
@SpringBootTest
class AdminCardControllerIntegrationTest {


    @Autowired
    private WebApplicationContext context;


    private MockMvc mockMvc;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private BankCardRepository bankCardRepository;


    @Autowired
    private JwtService jwtService;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BankCardRepository cardRepository;

    private BankCard bankCard;


    private String adminToken;

    private String userToken;

    private BankCard card;



    @BeforeEach
    void setUp() {

        mockMvc =
                MockMvcBuilders
                        .webAppContextSetup(context)
                        .apply(springSecurity())
                        .build();


        User admin =
                userRepository.findByUserName("admin")
                        .orElseGet(() ->
                                userRepository.save(
                                        new User(
                                                "admin",
                                                "admin@test.local",
                                                "Admin",
                                                "test"
                                        )
                                )
                        );

        admin.setRole("ADMIN");

        userRepository.save(admin);


        adminToken =
                jwtService.generateToken(
                        admin.getUserName()
                );


        User user =
                userRepository.findByUserName("user")
                        .orElseGet(() ->
                                userRepository.save(
                                        new User(
                                                "user",
                                                "user@test.local",
                                                "User",
                                                "test"
                                        )
                                )
                        );


        userToken =
                jwtService.generateToken(
                        user.getUserName()
                );


        BankAccount account =
                new BankAccount("admin");

        account.setUser(admin);

        account.deposit(
                new BigDecimal("5000")
        );

        accountRepository.save(account);



        String uniqueCardNumber =
                "123456789012" +
                        String.format("%04d",
                                System.currentTimeMillis() % 10000
                        );


        card =
                new BankCard(
                        uniqueCardNumber,
                        "hashed-pin",
                        LocalDate.now().plusYears(3),
                        "encrypted-cvv",
                        account
                );


        cardRepository.save(card);

    }





    @Test
    @DisplayName("ADMIN меняет статус ACTIVE -> BLOCKED")
    void updateStatus_shouldReturn200() throws Exception {


        String body = """
        {
          "status":"BLOCKED"
        }
        """;


        mockMvc.perform(
                        patch(
                                "/admin/cards/{id}/status",
                                card.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )


                .andExpect(status().isOk())

                .andExpect(jsonPath("$.status")
                        .value("BLOCKED"));

    }





    @Test
    @DisplayName("USER не может менять статус карты")
    void user_shouldReturn403() throws Exception {


        String body = """
        {
          "status":"BLOCKED"
        }
        """;


        mockMvc.perform(
                        patch(
                                "/admin/cards/{id}/status",
                                card.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )


                .andExpect(status().isForbidden());

    }





    @Test
    @DisplayName("Без токена возвращает 401")
    void withoutToken_shouldReturn401() throws Exception {


        String body = """
        {
          "status":"BLOCKED"
        }
        """;


        mockMvc.perform(
                        patch(
                                "/admin/cards/{id}/status",
                                card.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )


                .andExpect(status().isUnauthorized());

    }





    @Test
    @DisplayName("Карта не найдена возвращает 404")
    void cardNotFound_shouldReturn404() throws Exception {


        String body = """
        {
          "status":"BLOCKED"
        }
        """;


        mockMvc.perform(
                        patch(
                                "/admin/cards/999999/status"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )


                .andExpect(status().isNotFound());

    }





    @Test
    @DisplayName("Пустой статус возвращает 400")
    void nullStatus_shouldReturn400() throws Exception {


        String body = """
        {
          "status":null
        }
        """;


        mockMvc.perform(
                        patch(
                                "/admin/cards/{id}/status",
                                card.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )


                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Нельзя изменить статус закрытой карты")
    void updateStatus_closedCard_shouldReturn422() throws Exception {

        // сначала закрываем карту
        card.close(card.getId());

        cardRepository.save(card);


        String body = """
    {
      "status":"ACTIVE"
    }
    """;


        mockMvc.perform(
                        patch(
                                "/admin/cards/{id}/status",
                                card.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )


                .andExpect(status().isUnprocessableEntity());

    }


}
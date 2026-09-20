package com.example.bank.controller;

import com.example.bank.model.AccountStatus;
import com.example.bank.model.BankAccount;
import com.example.bank.model.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.JwtService;
import com.example.bank.dto.UpdateAccountStatusRequest;
import com.example.bank.repository.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
class AdminAccountControllerIntegrationTest {


    @Autowired
    private WebApplicationContext context;


    private MockMvc mockMvc;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private AccountRepository accountRepository;


    @Autowired
    private JwtService jwtService;


    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private ObjectMapper objectMapper;


    private String adminToken;
    private String userToken;

    private BankAccount account;




    @BeforeEach
    void setUp() {


        mockMvc =
                MockMvcBuilders
                        .webAppContextSetup(context)
                        .apply(springSecurity())
                        .build();



        User admin =
                userRepository.findByUserName("admin")
                        .orElseThrow();


        admin.setRole("ADMIN");

        userRepository.save(admin);



        adminToken =
                jwtService.generateToken(
                        admin.getUserName()
                );



        User user =
                userRepository.findByUserName("user")
                        .orElseThrow();


        userToken =
                jwtService.generateToken(
                        user.getUserName()
                );



        account = new BankAccount("admin");

        account.setUser(admin);

        account.deposit(
                new BigDecimal("5000")
        );

        account =
                accountRepository.save(account);

    }



    @Test
    @DisplayName("ADMIN deposit возвращает 200")
    void deposit_shouldReturn200() throws Exception {


        String body = """
        {
          "amount":1000
        }
        """;


        mockMvc.perform(post(
                        "/admin/accounts/{id}/deposit",
                        account.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.balance")
                        .value(6000));

    }




    @Test
    @DisplayName("ADMIN withdraw возвращает 200")
    void withdraw_shouldReturn200() throws Exception {


        String body = """
        {
          "amount":1000
        }
        """;


        mockMvc.perform(post(
                        "/admin/accounts/{id}/withdraw",
                        account.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))


                .andExpect(status().isOk())

                .andExpect(jsonPath("$.balance")
                        .value(4000));

    }




    @Test
    @DisplayName("USER не может deposit")
    void userDeposit_shouldReturn403() throws Exception {


        String body = """
        {
          "amount":1000
        }
        """;


        mockMvc.perform(post(
                        "/admin/accounts/{id}/deposit",
                        account.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))


                .andExpect(status().isForbidden());

    }




    @Test
    @DisplayName("USER не может withdraw")
    void userWithdraw_shouldReturn403() throws Exception {


        String body = """
        {
          "amount":1000
        }
        """;


        mockMvc.perform(post(
                        "/admin/accounts/{id}/withdraw",
                        account.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))


                .andExpect(status().isForbidden());

    }




    @Test
    @DisplayName("ADMIN меняет статус ACTIVE -> BLOCKED")
    void updateStatus_shouldReturn200() throws Exception {


        String body = """
        {
          "status":"BLOCKED"
        }
        """;


        mockMvc.perform(patch(
                        "/admin/accounts/{id}/status",
                        account.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))


                .andExpect(status().isOk())

                .andExpect(jsonPath("$.status")
                        .value("BLOCKED"));

    }





    @Test
    @DisplayName("Deposit несуществующего счета возвращает 404")
    void deposit_notFound_shouldReturn404() throws Exception {


        String body = """
        {
          "amount":1000
        }
        """;


        mockMvc.perform(post(
                        "/admin/accounts/999999/deposit"
                )
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))


                .andExpect(status().isNotFound());

    }





    @Test
    @DisplayName("Withdraw несуществующего счета возвращает 404")
    void withdraw_notFound_shouldReturn404() throws Exception {


        String body = """
        {
          "amount":1000
        }
        """;


        mockMvc.perform(post(
                        "/admin/accounts/999999/withdraw"
                )
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))


                .andExpect(status().isNotFound());

    }





    @Test
    @DisplayName("Недостаточно средств возвращает 422")
    void withdraw_insufficientFunds_shouldReturn422() throws Exception {


        String body = """
        {
          "amount":10000
        }
        """;


        mockMvc.perform(post(
                        "/admin/accounts/{id}/withdraw",
                        account.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))


                .andExpect(status().isUnprocessableEntity());

    }





    @Test
    @DisplayName("Отрицательная сумма deposit возвращает 400")
    void deposit_negativeAmount_shouldReturn400() throws Exception {


        String body = """
        {
          "amount":-100
        }
        """;


        mockMvc.perform(post(
                        "/admin/accounts/{id}/deposit",
                        account.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))


                .andExpect(status().isBadRequest());

    }





    @Test
    @DisplayName("Без токена возвращает 401")
    void withoutToken_shouldReturn401() throws Exception {


        String body = """
        {
          "amount":1000
        }
        """;


        mockMvc.perform(post(
                        "/admin/accounts/{id}/deposit",
                        account.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))


                .andExpect(status().isUnauthorized());

    }


}
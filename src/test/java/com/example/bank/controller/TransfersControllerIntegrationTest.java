package com.example.bank.controller;

import com.example.bank.config.TestcontainersConfig;
import com.example.bank.model.BankAccount;
import com.example.bank.model.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.TransferRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@DisplayName("TransfersController — интеграционные тесты")
class TransfersControllerIntegrationTest {


    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    TransferRepository transferRepository;


    private String userToken;
    private String adminToken;


    @BeforeEach
    void setUp() {

        transferRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();


        User user = new User(
                "user",
                "user@test.com",
                "Test User",
                "password"
        );

        user.setRole("USER");


        User admin = new User(
                "admin",
                "admin@test.com",
                "Admin",
                "password"
        );

        admin.setRole("ADMIN");


        userRepository.save(user);
        userRepository.save(admin);


        userToken = jwtService.generateToken("user");
        adminToken = jwtService.generateToken("admin");
    }



    @Test
    @DisplayName("POST /transfers — ADMIN успешно создаёт перевод")
    void transfer_asAdmin_shouldReturn201() throws Exception {


        User user = userRepository.findByUserName("user")
                .orElseThrow();


        BankAccount from =
                new BankAccount("user");

        from.setUser(user);
        from.deposit(new BigDecimal("1000"));


        BankAccount to =
                new BankAccount("user");

        to.setUser(user);


        accountRepository.save(from);
        accountRepository.save(to);



        String body = """
                {
                  "fromAccountId": %d,
                  "toAccountId": %d,
                  "amount": 100
                }
                """.formatted(
                from.getId(),
                to.getId()
        );


        mockMvc.perform(post("/transfers")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromAccountId")
                        .value(from.getId()))
                .andExpect(jsonPath("$.toAccountId")
                        .value(to.getId()));
    }



    @Test
    @DisplayName("POST /transfers — без токена возвращает 401")
    void transfer_withoutToken_shouldReturn401() throws Exception {


        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "fromAccountId":1,
                          "toAccountId":2,
                          "amount":100
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }



    @Test
    @DisplayName("POST /transfers — USER не может отправить с чужого счёта")
    void transfer_notOwner_shouldReturn403() throws Exception {


        User another = new User(
                "another",
                "another@test.com",
                "Another",
                "password"
        );

        another.setRole("USER");

        userRepository.save(another);


        BankAccount account =
                new BankAccount("another");

        account.setUser(another);

        account.deposit(new BigDecimal("1000"));

        accountRepository.save(account);



        BankAccount target =
                new BankAccount("user");

        User user =
                userRepository.findByUserName("user")
                        .orElseThrow();

        target.setUser(user);

        accountRepository.save(target);



        mockMvc.perform(post("/transfers")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "fromAccountId": %d,
                          "toAccountId": %d,
                          "amount":100
                        }
                        """.formatted(
                                account.getId(),
                                target.getId()
                        )))
                .andExpect(status().isForbidden());
    }



    @Test
    @DisplayName("POST /transfers — несуществующий счёт возвращает 404")
    void transfer_accountNotFound_shouldReturn404() throws Exception {


        mockMvc.perform(post("/transfers")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "fromAccountId":9999,
                          "toAccountId":8888,
                          "amount":100
                        }
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /transfers — USER успешно переводит со своего счета")
    void transfer_asOwner_shouldReturn201() throws Exception {

        User user = userRepository.findByUserName("user")
                .orElseThrow();


        BankAccount from =
                new BankAccount("user");

        from.setUser(user);
        from.deposit(new BigDecimal("1000"));


        BankAccount to =
                new BankAccount("user");

        to.setUser(user);


        accountRepository.save(from);
        accountRepository.save(to);


        String body = """
            {
              "fromAccountId": %d,
              "toAccountId": %d,
              "amount": 100
            }
            """.formatted(
                from.getId(),
                to.getId()
        );


        mockMvc.perform(post("/transfers")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromAccountId")
                        .value(from.getId()))
                .andExpect(jsonPath("$.toAccountId")
                        .value(to.getId()))
                .andExpect(jsonPath("$.amount")
                        .value(100));
    }

    @Test
    @DisplayName("POST /transfers — недостаточно средств возвращает 422")
    void transfer_notEnoughMoney_shouldReturn422() throws Exception {

        User user = userRepository.findByUserName("user")
                .orElseThrow();


        BankAccount from =
                new BankAccount("user");

        from.setUser(user);

        // денег меньше, чем переводим
        from.deposit(new BigDecimal("50"));


        BankAccount to =
                new BankAccount("user");

        to.setUser(user);


        accountRepository.save(from);
        accountRepository.save(to);



        String body = """
            {
              "fromAccountId": %d,
              "toAccountId": %d,
              "amount": 100
            }
            """.formatted(
                from.getId(),
                to.getId()
        );


        mockMvc.perform(post("/transfers")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());
    }



}
package com.example.bank.controller;

import com.example.bank.config.TestcontainersConfig;
import com.example.bank.model.BankAccount;
import com.example.bank.model.User;
import com.example.bank.repository.*;
import com.example.bank.security.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@DisplayName("UserController — интеграционные тесты")
class UserControllerIntegrationTest {


    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankCardRepository bankCardRepository;

    @Autowired
    JwtService jwtService;


    private String adminToken;
    private String userToken;


    @BeforeEach
    void setUp() {

        transferRepository.deleteAll();

        transactionRepository.deleteAll();

        bankCardRepository.deleteAll();

        accountRepository.deleteAll();

        userRepository.deleteAll();


        User admin = new User(
                "admin",
                "admin@test.com",
                "Admin",
                passwordEncoder.encode("password")
        );

        admin.setRole("ADMIN");


        User user = new User(
                "user",
                "user@test.com",
                "User",
                passwordEncoder.encode("password")
        );

        user.setRole("USER");


        userRepository.save(admin);
        userRepository.save(user);


        adminToken = jwtService.generateToken("admin");
        userToken = jwtService.generateToken("user");
    }



    @Test
    @DisplayName("GET /users — ADMIN получает всех пользователей")
    void getAllUsers_asAdmin_shouldReturnUsers() throws Exception {


        mockMvc.perform(get("/users")
                        .header("Authorization",
                                "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2));
    }



    @Test
    @DisplayName("GET /users — USER получает 403")
    void getAllUsers_asUser_shouldReturn403() throws Exception {


        mockMvc.perform(get("/users")
                        .header("Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }



    @Test
    @DisplayName("GET /users/{id} — ADMIN получает пользователя")
    void getById_asAdmin_shouldReturnUser() throws Exception {


        User user =
                userRepository.findByUserName("user")
                        .orElseThrow();


        mockMvc.perform(get("/users/" + user.getId())
                        .header("Authorization",
                                "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName")
                        .value("user"));
    }




    @Test
    @DisplayName("GET /users/{id} — USER получает себя")
    void getById_owner_shouldReturnUser() throws Exception {


        User user =
                userRepository.findByUserName("user")
                        .orElseThrow();


        mockMvc.perform(get("/users/" + user.getId())
                        .header("Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName")
                        .value("user"));
    }




    @Test
    @DisplayName("GET /users/{id} — USER не получает чужого пользователя")
    void getById_notOwner_shouldReturn403() throws Exception {


        User other = new User(
                "other",
                "other@test.com",
                "Other",
                passwordEncoder.encode("password")
        );

        other.setRole("USER");

        userRepository.save(other);



        mockMvc.perform(get("/users/" + other.getId())
                        .header("Authorization",
                                "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }




    @Test
    @DisplayName("GET /users/{id}/accounts — ADMIN получает счета пользователя")
    void getUserWithAccounts_asAdmin_shouldReturnAccounts()
            throws Exception {


        User user =
                userRepository.findByUserName("user")
                        .orElseThrow();


        BankAccount account =
                new BankAccount("User Account");

        account.setUser(user);

        accountRepository.save(account);



        mockMvc.perform(
                        get("/users/" + user.getId() + "/accounts")
                                .header("Authorization",
                                        "Bearer " + adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts.length()")
                        .value(1));
    }




    @Test
    @DisplayName("GET /users/{id}/accounts — USER получает свои счета")
    void getUserWithAccounts_owner_shouldReturnAccounts()
            throws Exception {


        User user =
                userRepository.findByUserName("user")
                        .orElseThrow();



        BankAccount account =
                new BankAccount("My Account");

        account.setUser(user);

        accountRepository.save(account);



        mockMvc.perform(
                        get("/users/" + user.getId() + "/accounts")
                                .header("Authorization",
                                        "Bearer " + userToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts.length()")
                        .value(1));
    }




    @Test
    @DisplayName("GET /users/{id}/accounts — USER не получает чужие счета")
    void getUserWithAccounts_notOwner_shouldReturn403()
            throws Exception {


        User other = new User(
                "other",
                "other@test.com",
                "Other",
                passwordEncoder.encode("password")
        );

        other.setRole("USER");

        userRepository.save(other);



        mockMvc.perform(
                        get("/users/" + other.getId() + "/accounts")
                                .header("Authorization",
                                        "Bearer " + userToken)
                )
                .andExpect(status().isForbidden());
    }





    @Test
    @DisplayName("POST /users — ADMIN создаёт пользователя")
    void createUser_asAdmin_shouldReturn201()
            throws Exception {


        String body = """
                {
                  "userName":"newuser",
                  "email":"new@test.com",
                  "fullName":"New User",
                  "password":"password"
                }
                """;


        mockMvc.perform(post("/users")
                        .header("Authorization",
                                "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated());
    }





    @Test
    @DisplayName("POST /users — USER получает 403")
    void createUser_asUser_shouldReturn403()
            throws Exception {


        String body = """
                {
                  "userName":"newuser",
                  "email":"new@test.com",
                  "fullName":"New User",
                  "password":"password"
                }
                """;


        mockMvc.perform(post("/users")
                        .header("Authorization",
                                "Bearer " + userToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }





    @Test
    @DisplayName("POST /users/{id}/accounts — ADMIN создаёт счет")
    void createAccountForUser_asAdmin_shouldReturn201()
            throws Exception {


        User user =
                userRepository.findByUserName("user")
                        .orElseThrow();


        String body = """
        {
          "owner":"New Account",
          "accountType":"DEBIT"
        }
        """;



        mockMvc.perform(
                        post("/users/" + user.getId() + "/accounts")
                                .header("Authorization",
                                        "Bearer " + adminToken)
                                .contentType("application/json")
                                .content(body)
                )
                .andExpect(status().isCreated());
    }


}
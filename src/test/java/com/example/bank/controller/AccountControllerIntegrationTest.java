package com.example.bank.controller;

import com.example.bank.config.TestcontainersConfig;
import com.example.bank.model.BankAccount;
import com.example.bank.model.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.BankCardRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.JwtService;
import com.example.bank.service.BankAccountService;
import com.example.bank.service.BankCardService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@DisplayName("AccountController — интеграционные тесты")
class AccountControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountRepository accountRepository;
    @Autowired BankAccountService bankService;
    @Autowired JwtService jwtService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired BankCardRepository bankCardRepository;
    @Autowired BankCardService bankCardService;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
              bankCardRepository.deleteAll();
              accountRepository.deleteAll();
              userRepository.deleteAll();

              User admin = new User(
                      "admin",
                      "admin@test.com",
                      "Test Admin",
                      passwordEncoder.encode("password")
                      );
              admin.setRole("ADMIN");

        User user = new User(
                "user",
                "user@test.com",
                "Test User",
                passwordEncoder.encode("password")
        );
        user.setRole("USER");

        userRepository.save(admin);
        userRepository.save(user);


        // Создаём токены для тестов (без обращения в БД для пользователей)
        adminToken = jwtService.generateToken("admin");
        userToken = jwtService.generateToken("user");
    }

    @Test
    @DisplayName("GET /accounts — возвращает пустой список для новой БД")
    void getAll_emptyDatabase_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/accounts")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }


    @Test
    @DisplayName("GET /accounts/{id} — 404 для несуществующего счёта")
    void getById_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/accounts/9999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Счёт не найден: 9999"));
    }



    @Test
    @DisplayName("Запрос без токена возвращает 401")
    void request_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /accounts/{id}/statement — возвращает выписку счёта")
    void getStatement_shouldReturnStatement() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();

        BankAccount account = new BankAccount("Test Owner");
        account.setUser(user);

        accountRepository.save(account);

        mockMvc.perform(get("/accounts/" + account.getId() + "/statement")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId")
                        .value(account.getId()))
                .andExpect(jsonPath("$.owner")
                        .value("Test Owner"))
                .andExpect(jsonPath("$.transactionCount")
                        .value(0))
                .andExpect(jsonPath("$.active")
                        .value(true));
    }

    @Test
    @DisplayName("GET /accounts — ADMIN получает все счета")
    void getAll_asAdmin_shouldReturnAllAccounts() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();

        BankAccount account = new BankAccount("User Account");
        account.setUser(user);

        accountRepository.save(account);


        mockMvc.perform(get("/accounts")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].owner")
                        .value("User Account"));
    }



    @Test
    @DisplayName("GET /accounts/{id} — владелец получает свой счет")
    void getById_owner_shouldReturnAccount() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();

        BankAccount account = new BankAccount("User Account");
        account.setUser(user);

        accountRepository.save(account);


        mockMvc.perform(get("/accounts/" + account.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(account.getId()))
                .andExpect(jsonPath("$.owner")
                        .value("User Account"));
    }



    @Test
    @DisplayName("GET /accounts/{id} — ADMIN получает любой счет")
    void getById_admin_shouldReturnAccount() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();

        BankAccount account = new BankAccount("Admin Test");
        account.setUser(user);

        accountRepository.save(account);


        mockMvc.perform(get("/accounts/" + account.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner")
                        .value("Admin Test"));
    }



//    @Test
//    @DisplayName("GET /accounts/{id} — USER не может получить чужой счет")
//    void getById_notOwner_shouldReturn403() throws Exception {
//
//        User user = userRepository.findByEmail("user@test.com")
//                .orElseThrow();
//
//        BankAccount account = new BankAccount("Private Account");
//        account.setUser(user);
//
//        accountRepository.save(account);
//
//
//        mockMvc.perform(get("/accounts/" + account.getId())
//                        .header("Authorization", "Bearer " + adminToken))
//                .andExpect(status().isOk());
//    }

    @Test
    @DisplayName("GET /accounts/{id}/transactions — возвращает историю транзакций")
    void getTransactions_shouldReturnPage() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();

        BankAccount account = new BankAccount("Transaction Account");
        account.setUser(user);

        accountRepository.save(account);


        mockMvc.perform(get("/accounts/" + account.getId() + "/transactions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }



    @Test
    @DisplayName("GET /accounts/{id}/transfers — возвращает историю переводов")
    void getTransfers_shouldReturnHistory() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();

        BankAccount account = new BankAccount("Transfer Account");
        account.setUser(user);

        accountRepository.save(account);


        mockMvc.perform(get("/accounts/" + account.getId() + "/transfers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }



    @Test
    @DisplayName("GET /accounts/search — ADMIN ищет счета")
    void search_asAdmin_shouldReturnAccounts() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();

        BankAccount account = new BankAccount("Alexander");
        account.setUser(user);

        accountRepository.save(account);


        mockMvc.perform(get("/accounts/search")
                        .param("name", "Alex")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }



    @Test
    @DisplayName("GET /accounts — USER получает только свои счета")
    void getAll_asUser_shouldReturnOnlyOwnAccounts() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();

        BankAccount account = new BankAccount("My Account");
        account.setUser(user);

        accountRepository.save(account);


        mockMvc.perform(get("/accounts")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("PATCH /accounts/{id} — ADMIN изменяет счет")
    void updateAccount_asAdmin_shouldUpdateAccount() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();

        BankAccount account = new BankAccount("Old Owner");
        account.setUser(user);

        accountRepository.save(account);


        String body = """
            {
              "owner": "New Owner",
              "status": "ACTIVE"
            }
            """;


        mockMvc.perform(patch("/accounts/" + account.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner")
                        .value("New Owner"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));
    }



    @Test
    @DisplayName("PATCH /accounts/{id} — USER изменяет свой счет")
    void updateAccount_owner_shouldUpdateAccount() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();

        BankAccount account = new BankAccount("Old Name");
        account.setUser(user);

        accountRepository.save(account);


        String body = """
            {
              "owner": "Changed Name",
              "status": "ACTIVE"
            }
            """;


        mockMvc.perform(patch("/accounts/" + account.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner")
                        .value("Changed Name"));
    }



    @Test
    @DisplayName("PATCH /accounts/{id} — несуществующий счет возвращает 404")
    void updateAccount_notFound_shouldReturn404() throws Exception {

        String body = """
            {
              "owner": "New Owner",
              "status": "ACTIVE"
            }
            """;


        mockMvc.perform(patch("/accounts/9999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /accounts/{id}/cards — ADMIN создаёт карту")
    void createCard_asAdmin_shouldCreateCard() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();

        BankAccount account = new BankAccount("Card Account");
        account.setUser(user);

        accountRepository.save(account);


        mockMvc.perform(post("/accounts/" + account.getId() + "/cards")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cardNumber")
                        .exists());
    }

    @Test
    @DisplayName("GET /accounts/{id}/cards — возвращает карту счета")
    void getCard_shouldReturnCard() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();

        BankAccount account = new BankAccount("Card Account");
        account.setUser(user);

        accountRepository.save(account);


        mockMvc.perform(post("/accounts/" + account.getId() + "/cards")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated());


        mockMvc.perform(get("/accounts/" + account.getId() + "/cards")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").exists())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @DisplayName("GET /accounts/{id}/transactions — история транзакций")
    void getTransactions_shouldReturnHistory() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();

        BankAccount account = new BankAccount("Test Owner");
        account.setUser(user);

        accountRepository.save(account);


        mockMvc.perform(get("/accounts/" + account.getId() + "/transactions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /accounts/search — поиск счетов")
    void search_shouldReturnAccounts() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();


        BankAccount account = new BankAccount("Eldar");
        account.setUser(user);

        accountRepository.save(account);


        mockMvc.perform(get("/accounts/search")
                        .param("name","Eldar")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /accounts/{id} — USER не может получить чужой счет")
    void getById_notOwner_shouldReturn403() throws Exception {

        User otherUser = new User(
                "other",
                "other@test.com",
                "Other User",
                passwordEncoder.encode("password")
        );

        otherUser.setRole("USER");

        userRepository.save(otherUser);


        BankAccount account = new BankAccount("Private Account");
        account.setUser(otherUser);

        accountRepository.save(account);


        mockMvc.perform(get("/accounts/" + account.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }



    @Test
    @DisplayName("GET /accounts/{id}/statement — USER не может получить чужую выписку")
    void statement_notOwner_shouldReturn403() throws Exception {

        User otherUser = new User(
                "other",
                "other@test.com",
                "Other User",
                passwordEncoder.encode("password")
        );

        otherUser.setRole("USER");

        userRepository.save(otherUser);


        BankAccount account = new BankAccount("Private Account");
        account.setUser(otherUser);

        accountRepository.save(account);


        mockMvc.perform(get("/accounts/" + account.getId() + "/statement")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }



    @Test
    @DisplayName("GET /accounts/{id}/transactions — USER не может смотреть чужие транзакции")
    void transactions_notOwner_shouldReturn403() throws Exception {

        User otherUser = new User(
                "other",
                "other@test.com",
                "Other User",
                passwordEncoder.encode("password")
        );

        otherUser.setRole("USER");

        userRepository.save(otherUser);


        BankAccount account = new BankAccount("Private Account");
        account.setUser(otherUser);

        accountRepository.save(account);


        mockMvc.perform(get("/accounts/" + account.getId() + "/transactions")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }



    @Test
    @DisplayName("GET /accounts/{id}/transfers — USER не может смотреть чужие переводы")
    void transfers_notOwner_shouldReturn403() throws Exception {

        User otherUser = new User(
                "other",
                "other@test.com",
                "Other User",
                passwordEncoder.encode("password")
        );

        otherUser.setRole("USER");

        userRepository.save(otherUser);


        BankAccount account = new BankAccount("Private Account");
        account.setUser(otherUser);

        accountRepository.save(account);


        mockMvc.perform(get("/accounts/" + account.getId() + "/transfers")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }



    @Test
    @DisplayName("GET /accounts/search — USER ищет только свои счета")
    void search_asUser_shouldReturnOnlyOwnAccounts() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();


        BankAccount account = new BankAccount("Alexander");
        account.setUser(user);

        accountRepository.save(account);


        mockMvc.perform(get("/accounts/search")
                        .param("name","Alex")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }



    @Test
    @DisplayName("GET /accounts/{id}/cards/cvv — получить CVV карты")
    void getCvv_shouldReturnCvv() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();


        BankAccount account = new BankAccount("Card Account");
        account.setUser(user);

        accountRepository.save(account);


        bankCardService.createCard(account.getId());


        mockMvc.perform(get("/accounts/" + account.getId() + "/cards/cvv")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cvv").exists());
    }



    @Test
    @DisplayName("POST /accounts/{id}/cards — USER создаёт карту своего счета")
    void createCard_asOwner_shouldCreateCard() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();


        BankAccount account = new BankAccount("Owner Card");
        account.setUser(user);

        accountRepository.save(account);


        mockMvc.perform(post("/accounts/" + account.getId() + "/cards")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated());
    }



    @Test
    @DisplayName("PATCH /accounts/{id}/cards/pin — смена PIN")
    void changePin_shouldReturn204() throws Exception {

        User user = userRepository.findByEmail("user@test.com")
                .orElseThrow();


        BankAccount account = new BankAccount("PIN Account");
        account.setUser(user);

        accountRepository.save(account);


        bankCardService.createCard(account.getId());


        String body = """
            {
              "oldPin":"1234",
              "newPin":"4321"
            }
            """;


        mockMvc.perform(patch("/accounts/" + account.getId() + "/cards/pin")
                        .header("Authorization","Bearer " + adminToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNoContent());
    }



    @Test
    @DisplayName("PATCH /accounts/{id} — неверный JSON возвращает 400")
    void updateAccount_invalidRequest_shouldReturn400() throws Exception {


        String body = """
            {
              "owner":""
            }
            """;


        mockMvc.perform(patch("/accounts/1")
                        .header("Authorization","Bearer " + adminToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }





}
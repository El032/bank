package com.example.bank.controller;

import com.example.bank.model.BankAccount;
import com.example.bank.model.BankCard;
import com.example.bank.model.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.BankCardRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.service.AuthService;
import com.example.bank.security.JwtService;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
class AtmControllerIntegrationTest {


    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankCardRepository bankCardRepository;


    @Autowired
    private AuthService authService;


    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;


    private String userToken;
    private BankCard card;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;


    @BeforeEach
    void setUp() {

        mockMvc =
                MockMvcBuilders
                        .webAppContextSetup(context)
                        .build();


        User user =
                userRepository.findByUserName("user")
                        .orElseThrow();


        userToken =
                jwtService.generateToken(user.getUserName());


        BankAccount account =
                new BankAccount("user");


        account.setUser(user);

        account.deposit(new BigDecimal("5000"));


        accountRepository.save(account);



        String cardNumber =
                "111122223333" + (System.currentTimeMillis() % 1000000);

        card =
                new BankCard(
                        cardNumber,
                        passwordEncoder.encode("1234"),
                        LocalDate.now().plusYears(3),
                        "encrypted-cvv",
                        account
                );


        bankCardRepository.save(card);
    }



    @Test
    @DisplayName("POST /atm/cards — вставить карту должен вернуть 201")
    void insertCard_shouldReturn201() throws Exception {


        String body = """
                {
                  "cardId": %d
                }
                """.formatted(card.getId());


        mockMvc.perform(post("/atm/cards")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authenticated")
                        .value(false));
    }



    @Test
    @DisplayName("POST /atm/sessions/{id}/pin — правильный PIN возвращает 204")
    void verifyPin_shouldReturn204() throws Exception {


        String insertBody = """
                {
                  "cardId": %d
                }
                """.formatted(card.getId());


        String response =
                mockMvc.perform(post("/atm/cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(insertBody))

                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        System.out.println("ATM RESPONSE = " + response);

        JsonNode json =
                objectMapper.readTree(response);

        Long sessionId =
                json.get("sessionId").asLong();


        String pinBody = """
                {
                  "pin":"1234"
                }
                """;


        mockMvc.perform(post("/atm/sessions/{id}/pin", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pinBody))

                .andExpect(status().isNoContent());

    }



    @Test
    @DisplayName("GET /atm/sessions/{id}/balance — без PIN должен вернуть 422")
    void balance_withoutPin_shouldReturn422() throws Exception {


        String body = """
                {
                  "cardId": %d
                }
                """.formatted(card.getId());


        String response =
                mockMvc.perform(post("/atm/cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))

                        .andReturn()
                        .getResponse()
                        .getContentAsString();



        JsonNode json =
                objectMapper.readTree(response);

        Long sessionId =
                json.get("sessionId").asLong();



        mockMvc.perform(get("/atm/sessions/{id}/balance", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        ))

                .andExpect(status().isUnprocessableEntity());
    }



    @Test
    @DisplayName("POST /atm/sessions/{id}/close — закрытие ATM возвращает 204")
    void closeSession_shouldReturn204() throws Exception {


        String body = """
                {
                  "cardId": %d
                }
                """.formatted(card.getId());


        String response =
                mockMvc.perform(post("/atm/cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))

                        .andReturn()
                        .getResponse()
                        .getContentAsString();



        JsonNode json =
                objectMapper.readTree(response);

        Long sessionId =
                json.get("sessionId").asLong();



        mockMvc.perform(post("/atm/sessions/{id}/close", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        ))

                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /atm/sessions/{id}/deposit — без PIN должен вернуть 422")
    void deposit_withoutPin_shouldReturn422() throws Exception {


        String insertBody = """
            {
              "cardId": %d
            }
            """.formatted(card.getId());


        String response =
                mockMvc.perform(post("/atm/cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(insertBody))

                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();



        ObjectMapper mapper = new ObjectMapper();

        Long sessionId =
                mapper.readTree(response)
                        .get("sessionId")
                        .asLong();



        String depositBody = """
            {
              "amount": 1000
            }
            """;



        mockMvc.perform(post("/atm/sessions/{id}/deposit", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositBody))

                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /atm/sessions/{id}/deposit — успешное пополнение возвращает 200")
    void deposit_shouldReturn200() throws Exception {


        String insertBody = """
            {
              "cardId": %d
            }
            """.formatted(card.getId());


        String response =
                mockMvc.perform(post("/atm/cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(insertBody))

                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();



        ObjectMapper mapper = new ObjectMapper();


        Long sessionId =
                mapper.readTree(response)
                        .get("sessionId")
                        .asLong();



        String pinBody = """
            {
              "pin":"1234"
            }
            """;


        mockMvc.perform(post("/atm/sessions/{id}/pin", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pinBody))

                .andExpect(status().isNoContent());



        String depositBody = """
            {
              "amount":1000
            }
            """;



        mockMvc.perform(post("/atm/sessions/{id}/deposit", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositBody))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.depositedAmount")
                        .value(1000));
    }

    @Test
    @DisplayName("POST /atm/sessions/{id}/deposit — сумма меньше 100 должна вернуть 400")
    void deposit_lessThanMinimum_shouldReturn400() throws Exception {


        String insertBody = """
            {
              "cardId": %d
            }
            """.formatted(card.getId());


        String response =
                mockMvc.perform(post("/atm/cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(insertBody))

                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();



        ObjectMapper mapper = new ObjectMapper();


        Long sessionId =
                mapper.readTree(response)
                        .get("sessionId")
                        .asLong();



        String pinBody = """
            {
              "pin":"1234"
            }
            """;


        mockMvc.perform(post("/atm/sessions/{id}/pin", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pinBody))

                .andExpect(status().isNoContent());



        String depositBody = """
            {
              "amount":50
            }
            """;


        mockMvc.perform(post("/atm/sessions/{id}/deposit", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositBody))

                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /atm/sessions/{id}/withdraw — без PIN должен вернуть 422")
    void withdraw_withoutPin_shouldReturn422() throws Exception {


        String insertBody = """
            {
              "cardId": %d
            }
            """.formatted(card.getId());


        String response =
                mockMvc.perform(post("/atm/cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(insertBody))

                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();


        ObjectMapper mapper = new ObjectMapper();


        Long sessionId =
                mapper.readTree(response)
                        .get("sessionId")
                        .asLong();



        String withdrawBody = """
            {
              "amount":1000
            }
            """;


        mockMvc.perform(post("/atm/sessions/{id}/withdraw", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawBody))

                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /atm/sessions/{id}/withdraw — успешное снятие возвращает 204")
    void withdraw_shouldReturn204() throws Exception {


        String insertBody = """
            {
              "cardId": %d
            }
            """.formatted(card.getId());


        String response =
                mockMvc.perform(post("/atm/cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(insertBody))

                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();



        ObjectMapper mapper = new ObjectMapper();


        Long sessionId =
                mapper.readTree(response)
                        .get("sessionId")
                        .asLong();



        String pinBody = """
            {
              "pin":"1234"
            }
            """;


        mockMvc.perform(post("/atm/sessions/{id}/pin", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pinBody))

                .andExpect(status().isNoContent());



        String withdrawBody = """
            {
              "amount":1000
            }
            """;



        mockMvc.perform(post("/atm/sessions/{id}/withdraw", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawBody))

                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /atm/sessions/{id}/withdraw — недостаточно средств должен вернуть 422")
    void withdraw_insufficientBalance_shouldReturn422() throws Exception {


        String insertBody = """
            {
              "cardId": %d
            }
            """.formatted(card.getId());


        String response =
                mockMvc.perform(post("/atm/cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(insertBody))

                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();



        ObjectMapper mapper = new ObjectMapper();


        Long sessionId =
                mapper.readTree(response)
                        .get("sessionId")
                        .asLong();



        String pinBody = """
            {
              "pin":"1234"
            }
            """;


        mockMvc.perform(post("/atm/sessions/{id}/pin", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pinBody))

                .andExpect(status().isNoContent());



        String withdrawBody = """
            {
              "amount":10000
            }
            """;


        mockMvc.perform(post("/atm/sessions/{id}/withdraw", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawBody))

                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /atm/sessions/{id}/withdraw — превышение лимита ATM должен вернуть 422")
    void withdraw_exceedDailyLimit_shouldReturn422() throws Exception {


        String insertBody = """
            {
              "cardId": %d
            }
            """.formatted(card.getId());


        String response =
                mockMvc.perform(post("/atm/cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(insertBody))

                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();



        ObjectMapper mapper = new ObjectMapper();


        Long sessionId =
                mapper.readTree(response)
                        .get("sessionId")
                        .asLong();



        String pinBody = """
            {
              "pin":"1234"
            }
            """;


        mockMvc.perform(post("/atm/sessions/{id}/pin", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pinBody))

                .andExpect(status().isNoContent());



        String withdrawBody = """
            {
              "amount":60000
            }
            """;


        mockMvc.perform(post("/atm/sessions/{id}/withdraw", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawBody))

                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /atm/cards — заблокированная карта должна вернуть 422")
    void insertBlockedCard_shouldReturn422() throws Exception {


        card.block(card.getId());

        bankCardRepository.save(card);


        String body = """
            {
              "cardId": %d
            }
            """.formatted(card.getId());


        mockMvc.perform(post("/atm/cards")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /atm/cards — закрытая карта должна вернуть 422")
    void insertClosedCard_shouldReturn422() throws Exception {


        card.close(card.getId());

        bankCardRepository.save(card);


        String body = """
            {
              "cardId": %d
            }
            """.formatted(card.getId());


        mockMvc.perform(post("/atm/cards")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /atm/cards — несуществующая карта возвращает 404")
    void insertCard_notFound_shouldReturn404() throws Exception {

        String body = """
            {
              "cardId": 999999
            }
            """;


        mockMvc.perform(post("/atm/cards")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /atm/cards — активная ATM-сессия должна вернуть 409")
    void insertCard_activeSession_shouldReturn409() throws Exception {


        String body = """
            {
              "cardId": %d
            }
            """.formatted(card.getId());



        mockMvc.perform(post("/atm/cards")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

                .andExpect(status().isCreated());



        mockMvc.perform(post("/atm/cards")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /atm/sessions/{id}/pin — неправильный PIN возвращает 400")
    void verifyPin_wrongPin_shouldReturn400() throws Exception {


        String insertBody = """
            {
              "cardId": %d
            }
            """.formatted(card.getId());


        String response =
                mockMvc.perform(post("/atm/cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(insertBody))

                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();


        ObjectMapper mapper = new ObjectMapper();

        Long sessionId =
                mapper.readTree(response)
                        .get("sessionId")
                        .asLong();



        String pinBody = """
            {
              "pin":"9999"
            }
            """;


        mockMvc.perform(post("/atm/sessions/{id}/pin", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pinBody))

                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /atm/sessions/{id}/withdraw — успешное снятие возвращает 200")
    void withdraw_shouldReturn200() throws Exception {


        String insertBody = """
        {
          "cardId": %d
        }
        """.formatted(card.getId());


        String response =
                mockMvc.perform(post("/atm/cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(insertBody))

                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();



        ObjectMapper mapper = new ObjectMapper();


        Long sessionId =
                mapper.readTree(response)
                        .get("sessionId")
                        .asLong();



        String pinBody = """
        {
          "pin":"1234"
        }
        """;


        mockMvc.perform(post("/atm/sessions/{id}/pin", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pinBody))

                .andExpect(status().isNoContent());



        String withdrawBody = """
        {
          "amount":1000
        }
        """;



        mockMvc.perform(post("/atm/sessions/{id}/withdraw", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawBody))

                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /atm/sessions/{id}/close — неактивная сессия должна вернуть 409")
    void closeSession_notActive_shouldReturn409() throws Exception {


        mockMvc.perform(post("/atm/sessions/{id}/close", 999999L)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        ))

                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /atm/sessions/{id}/pin — повторная проверка PIN должна вернуть 204")
    void verifyPin_twice_shouldReturn204() throws Exception {


        String insertBody = """
        {
          "cardId": %d
        }
        """.formatted(card.getId());


        String response =
                mockMvc.perform(post("/atm/cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(insertBody))

                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();



        Long sessionId =
                objectMapper.readTree(response)
                        .get("sessionId")
                        .asLong();



        String pinBody = """
        {
          "pin":"1234"
        }
        """;


        // первый PIN
        mockMvc.perform(post("/atm/sessions/{id}/pin", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pinBody))

                .andExpect(status().isNoContent());



        // второй PIN
        mockMvc.perform(post("/atm/sessions/{id}/pin", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pinBody))

                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /atm/sessions/{id}/withdraw — сумма меньше минимума должна вернуть 422")
    void withdraw_lessThanMinimum_shouldReturn422() throws Exception {


        String insertBody = """
        {
          "cardId": %d
        }
        """.formatted(card.getId());


        String response =
                mockMvc.perform(post("/atm/cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + userToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(insertBody))

                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();



        Long sessionId =
                objectMapper.readTree(response)
                        .get("sessionId")
                        .asLong();



        String pinBody = """
        {
          "pin":"1234"
        }
        """;


        mockMvc.perform(post("/atm/sessions/{id}/pin", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pinBody))

                .andExpect(status().isNoContent());



        String withdrawBody = """
        {
          "amount":50
        }
        """;


        mockMvc.perform(post("/atm/sessions/{id}/withdraw", sessionId)
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawBody))

                .andExpect(status().isUnprocessableEntity());
    }



}
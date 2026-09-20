package com.example.bank.controller;

import com.example.bank.config.TestcontainersConfig;
import com.example.bank.model.User;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
@SpringBootTest
class AdminUserControllerIntegrationTest {


    @Autowired
    private WebApplicationContext context;


    private MockMvc mockMvc;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private JwtService jwtService;


    @Autowired
    private ObjectMapper objectMapper;


    private String adminToken;

    private String userToken;


    private User targetUser;



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



        targetUser = user;

        targetUser.activate();

        userRepository.save(targetUser);

    }



    @Test
    @DisplayName("ADMIN деактивирует пользователя")
    void updateStatus_disable_shouldReturn200() throws Exception {


        String body = """
        {
          "active":false
        }
        """;


        mockMvc.perform(
                        patch(
                                "/admin/users/{id}/status",
                                targetUser.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )


                .andExpect(status().isOk())

                .andDo(print());

    }




    @Test
    @DisplayName("ADMIN активирует пользователя")
    void updateStatus_enable_shouldReturn200() throws Exception {


        targetUser.deactivate();

        userRepository.save(targetUser);


        String body = """
        {
          "active":true
        }
        """;


        mockMvc.perform(
                        patch(
                                "/admin/users/{id}/status",
                                targetUser.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )


                .andExpect(status().isOk())

                .andDo(print());

    }





    @Test
    @DisplayName("USER не может менять статус")
    void user_shouldReturn403() throws Exception {


        String body = """
        {
          "active":false
        }
        """;


        mockMvc.perform(
                        patch(
                                "/admin/users/{id}/status",
                                targetUser.getId()
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
          "active":false
        }
        """;


        mockMvc.perform(
                        patch(
                                "/admin/users/{id}/status",
                                targetUser.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )


                .andExpect(status().isUnauthorized());

    }





    @Test
    @DisplayName("Несуществующий пользователь возвращает 404")
    void notFound_shouldReturn404() throws Exception {


        String body = """
        {
          "active":false
        }
        """;


        mockMvc.perform(
                        patch(
                                "/admin/users/999999/status"
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
    @DisplayName("Без active возвращает 400")
    void invalidRequest_shouldReturn400() throws Exception {


        String body = """
        {
        }
        """;


        mockMvc.perform(
                        patch(
                                "/admin/users/{id}/status",
                                targetUser.getId()
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

}
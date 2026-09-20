package com.example.bank.service;

import com.example.bank.actuator.UserMetrics;
import com.example.bank.exception.UserAlreadyExistsException;
import com.example.bank.exception.UserInactiveException;
import com.example.bank.model.User;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.JwtService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;


import org.springframework.security.crypto.password.PasswordEncoder;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — тесты аутентификации")
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JwtService jwtService;

    @Mock
    private UserMetrics userMetrics;

    @InjectMocks AuthService authService;

    @Captor ArgumentCaptor<User> userCaptor;

    @Nested
    @DisplayName("Регистрация")
    class RegisterTests {

        @Test
        @DisplayName("Успешная регистрация сохраняет хешированный пароль")
        void register_success_shouldSaveHashedPassword() {
            // Arrange
            when(userRepository.existsByUserName("eldar")).thenReturn(false);
            when(userRepository.existsByEmail("eldar@bank.ru")).thenReturn(false);
            when(passwordEncoder.encode("secret123")).thenReturn("$2a$hashed");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            User result = authService.register("eldar", "eldar@bank.ru",
                    "secret123", "Эльдар");

            // Assert
            verify(userRepository).save(userCaptor.capture());
            assertEquals("$2a$hashed", userCaptor.getValue().getPasswordHash());
            assertNotEquals("secret123", userCaptor.getValue().getPasswordHash());
        }

        @Test
        @DisplayName("Занятый username выбрасывает UserAlreadyExistsException")
        void register_duplicateUsername_shouldThrow() {
            when(userRepository.existsByUserName("eldar")).thenReturn(true);

            assertThrows(UserAlreadyExistsException.class,
                    () -> authService.register("eldar", "eldar@bank.ru",
                            "secret", "Эльдар"));

            // Email проверяться не должен — упали раньше
            verify(userRepository, never()).existsByEmail(any());
            // Сохранения не должно быть
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Занятый email выбрасывает UserAlreadyExistsException")
        void register_duplicateEmail_shouldThrow() {
            when(userRepository.existsByUserName("eldar")).thenReturn(false);
            when(userRepository.existsByEmail("eldar@bank.ru")).thenReturn(true);

            assertThrows(UserAlreadyExistsException.class,
                    () -> authService.register("eldar", "eldar@bank.ru",
                            "secret", "Эльдар"));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Пароль шифруется через PasswordEncoder")
        void register_shouldUsePasswordEncoder() {
            when(userRepository.existsByUserName(any())).thenReturn(false);
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            authService.register("eldar", "eldar@bank.ru", "mypassword", null);

            verify(passwordEncoder).encode("mypassword");
        }
    }

    @Nested
    @DisplayName("Логин")
    class LoginTests {


        @Test
        @DisplayName("Успешный логин возвращает JWT токен")
        void login_success_shouldReturnToken() {
            // Arrange
            Authentication auth = mock(Authentication.class);

            when(authenticationManager.authenticate(any()))
                    .thenReturn(auth);

            when(jwtService.generateToken("eldar"))
                    .thenReturn("eyJhbGci.payload.signature");

            // Act
            String token = authService.login("eldar", "secret123");

            // Assert
            assertEquals("eyJhbGci.payload.signature", token);
            verify(jwtService).generateToken("eldar");
        }


        @Test
        @DisplayName("Неверный пароль выбрасывает исключение")
        void login_wrongPassword_shouldThrow() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Неверный пароль"));

            assertThrows(BadCredentialsException.class,
                    () -> authService.login("eldar", "wrong"));

            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Заблокированный пользователь выбрасывает UserInactiveException")
        void login_disabledUser_shouldThrowUserInactiveException() {

            when(authenticationManager.authenticate(any()))
                    .thenThrow(new DisabledException("Пользователь заблокирован"));

            UserInactiveException exception = assertThrows(
                    UserInactiveException.class,
                    () -> authService.login("eldar", "secret123")
            );

            assertEquals(
                    "Пользователь заблокирован",
                    exception.getMessage()
            );

            verify(jwtService, never()).generateToken(any());
        }
    }
}
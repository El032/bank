package com.example.bank.exception;

import com.example.bank.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.converter.HttpMessageNotReadableException;

import javax.naming.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/test");
    }

    @Test
    @DisplayName("UserInactiveException -> 403")
    void handleUserInactiveException_shouldReturn403() {
        UserInactiveException exception = new UserInactiveException();

        ResponseEntity<ErrorResponse> response =
                handler.handleUserInactiveException(exception, request);

        assertResponse(
                response,
                HttpStatus.FORBIDDEN,
                "Пользователь заблокирован",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("CreditAccountNotFoundException -> 404")
    void handleCreditAccountNotFound_shouldReturn404() {
        CreditAccountNotFoundException exception =
                mock(CreditAccountNotFoundException.class);

        when(exception.getMessage()).thenReturn("Кредитный счёт не найден: 10");

        ResponseEntity<ErrorResponse> response =
                handler.handleCreditAccountNotFound(exception, request);

        assertResponse(
                response,
                HttpStatus.NOT_FOUND,
                "Кредитный счёт не найден",
                "Кредитный счёт не найден: 10"
        );
    }

    @Test
    @DisplayName("CreditPaymentExceededException -> 400")
    void handleCreditPaymentExceeded_shouldReturn400() {
        CreditPaymentExceededException exception =
                new CreditPaymentExceededException(
                        new BigDecimal("1500.00"),
                        new BigDecimal("1000.00")
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleCreditPaymentExceeded(exception, request);

        assertResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Сумма платежа превышает задолженность",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("CardNotFoundException -> 404")
    void handleCardNotFound_shouldReturn404() {
        CardNotFoundException exception =
                mock(CardNotFoundException.class);

        when(exception.getMessage()).thenReturn("Карта не найдена: 5");

        ResponseEntity<ErrorResponse> response =
                handler.handleCardNotFound(exception, request);

        assertResponse(
                response,
                HttpStatus.NOT_FOUND,
                "Карта не найдена",
                "Карта не найдена: 5"
        );
    }

    @Test
    @DisplayName("InactiveCardException -> 422")
    void handleInactiveCardException_shouldReturn422() {
        InactiveCardException exception =
                mock(InactiveCardException.class);

        when(exception.getMessage()).thenReturn("Карта неактивна");

        ResponseEntity<ErrorResponse> response =
                handler.handleInactiveCardException(exception, request);

        assertResponse(
                response,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Карта неактивна",
                "Карта неактивна"
        );
    }

    @Test
    @DisplayName("InvalidAccountStatusTransitionException -> 422")
    void handleInvalidAccountStatusTransition_shouldReturn422() {
        InvalidAccountStatusTransitionException exception =
                new InvalidAccountStatusTransitionException(
                        "Нельзя изменить статус"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidAccountStatusTransition(exception, request);

        assertResponse(
                response,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Недопустимый переход статуса счёта",
                "Нельзя изменить статус"
        );
    }

    @Test
    @DisplayName("UserNotFoundException -> 404")
    void handleUserNotFound_shouldReturn404() {
        UserNotFoundException exception =
                mock(UserNotFoundException.class);

        when(exception.getMessage()).thenReturn("Пользователь не найден: 10");

        ResponseEntity<ErrorResponse> response =
                handler.handleUserNotFound(exception, request);

        assertResponse(
                response,
                HttpStatus.NOT_FOUND,
                "Пользователь не найден",
                "Пользователь не найден: 10"
        );
    }

    @Test
    @DisplayName("CannotCloseAccountException -> 422")
    void handleCannotCloseAccount_shouldReturn422() {
        CannotCloseAccountException exception =
                new CannotCloseAccountException(
                        "У счёта есть активные операции"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleCannotCloseAccount(exception, request);

        assertResponse(
                response,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Нельзя закрыть счёт",
                "У счёта есть активные операции"
        );
    }

    @Test
    @DisplayName("InvalidCardStatusTransitionException -> 422")
    void handleInvalidCardStatusTransition_shouldReturn422() {
        InvalidCardStatusTransitionException exception =
                new InvalidCardStatusTransitionException(
                        "Нельзя активировать закрытую карту"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidCardStatusTransition(exception, request);

        assertResponse(
                response,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Недопустимый переход статуса карты",
                "Нельзя активировать закрытую карту"
        );
    }

    @Test
    @DisplayName("ActiveAtmSessionException -> 409")
    void handleActiveAtmSession_shouldReturn409() {
        ActiveAtmSessionException exception =
                mock(ActiveAtmSessionException.class);

        when(exception.getMessage()).thenReturn("ATM-сессия уже существует");

        ResponseEntity<ErrorResponse> response =
                handler.handleActiveAtmSession(exception, request);

        assertResponse(
                response,
                HttpStatus.CONFLICT,
                "ATM-сессия уже существует",
                "ATM-сессия уже существует"
        );
    }

    @Test
    @DisplayName("AtmPinRequiredException -> 422")
    void handleAtmPinRequired_shouldReturn422() {
        AtmPinRequiredException exception =
                mock(AtmPinRequiredException.class);

        when(exception.getMessage()).thenReturn("Введите PIN");

        ResponseEntity<ErrorResponse> response =
                handler.handleAtmPinRequired(exception, request);

        assertResponse(
                response,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "PIN требуется",
                "Введите PIN"
        );
    }

    @Test
    @DisplayName("AtmSessionNotActiveException -> 409")
    void handleAtmSessionNotActive_shouldReturn409() {
        AtmSessionNotActiveException exception =
                mock(AtmSessionNotActiveException.class);

        when(exception.getMessage()).thenReturn("ATM-сессия неактивна");

        ResponseEntity<ErrorResponse> response =
                handler.handleAtmSessionNotActive(exception, request);

        assertResponse(
                response,
                HttpStatus.CONFLICT,
                "ATM-сессия неактивна",
                "ATM-сессия неактивна"
        );
    }

    @Test
    @DisplayName("TransferMonthlyLimitException -> 422")
    void handleTransferMonthlyLimit_shouldReturn422() {
        TransferMonthlyLimitException exception =
                mock(TransferMonthlyLimitException.class);

        when(exception.getMessage()).thenReturn("Месячный лимит превышен");

        ResponseEntity<ErrorResponse> response =
                handler.handleTransferMonthlyLimit(exception, request);

        assertResponse(
                response,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Превышен месячный лимит для перевода",
                "Месячный лимит превышен"
        );
    }

    @Test
    @DisplayName("AtmMinimumAmountException -> 422")
    void handleAtmMinimumAmount_shouldReturn422() {
        AtmMinimumAmountException exception =
                mock(AtmMinimumAmountException.class);

        when(exception.getMessage()).thenReturn("Минимальная сумма 100");

        ResponseEntity<ErrorResponse> response =
                handler.handleAtmMinimumAmount(exception, request);

        assertResponse(
                response,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Минимальная сумма операции ATM",
                "Минимальная сумма 100"
        );
    }

    @Test
    @DisplayName("TransferMinimumAmountException -> 422")
    void handleTransferMinimumAmount_shouldReturn422() {
        TransferMinimumAmountException exception =
                mock(TransferMinimumAmountException.class);

        when(exception.getMessage()).thenReturn("Минимальная сумма перевода 10");

        ResponseEntity<ErrorResponse> response =
                handler.handleTransferMinimumAmount(exception, request);

        assertResponse(
                response,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Минимальная сумма перевода",
                "Минимальная сумма перевода 10"
        );
    }

    @Test
    @DisplayName("AtmDailyWithdrawalLimitException -> 422")
    void handleAtmDailyWithdrawalLimit_shouldReturn422() {
        AtmDailyWithdrawalLimitException exception =
                mock(AtmDailyWithdrawalLimitException.class);

        when(exception.getMessage()).thenReturn("Суточный лимит превышен");

        ResponseEntity<ErrorResponse> response =
                handler.handleAtmDailyWithdrawalLimit(exception, request);

        assertResponse(
                response,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Превышен суточный лимит ATM",
                "Суточный лимит превышен"
        );
    }

    @Test
    @DisplayName("HttpMessageNotReadableException -> 400")
    void handleHttpMessageNotReadable_shouldReturn400() {
        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponse> response =
                handler.handleHttpMessageNotReadable(exception, request);

        assertResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Некорректный JSON",
                "Тело запроса имеет неверный формат"
        );
    }

    @Test
    @DisplayName("InvalidPinException -> 422")
    void handleInvalidPin_shouldReturn422() {
        InvalidPinException exception =
                mock(InvalidPinException.class);

        when(exception.getMessage()).thenReturn("PIN неверный");

        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidPin(exception, request);

        assertResponse(
                response,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Неверный PIN",
                "PIN неверный"
        );
    }

    @Test
    @DisplayName("SavingsCardNotAllowedException -> 422")
    void handleSavingsCardNotAllowed_shouldReturn422() {
        SavingsCardNotAllowedException exception =
                mock(SavingsCardNotAllowedException.class);

        when(exception.getMessage()).thenReturn("Карта недоступна");

        ResponseEntity<ErrorResponse> response =
                handler.handleSavingsCardNotAllowed(exception, request);

        assertResponse(
                response,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Карта недоступна",
                "Карта недоступна"
        );
    }

    @Test
    @DisplayName("DataIntegrityViolationException -> 409")
    void handleDataIntegrityViolation_shouldReturn409() {
        DataIntegrityViolationException exception =
                mock(DataIntegrityViolationException.class);

        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrityViolation(exception, request);

        assertResponse(
                response,
                HttpStatus.CONFLICT,
                "Нарушение ограничения базы данных",
                "Для этого счёта уже существует карта"
        );
    }

    @Test
    @DisplayName("CreditLimitExceededException -> 422")
    void handleCreditLimitExceeded_shouldReturn422() {
        CreditLimitExceededException exception =
                mock(CreditLimitExceededException.class);

        when(exception.getMessage()).thenReturn("Кредитный лимит превышен");

        ResponseEntity<ErrorResponse> response =
                handler.handleCreditLimitExceeded(exception, request);

        assertResponse(
                response,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Превышен кредитный лимит",
                "Кредитный лимит превышен"
        );
    }

    @Test
    @DisplayName("UnsupportedOperationException -> 400")
    void handleUnsupportedOperation_shouldReturn400() {
        UnsupportedOperationException exception =
                new UnsupportedOperationException(
                        "Операция недоступна"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleUnsupportedOperation(exception, request);

        assertResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Нельзя пополнить сберегательный счет",
                "Операция недоступна"
        );
    }

    @Test
    @DisplayName("SavingsAccountNotFoundException -> 400")
    void handleSavingsAccountNotFoundException_shouldReturn400() {
        SavingsAccountNotFoundException exception =
                mock(SavingsAccountNotFoundException.class);

        when(exception.getMessage()).thenReturn("Сберегательный счёт не найден");

        ResponseEntity<ErrorResponse> response =
                handler.handleSavingsAccountNotFoundException(exception, request);

        assertResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Неверный тип счёта",
                "Сберегательный счёт не найден"
        );
    }

    @Test
    @DisplayName("ResourceConflictException -> 409")
    void handleResourceConflict_shouldReturn409() {
        ResourceConflictException exception =
                mock(ResourceConflictException.class);

        when(exception.getMessage()).thenReturn("У пользователя есть счета");

        ResponseEntity<ErrorResponse> response =
                handler.handleResourceConflict(exception, request);

        assertResponse(
                response,
                HttpStatus.CONFLICT,
                "Конфликт данных",
                "У пользователя есть счета"
        );
    }

    @Test
    @DisplayName("InactiveAccountException -> 403")
    void handleInactiveAccountException_shouldReturn403() {
        InactiveAccountException exception =
                new InactiveAccountException(10L);

        ResponseEntity<ErrorResponse> response =
                handler.handleInactiveAccountException(exception, request);

        assertResponse(
                response,
                HttpStatus.FORBIDDEN,
                "Счет неактивен",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("AccountNotFoundException -> 404")
    void handleNotFound_shouldReturn404() {
        AccountNotFoundException exception =
                new AccountNotFoundException(42L);

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(exception, request);

        assertResponse(
                response,
                HttpStatus.NOT_FOUND,
                "Not Found",
                "Счёт не найден: 42"
        );
    }

    @Test
    @DisplayName("InsufficientFundsException -> 422")
    void handleInsufficientFunds_shouldReturn422() {
        InsufficientFundsException exception =
                mock(InsufficientFundsException.class);

        when(exception.getMessage()).thenReturn("Недостаточно средств");

        ResponseEntity<ErrorResponse> response =
                handler.handleInsufficientFunds(exception, request);

        assertResponse(
                response,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Unprocessable Entity",
                "Недостаточно средств"
        );
    }

    @Test
    @DisplayName("InvalidAmountException -> 400")
    void handleInvalidAmountException_shouldReturn400() {
        InvalidAmountException exception =
                mock(InvalidAmountException.class);

        when(exception.getMessage()).thenReturn("Сумма должна быть больше 0");

        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidAmountException(exception, request);

        assertResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Сумма должна быть больше 0"
        );
    }

    @Test
    @DisplayName("MethodArgumentNotValidException -> 400")
    void handleValidation_shouldReturn400WithFieldErrors() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError nameError =
                new FieldError(
                        "request",
                        "name",
                        "Имя обязательно"
                );

        FieldError emailError =
                new FieldError(
                        "request",
                        "email",
                        "Некорректный email"
                );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(nameError, emailError));

        ResponseEntity<ErrorResponse> response =
                handler.handleValidation(exception, request);

        assertResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "name: Имя обязательно; email: Некорректный email"
        );
    }

    @Test
    @DisplayName("UsernameNotFoundException -> 401")
    void handleUsernameNotFound_shouldReturn401() {
        UsernameNotFoundException exception =
                new UsernameNotFoundException("unknown");

        ResponseEntity<ErrorResponse> response =
                handler.handleUsernameNotFound(exception, request);

        assertResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Неверный логин или пароль"
        );
    }

    @Test
    @DisplayName("AccessDeniedException -> 403")
    void handleAccessDenied_shouldReturn403() {
        AccessDeniedException exception =
                new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDenied(exception, request);

        assertResponse(
                response,
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Недостаточно прав для выполнения операции"
        );
    }

    @Test
    @DisplayName("BadCredentialsException -> 401")
    void handleBadCredentials_shouldReturn401() {
        BadCredentialsException exception =
                new BadCredentialsException("Bad credentials");

        ResponseEntity<ErrorResponse> response =
                handler.handleBadCredentials(exception, request);

        assertResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Неверный логин или пароль"
        );
    }

    @Test
    @DisplayName("AuthenticationException -> 401")
    void handleAuthenticationException_shouldReturn401() {
        AuthenticationException exception =
                new AuthenticationException("Authentication failed");

        ResponseEntity<ErrorResponse> response =
                handler.handleAuthenticationException(exception, request);

        assertResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Неверный логин или пароль"
        );
    }

    @Test
    @DisplayName("Exception -> 500")
    void handleAll_shouldReturn500() {
        Exception exception =
                new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response =
                handler.handleAll(exception, request);

        assertResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Произошла внутренняя ошибка сервера"
        );
    }

    @Test
    @DisplayName("TransferException -> 400")
    void handleTransfer_shouldReturn400() {
        TransferException exception =
                mock(TransferException.class);

        when(exception.getMessage()).thenReturn("Ошибка перевода");

        ResponseEntity<ErrorResponse> response =
                handler.handleTransfer(exception, request);

        assertResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Ошибка перевода"
        );
    }

    @Test
    @DisplayName("UserAlreadyExistsException -> 409")
    void handleUserAlreadyExists_shouldReturn409() {
        UserAlreadyExistsException exception =
                mock(UserAlreadyExistsException.class);

        when(exception.getMessage()).thenReturn("Username занят");

        ResponseEntity<ErrorResponse> response =
                handler.handleUserAlreadyExists(exception, request);

        assertResponse(
                response,
                HttpStatus.CONFLICT,
                "Conflict",
                "Username занят"
        );
    }

    private void assertResponse(
            ResponseEntity<ErrorResponse> response,
            HttpStatus expectedStatus,
            String expectedError,
            String expectedMessage
    ) {
        assertEquals(expectedStatus, response.getStatusCode());
        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();

        assertEquals(expectedStatus.value(), body.getStatus());
        assertEquals(expectedError, body.getError());
        assertEquals(expectedMessage, body.getMessage());
        assertEquals("/test", body.getPath());
        assertNotNull(body.getTimestamp());
    }
}


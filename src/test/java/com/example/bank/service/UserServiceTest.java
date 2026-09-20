package com.example.bank.service;

import com.example.bank.actuator.AccountMetrics;
import com.example.bank.actuator.UserMetrics;
import com.example.bank.dto.UserResponse;
import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.exception.ResourceConflictException;
import com.example.bank.exception.UserAlreadyExistsException;
import com.example.bank.model.*;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.bank.exception.UserNotFoundException;
import com.example.bank.dto.CreateAccountRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — unit тесты")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @Mock
    private UserMetrics userMetrics;

    @Mock
    private AccountMetrics accountMetrics;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Все зависимости создаются Mockito автоматически.
        // Здесь отдельная настройка пока не требуется.
    }

    @Test
    @DisplayName("Создание нового пользователя проходит успешно")
    void createUser_shouldCreateUserSuccessfully() {

        // Arrange
        String username = "eldar";
        String email = "eldar@example.com";
        String fullName = "Eldar";
        String password = "123456";

        String encodedPassword = "encoded-password";

        when(userRepository.existsByUserName(username))
                .thenReturn(false);

        when(userRepository.existsByEmail(email))
                .thenReturn(false);

        when(passwordEncoder.encode(password))
                .thenReturn(encodedPassword);

        User savedUser = new User(
                username,
                email,
                fullName,
                encodedPassword
        );

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        // Act
        User result = userService.createUser(
                username,
                email,
                fullName,
                password
        );

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUserName());
        assertEquals(email, result.getEmail());
        assertEquals(fullName, result.getFullName());

        // Проверяем, что пароль был захеширован,
        // а не сохранён в открытом виде.
        assertEquals(encodedPassword, result.getPasswordHash());

        verify(userRepository).existsByUserName(username);
        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Нельзя создать пользователя с уже существующим username")
    void createUser_duplicateUsername_shouldThrowException() {

        // Arrange
        String username = "eldar";
        String email = "eldar@example.com";
        String fullName = "Eldar";
        String password = "123456";

        when(userRepository.existsByUserName(username))
                .thenReturn(true);

        // Act + Assert
        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(
                        username,
                        email,
                        fullName,
                        password
                )
        );

        verify(userRepository)
                .existsByUserName(username);

        // Проверка email уже не должна выполняться.
        verify(userRepository, never())
                .existsByEmail(anyString());

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    @DisplayName("Нельзя создать пользователя с уже существующим email")
    void createUser_duplicateEmail_shouldThrowException() {

        // Arrange
        String username = "eldar";
        String email = "eldar@example.com";
        String fullName = "Eldar";
        String password = "123456";

        when(userRepository.existsByUserName(username))
                .thenReturn(false);

        when(userRepository.existsByEmail(email))
                .thenReturn(true);

        // Act + Assert
        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(
                        username,
                        email,
                        fullName,
                        password
                )
        );

        verify(userRepository)
                .existsByUserName(username);

        verify(userRepository)
                .existsByEmail(email);

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    @DisplayName("При создании пользователя сохраняется хешированный пароль")
    void createUser_shouldSaveEncodedPassword() {

        // Arrange
        String username = "eldar";
        String email = "eldar@example.com";
        String fullName = "Eldar";
        String rawPassword = "123456";
        String encodedPassword = "$2a$encoded-password";

        when(userRepository.existsByUserName(username))
                .thenReturn(false);

        when(userRepository.existsByEmail(email))
                .thenReturn(false);

        when(passwordEncoder.encode(rawPassword))
                .thenReturn(encodedPassword);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.createUser(
                username,
                email,
                fullName,
                rawPassword
        );

        // Assert
        verify(userRepository)
                .save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(username, savedUser.getUserName());
        assertEquals(email, savedUser.getEmail());
        assertEquals(fullName, savedUser.getFullName());

        assertEquals(
                encodedPassword,
                savedUser.getPasswordHash()
        );

        assertNotEquals(
                rawPassword,
                savedUser.getPasswordHash()
        );

        verify(passwordEncoder)
                .encode(rawPassword);
    }


    @Test
    @DisplayName("Удаление пользователя без счетов проходит успешно")
    void deleteUser_withoutAccounts_shouldDeleteUser() {

        // Arrange
        Long userId = 1L;

        User user = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        user.getAccounts().clear();

        when(userRepository.findWithAccountsById(userId))
                .thenReturn(java.util.Optional.of(user));

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository)
                .findWithAccountsById(userId);

        verify(userRepository)
                .delete(user);
    }

    @Test
    @DisplayName("Нельзя удалить пользователя, у которого есть банковские счета")
    void deleteUser_withAccounts_shouldThrowConflict() {

        // Arrange
        Long userId = 1L;

        User user = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        user.addAccount(new BankAccount("eldar"));

        when(userRepository.findWithAccountsById(userId))
                .thenReturn(java.util.Optional.of(user));

        // Act + Assert
        assertThrows(
                ResourceConflictException.class,
                () -> userService.deleteUser(userId)
        );

        verify(userRepository)
                .findWithAccountsById(userId);

        verify(userRepository, never())
                .delete(any(User.class));
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя выбрасывает AccountNotFoundException")
    void deleteUser_notFound_shouldThrowException() {

        // Arrange
        Long userId = 999L;

        when(userRepository.findWithAccountsById(userId))
                .thenReturn(java.util.Optional.empty());

        // Act + Assert
        assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(userId)
        );

        verify(userRepository)
                .findWithAccountsById(userId);

        verify(userRepository, never())
                .delete(any(User.class));
    }


    @Test
    @DisplayName("Изменение активности пользователя на false проходит успешно")
    void updateActive_shouldDeactivateUser() {

        // Arrange
        Long userId = 1L;

        User user = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        user.activate();

        when(userRepository.findById(userId))
                .thenReturn(java.util.Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.updateStatus(userId, false);

        // Assert
        assertNotNull(result);
        assertFalse(result.isActive());

        verify(userRepository)
                .findById(userId);

        verify(userRepository)
                .save(user);
    }

    @Test
    @DisplayName("Изменение активности пользователя на true проходит успешно")
    void updateActive_shouldActivateUser() {

        // Arrange
        Long userId = 1L;

        User user = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        user.deactivate();

        when(userRepository.findById(userId))
                .thenReturn(java.util.Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.updateStatus(userId, true);

        // Assert
        assertNotNull(result);
        assertTrue(result.isActive());

        verify(userRepository)
                .findById(userId);

        verify(userRepository)
                .save(user);
    }

    @Test
    @DisplayName("Изменение активности несуществующего пользователя выбрасывает AccountNotFoundException")
    void updateActive_notFound_shouldThrowException() {

        // Arrange
        Long userId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(java.util.Optional.empty());

        // Act + Assert
        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateStatus(userId, false)
        );

        verify(userRepository)
                .findById(userId);

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    @DisplayName("Получение всех пользователей с преобразованием в UserResponse")
    void getAllUsersCached_shouldReturnUserResponses() {

        User user = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        when(userRepository.findAll())
                .thenReturn(java.util.List.of(user));

        java.util.List<com.example.bank.dto.UserResponse> result =
                userService.getAllUsersCached();

        assertNotNull(result);
        assertEquals(1, result.size());

        com.example.bank.dto.UserResponse response = result.get(0);

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUserName(), response.getUserName());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getFullName(), response.getFullName());
        assertEquals(user.isActive(), response.isActive());

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Получение всех пользователей возвращает список из репозитория")
    void getAllUsers_shouldReturnUsers() {

        User user1 = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        User user2 = new User(
                "admin",
                "admin@example.com",
                "Admin",
                "encoded-password"
        );

        when(userRepository.findAll())
                .thenReturn(java.util.List.of(user1, user2));

        java.util.List<User> result =
                userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(user1, result.get(0));
        assertSame(user2, result.get(1));

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Поиск пользователя по ID проходит успешно")
    void findById_shouldReturnUser() {

        Long userId = 1L;

        User user = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        when(userRepository.findById(userId))
                .thenReturn(java.util.Optional.of(user));

        User result = userService.findById(userId);

        assertNotNull(result);
        assertSame(user, result);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Поиск пользователя по ID выбрасывает UserNotFoundException")
    void findById_notFound_shouldThrowException() {

        Long userId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.findById(userId)
        );

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Получение UserResponse по ID проходит успешно")
    void findUserResponseById_shouldReturnResponse() {

        Long userId = 1L;

        User user = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        when(userRepository.findById(userId))
                .thenReturn(java.util.Optional.of(user));

        UserResponse result =
                userService.findUserResponseById(userId);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUserName(), result.getUserName());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getFullName(), result.getFullName());
        assertEquals(user.isActive(), result.isActive());

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Получение UserResponse для несуществующего пользователя выбрасывает исключение")
    void findUserResponseById_notFound_shouldThrowException() {

        Long userId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.findUserResponseById(userId)
        );

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Создание DEBIT счёта для пользователя проходит успешно")
    void createAccountForUser_debit_shouldCreateAccount() {

        Long userId = 1L;

        User user = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        CreateAccountRequest request =
                mock(CreateAccountRequest.class);

        when(request.getAccountType())
                .thenReturn(AccountType.DEBIT);

        when(userRepository.findWithAccountsById(userId))
                .thenReturn(java.util.Optional.of(user));

        when(accountNumberGenerator.generate())
                .thenReturn("4081-7910-1111-2222-3333");

        when(accountRepository.findByAccountNumber(
                "4081-7910-1111-2222-3333"
        )).thenReturn(java.util.Optional.empty());

        when(accountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount result =
                userService.createAccountForUser(userId, request);

        assertNotNull(result);
        assertTrue(result instanceof BankAccount);

        assertEquals(
                "4081-7910-1111-2222-3333",
                result.getAccountNumber()
        );

        assertEquals(
                "eldar",
                result.getOwner()
        );

        assertTrue(user.getAccounts().contains(result));

        verify(accountNumberGenerator).generate();

        verify(accountRepository)
                .findByAccountNumber("4081-7910-1111-2222-3333");

        verify(accountRepository)
                .save(any(BankAccount.class));

        verify(accountMetrics).accountCreated();
    }

    @Test
    @DisplayName("Создание SAVINGS счёта проходит успешно")
    void createAccountForUser_savings_shouldCreateSavingsAccount() {

        Long userId = 1L;

        User user = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        CreateAccountRequest request =
                mock(CreateAccountRequest.class);

        when(request.getAccountType())
                .thenReturn(AccountType.SAVINGS);

        when(request.getInitialBalance())
                .thenReturn(new java.math.BigDecimal("1000"));

        when(request.getInterestRate())
                .thenReturn(new java.math.BigDecimal("5"));

        when(userRepository.findWithAccountsById(userId))
                .thenReturn(java.util.Optional.of(user));

        when(accountNumberGenerator.generate())
                .thenReturn("4081-7910-1111-2222-3333");

        when(accountRepository.findByAccountNumber(
                "4081-7910-1111-2222-3333"
        )).thenReturn(java.util.Optional.empty());

        when(accountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount result =
                userService.createAccountForUser(userId, request);

        assertNotNull(result);
        assertTrue(result instanceof SavingsAccount);

        assertEquals(
                "4081-7910-1111-2222-3333",
                result.getAccountNumber()
        );

        assertTrue(user.getAccounts().contains(result));

        verify(accountMetrics).accountCreated();
    }

    @Test
    @DisplayName("Создание CREDIT счёта проходит успешно")
    void createAccountForUser_credit_shouldCreateCreditAccount() {

        Long userId = 1L;

        User user = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        CreateAccountRequest request =
                mock(CreateAccountRequest.class);

        when(request.getAccountType())
                .thenReturn(AccountType.CREDIT);

        when(request.getCreditLimit())
                .thenReturn(new java.math.BigDecimal("50000"));

        when(request.getInterestRate())
                .thenReturn(new java.math.BigDecimal("10"));

        when(userRepository.findWithAccountsById(userId))
                .thenReturn(java.util.Optional.of(user));

        when(accountNumberGenerator.generate())
                .thenReturn("4081-7910-1111-2222-3333");

        when(accountRepository.findByAccountNumber(
                "4081-7910-1111-2222-3333"
        )).thenReturn(java.util.Optional.empty());

        when(accountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount result =
                userService.createAccountForUser(userId, request);

        assertNotNull(result);
        assertTrue(result instanceof CreditAccount);

        assertEquals(
                "4081-7910-1111-2222-3333",
                result.getAccountNumber()
        );

        assertTrue(user.getAccounts().contains(result));

        verify(accountMetrics).accountCreated();
    }

    @Test
    @DisplayName("Создание счёта для несуществующего пользователя выбрасывает исключение")
    void createAccountForUser_userNotFound_shouldThrowException() {

        Long userId = 999L;

        CreateAccountRequest request =
                mock(CreateAccountRequest.class);

        when(userRepository.findWithAccountsById(userId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.createAccountForUser(userId, request)
        );

        verify(accountNumberGenerator, never()).generate();
        verify(accountRepository, never()).save(any(BankAccount.class));
        verify(accountMetrics, never()).accountCreated();
    }

    @Test
    @DisplayName("При занятом номере счёта генератор создаёт новый номер")
    void createAccountForUser_duplicateAccountNumber_shouldGenerateAnotherNumber() {

        Long userId = 1L;

        User user = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        CreateAccountRequest request =
                mock(CreateAccountRequest.class);

        when(request.getAccountType())
                .thenReturn(AccountType.DEBIT);

        when(userRepository.findWithAccountsById(userId))
                .thenReturn(java.util.Optional.of(user));

        when(accountNumberGenerator.generate())
                .thenReturn(
                        "4081-7910-1111-2222-3333",
                        "4081-7910-4444-5555-6666"
                );

        when(accountRepository.findByAccountNumber(
                "4081-7910-1111-2222-3333"
        )).thenReturn(java.util.Optional.of(new BankAccount("other")));

        when(accountRepository.findByAccountNumber(
                "4081-7910-4444-5555-6666"
        )).thenReturn(java.util.Optional.empty());

        when(accountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount result =
                userService.createAccountForUser(userId, request);

        assertEquals(
                "4081-7910-4444-5555-6666",
                result.getAccountNumber()
        );

        verify(accountNumberGenerator, times(2))
                .generate();

        verify(accountRepository)
                .findByAccountNumber(
                        "4081-7910-4444-5555-6666"
                );
    }

    @Test
    @DisplayName("Получение пользователя со счетами проходит успешно")
    void getUserWithAccounts_shouldReturnUser() {

        Long userId = 1L;

        User user = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        user.addAccount(new BankAccount("eldar"));

        when(userRepository.findWithAccountsById(userId))
                .thenReturn(java.util.Optional.of(user));

        User result =
                userService.getUserWithAccounts(userId);

        assertNotNull(result);
        assertSame(user, result);
        assertEquals(1, result.getAccounts().size());

        verify(userRepository)
                .findWithAccountsById(userId);
    }

    @Test
    @DisplayName("Получение пользователя со счетами для несуществующего пользователя выбрасывает исключение")
    void getUserWithAccounts_notFound_shouldThrowException() {

        Long userId = 999L;

        when(userRepository.findWithAccountsById(userId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserWithAccounts(userId)
        );

        verify(userRepository)
                .findWithAccountsById(userId);
    }

    @Test
    @DisplayName("Перевод счёта другому пользователю проходит успешно")
    void transferAccountOwnership_shouldTransferAccount() {

        Long accountId = 1L;
        Long newUserId = 2L;

        User oldUser = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        User newUser = new User(
                "admin",
                "admin@example.com",
                "Admin",
                "encoded-password"
        );

        BankAccount account =
                new BankAccount("eldar");

        oldUser.addAccount(account);

        when(accountRepository.findById(accountId))
                .thenReturn(java.util.Optional.of(account));

        when(userRepository.findById(newUserId))
                .thenReturn(java.util.Optional.of(newUser));

        userService.transferAccountOwnership(
                accountId,
                newUserId
        );

        assertFalse(oldUser.getAccounts().contains(account));
        assertTrue(newUser.getAccounts().contains(account));

        verify(accountRepository)
                .findById(accountId);

        verify(userRepository)
                .findById(newUserId);
    }

    @Test
    @DisplayName("Перевод несуществующего счёта выбрасывает AccountNotFoundException")
    void transferAccountOwnership_accountNotFound_shouldThrowException() {

        Long accountId = 999L;
        Long newUserId = 2L;

        when(accountRepository.findById(accountId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> userService.transferAccountOwnership(
                        accountId,
                        newUserId
                )
        );

        verify(userRepository, never())
                .findById(anyLong());
    }

    @Test
    @DisplayName("Перевод счёта несуществующему пользователю выбрасывает UserNotFoundException")
    void transferAccountOwnership_newUserNotFound_shouldThrowException() {

        Long accountId = 1L;
        Long newUserId = 999L;

        User oldUser = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        BankAccount account =
                new BankAccount("eldar");

        oldUser.addAccount(account);

        when(accountRepository.findById(accountId))
                .thenReturn(java.util.Optional.of(account));

        when(userRepository.findById(newUserId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.transferAccountOwnership(
                        accountId,
                        newUserId
                )
        );

        assertTrue(oldUser.getAccounts().contains(account));

        verify(userRepository)
                .findById(newUserId);
    }

    @Test
    @DisplayName("Очистка кэша пользователей выполняется")
    void clearUsersCache_shouldCompleteSuccessfully() {

        assertDoesNotThrow(
                () -> userService.clearUsersCache()
        );

        verifyNoInteractions(
                userRepository,
                accountRepository,
                passwordEncoder,
                accountNumberGenerator,
                accountMetrics,
                userMetrics
        );
    }

    @Test
    @DisplayName("Неизвестный тип счёта выбрасывает IllegalArgumentException")
    void createAccountForUser_unknownAccountType_shouldThrowException() {

        Long userId = 1L;

        User user = new User(
                "eldar",
                "eldar@example.com",
                "Eldar",
                "encoded-password"
        );

        CreateAccountRequest request =
                mock(CreateAccountRequest.class);

        when(request.getAccountType())
                .thenReturn(null);

        when(userRepository.findWithAccountsById(userId))
                .thenReturn(java.util.Optional.of(user));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createAccountForUser(userId, request)
        );

        assertEquals(
                "Неизвестный тип счёта",
                exception.getMessage()
        );

        verify(accountNumberGenerator, never())
                .generate();

        verify(accountRepository, never())
                .save(any(BankAccount.class));

        verify(accountMetrics, never())
                .accountCreated();
    }

}

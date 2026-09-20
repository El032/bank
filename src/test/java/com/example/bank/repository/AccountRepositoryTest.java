package com.example.bank.repository;

import com.example.bank.config.TestcontainersConfig;
import com.example.bank.model.AccountStatus;
import com.example.bank.model.BankAccount;
import com.example.bank.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfig.class)
@DisplayName("AccountRepository — интеграционные тесты")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private BankAccount savedAccount;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll(); // чистим перед каждым тестом
        User user = new User(
                "eldar",
                "Eldar@test.com",
                "Eldar",
                "password"
        );

        userRepository.save(user);


        BankAccount account =
                new BankAccount("Eldar");

        user.addAccount(account);

        savedAccount = repository.save(account);
        repository.flush();
    }

    @Test
    @DisplayName("Сохранённый счёт получает ID от БД")
    void save_shouldAssignIdFromDatabase() {
        assertNotNull(savedAccount.getId());
        assertTrue(savedAccount.getId() > 0);
    }

    @Test
    @DisplayName("findById возвращает сохранённый счёт")
    void findById_shouldReturnSavedAccount() {
        Optional<BankAccount> found = repository.findById(savedAccount.getId());

        assertTrue(found.isPresent());
        assertEquals("Eldar", found.get().getOwner());
        assertEquals(new BigDecimal("0.00"), found.get().getBalance());
    }

    @Test
    @DisplayName("findById несуществующего ID возвращает пустой Optional")
    void findById_notFound_shouldReturnEmpty() {
        Optional<BankAccount> found = repository.findById(9999L);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("findByStatus возвращает счета с нужным статусом")
    void findByStatus_shouldReturnOnlyMatchingAccounts(){
        BankAccount inactive = new BankAccount("Kola");
        inactive.block();
        savedAccount.getUser().addAccount(inactive);
        repository.save(inactive);

        List<BankAccount> active = repository.findByStatus(AccountStatus.ACTIVE);
        List<BankAccount> blocked = repository.findByStatus(AccountStatus.BLOCKED);

        assertEquals(1, active.size());
        assertEquals("Eldar", active.get(0).getOwner());
        assertEquals(1, blocked.size());
        assertEquals("Kola", blocked.get(0).getOwner());
    }

    @Test
    @DisplayName("findByOwnerContainingIgnoreCase ищет по части имени без учёта регистра")
    void findByOwnerContaining_shouldBeCaseInsensitive() {
        BankAccount elena = new BankAccount("Elena");
        savedAccount.getUser().addAccount(elena);
        repository.save(elena);

        List<BankAccount> results = repository
                .findByOwnerContainingIgnoreCase("el");

        assertEquals(2, results.size()); // Eldar и Elena
    }

    @Test
    @DisplayName("findByBalanceRange возвращает счета в диапазоне")
    void findByBalanceRange_shouldReturnAccountsInRange() {
        BankAccount kola = new BankAccount("Kola");
        savedAccount.getUser().addAccount(kola);
        kola.deposit(new BigDecimal("500.00"));
        repository.save(kola);

        BankAccount lola = new BankAccount("Lola");
        savedAccount.getUser().addAccount(lola);
        lola.deposit(new BigDecimal("1000.00"));
        repository.save(lola);


        List<BankAccount> result = repository.findByBalanceRange(
                new BigDecimal("400.00"),
                new BigDecimal("1500.00"));

        assertEquals(2, result.size()); // 500 и 1000
    }

    @Test
    @DisplayName("deleteById реально удаляет из БД")
    void deleteById_shouldRemoveFromDatabase() {
        Long id = savedAccount.getId();
//        repository.delete(savedAccount);
//        repository.flush();
//        entityManager.clear();
        savedAccount.getUser().removeAccount(savedAccount);
        entityManager.flush();
        entityManager.clear();

        assertFalse(repository.findById(id).isPresent());
        assertEquals(0, repository.count());
    }

    @Test
    @DisplayName("count возвращает актуальное количество")
    void count_shouldReturnCorrectNumber() {
        BankAccount account = new BankAccount("Kola");
        account.setUser(savedAccount.getUser());
        repository.save(account);
        assertEquals(2, repository.count());
    }

    @Test
    @DisplayName("Flyway создал таблицу accounts")
    void flyway_shouldCreateAccountsTable() {
        Boolean exists = jdbcTemplate.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1
                    FROM information_schema.tables
                    WHERE table_name = 'accounts'
                )
                """,
                Boolean.class
        );

        assertTrue(exists);
    }
}
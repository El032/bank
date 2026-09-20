package com.example.bank.service;



import com.example.bank.actuator.TransferMetrics;
import com.example.bank.event.TransferCompletedEvent;
import com.example.bank.model.*;
import com.example.bank.exception.*;
import com.example.bank.repository.*;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.bank.repository.OutboxEventRepository;
import com.example.bank.model.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class TransferService {

    private static final Logger log =
            LoggerFactory.getLogger(TransferService.class);



    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;
    private final TransferMetrics transferMetrics;


    private static final BigDecimal MONTHLY_TRANSFER_LIMIT =
            new BigDecimal("500000.00");


    public TransferService(AccountRepository accountRepository,
                           TransactionRepository transactionRepository,
                           TransferRepository transferRepository,
                           UserRepository userRepository,
                           ObjectMapper objectMapper,
                           OutboxEventRepository outboxEventRepository,
                           TransferMetrics transferMetrics) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferRepository = transferRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.outboxEventRepository = outboxEventRepository;
        this.transferMetrics = transferMetrics;


    }

    @Transactional(rollbackFor = Exception.class)
    public Transfer transfer(Long fromId, Long toId, BigDecimal amount) {

        log.info("Начало перевода: fromId={}, toId={}, amount={}",
                fromId, toId, amount);

        Timer.Sample timerSample = transferMetrics.startTimer();

        transferMetrics.transferStarted();

        try {
        // 1. Валидация входных данных
        if (fromId.equals(toId)) {
            throw new TransferException("Нельзя перевести деньги на тот же счёт");
        }

        if (amount == null){
            throw new InvalidAmountException();
        }
        if(amount.compareTo(BigDecimal.TEN) < 0) {
            throw new TransferMinimumAmountException(BigDecimal.TEN);
        }

        // 2. Загружаем оба счёта
        BankAccount from = accountRepository.findById(fromId)
                .orElseThrow(() -> new AccountNotFoundException(fromId));
        BankAccount to = accountRepository.findById(toId)
                .orElseThrow(() -> new AccountNotFoundException(toId));

        // 3. Проверяем активность счетов
        if (!from.isActive()) {
            throw new InactiveAccountException(fromId);
        }
        if (!to.isActive()) {
            throw new InactiveAccountException(toId);
        }

        if (from instanceof SavingsAccount || to instanceof SavingsAccount) {
            throw new TransferException(
                    "Переводы с накопительного счёта и на накопительный счёт запрещены"
            );
        }

         //4. Проверка пользователей
        String fromUsername = from.getOwner();
        String toUsername = to.getOwner();

        User fromUser = userRepository.findByUserName(fromUsername)
                .orElseThrow(() -> new UserInactiveException(fromUsername));

        User toUser = userRepository.findByUserName(toUsername)
                .orElseThrow(() -> new UserInactiveException(toUsername));

        if (!fromUser.isActive()) {
            throw new UserInactiveException(fromUsername);
        }

        if (!toUser.isActive()) {
            throw new UserInactiveException(toUsername);
        }


        //проверка месячного лимита
        LocalDate firstDayOfMonth = LocalDate.now()
                .withDayOfMonth(1);

        LocalDateTime fromDate = firstDayOfMonth.atStartOfDay();

        LocalDateTime toDate = firstDayOfMonth
                .plusMonths(1)
                .atStartOfDay();

        BigDecimal transferredThisMonth =
                transferRepository.sumOutgoingTransfers(
                        fromId,
                        fromDate,
                        toDate
                );

        BigDecimal totalAfterTransfer =
                transferredThisMonth.add(amount);

        if (totalAfterTransfer.compareTo(MONTHLY_TRANSFER_LIMIT) > 0) {
            throw new TransferMonthlyLimitException(
                    MONTHLY_TRANSFER_LIMIT,
                    transferredThisMonth,
                    amount
            );
        }

        // 6. Выполняем перевод
        from.withdraw(amount);
        to.deposit(amount);


        // dirty checking сохранит оба счёта автоматически при commit

        // 7. Записываем транзакции в истории обоих счетов
        transactionRepository.save(
                new Transaction(
                        from,
                        amount,
                        TransactionType.WITHDRAW,
                        TransactionSource.TRANSFER
                )
        );
        transactionRepository.save(
                new Transaction(
                        to,
                        amount,
                        TransactionType.DEPOSIT,
                        TransactionSource.TRANSFER
                )
        );

        // 8. Сохраняем запись о переводе
        Transfer transfer = new Transfer(from, to, amount);
        Transfer saved = transferRepository.save(transfer);



        TransferCompletedEvent event = new TransferCompletedEvent(
                saved.getId(),
                from.getId(),
                to.getId(),
                from.getOwner(),
                to.getOwner(),
                amount
        );

        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = new OutboxEvent(
                    "TRANSFER_COMPLETED",
                    "Transfer",
                    saved.getId(),
                    payload
            );

            outboxEventRepository.save(outboxEvent);

            transferMetrics.transferSucceeded();

            transferMetrics.recordAmount(amount.doubleValue());

            log.info("Перевод успешно выполнен: transferId={}, fromId={}, toId={}, amount={}",
                    saved.getId(), fromId, toId, amount);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации TransferCompletedEvent", e);
        }



        return saved;

        // При выходе из метода — Spring делает COMMIT для всех 5 операций разом
        // Если что-то пошло не так выше — ROLLBACK, ничего не применится
        } catch (InsufficientFundsException e) {

            transferMetrics.transferFailed();

            log.warn("Недостаточно средств: fromId={}, amount={}, message={}",
                    fromId, amount, e.getMessage());
            throw e;
        } catch (Exception e) {

            transferMetrics.transferFailed();

            log.error("Неожиданная ошибка при переводе: fromId={}, toId={}, amount={}",
                    fromId, toId, amount, e);
            throw e;
        }
        finally {
            transferMetrics.stopTimer(timerSample);
            transferMetrics.transferFinished();
        }
    }



    public List<Transfer> getTransferHistory(Long accountId) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        List<Transfer> outgoing =
                transferRepository.findByFromAccountId(accountId);

        List<Transfer> incoming =
                transferRepository.findByToAccountId(accountId);

        outgoing.addAll(incoming);

        outgoing.sort(
                Comparator.comparing(Transfer::getCreatedAt).reversed());
        return outgoing;


    }




}
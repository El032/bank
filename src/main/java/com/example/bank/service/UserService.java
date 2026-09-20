package com.example.bank.service;

import com.example.bank.actuator.AccountMetrics;
import com.example.bank.actuator.UserMetrics;
import com.example.bank.dto.CreateAccountRequest;
import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.exception.ResourceConflictException;
import com.example.bank.exception.UserAlreadyExistsException;
import com.example.bank.exception.UserNotFoundException;
import com.example.bank.model.*;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import com.example.bank.dto.UserResponse;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountMetrics accountMetrics;
    private final UserMetrics userMetrics;

    public UserService(UserRepository userRepository,
                       AccountRepository accountRepository,
                       PasswordEncoder passwordEncoder,
                       AccountNumberGenerator accountNumberGenerator,
                       AccountMetrics accountMetrics,
                       UserMetrics userMetrics) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountNumberGenerator = accountNumberGenerator;
        this.accountMetrics = accountMetrics;
        this.userMetrics = userMetrics;
    }


   // @CacheEvict(value = "users", key = "'all'")
   @CacheEvict(value = "users-list", key = "'all'")
    public User createUser(String userName,String email,String fullName,String password) {
        if(userRepository.existsByUserName(userName)){
            throw new UserAlreadyExistsException("Username уже занят" + userName);
        }
        if(userRepository.existsByEmail(email)){
            throw new UserAlreadyExistsException("Email уже используется" + email);
        }

        String encodedPassword = passwordEncoder.encode(password);


       User savedUser = userRepository.save(
               new User(
                       userName,
                       email,
                       fullName,
                       encodedPassword
               )
       );

       userMetrics.userCreated();
       return savedUser;

// это поменял если не работает удалить      User savedUser = userRepository.save(
//               new User(
//                       userName,
//                       email,
//                       fullName,
//                       encodedPassword
//               )
//       );
//
//       userMetrics.userCreated();
//       return savedUser;
       //вернуть реурт что ниже

//        return userRepository.save(
//                new User(
//                        userName,
//                        email,
//                        fullName,
//                        encodedPassword
//                )
//        );

    }

    // @Cacheable(value = "users", key = "'all'")
    @Cacheable(value = "users-list", key = "'all'")
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsersCached() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUserName(),
                        user.getEmail(),
                        user.getFullName(),
                        user.isActive()
                ))
                .toList();
    }

    // Кэшируем список пользователей — но с осторожностью
    //@Cacheable(value = "users", key = "'all'")
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {return userRepository.findAll();
    }


    // Кэшируем результат — повторный вызов с тем же id не идёт в БД
    //@Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException(id));
    }

    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public UserResponse findUserResponseById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getFullName(),
                user.isActive()
        );
    }


    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "users-list", key = "'all'")
    })
    @Transactional
    public void deleteUser(Long id) {

        User user = userRepository.findWithAccountsById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getAccounts().isEmpty()) {
            throw new ResourceConflictException(
                    "Невозможно удалить пользователя: у него есть банковские счета"
            );
        }

        userRepository.delete(user);
    }


    //создаем счет и сразу привязываем его к пользователю
    @Transactional
    public BankAccount createAccountForUser(Long userId, CreateAccountRequest request) {
        User user = userRepository.findWithAccountsById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));

        BankAccount account;

        if (request.getAccountType() == AccountType.DEBIT) {
            account = new BankAccount(user.getUserName());
        } else if (request.getAccountType() == AccountType.SAVINGS) {
            account = new SavingsAccount(
                    user.getUserName(),
                    request.getInitialBalance(),
                    request.getInterestRate()
            );
        }else if(request.getAccountType() == AccountType.CREDIT){
            account = new CreditAccount(
                    user.getUserName(),
                    request.getCreditLimit(),
                    request.getInterestRate()

            );
        }
        else {
            throw new IllegalArgumentException("Неизвестный тип счёта");
        }

        String accountNumber;

        do {
            accountNumber = accountNumberGenerator.generate();
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());

        account.setAccountNumber(accountNumber);

        user.addAccount(account);


        BankAccount savedAccount = accountRepository.save(account);

        accountMetrics.accountCreated();

        return savedAccount;
    }
    //получить пользователя со всеми счетами
    public User getUserWithAccounts(Long userId){
        return userRepository.findWithAccountsById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));
    }

    //перевести счёт от одного пользователя к другому
    @Transactional
    public void transferAccountOwnership(Long accountId, Long newUserId){
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(()-> new AccountNotFoundException(accountId));

        User oldUser = account.getUser();
        User newUser = userRepository.findById(newUserId)
                .orElseThrow(()-> new UserNotFoundException(newUserId));

      if( oldUser != null){
          oldUser.removeAccount(account);// убираем со старого владельца
      }
      newUser.addAccount(account);//добавляем к новому
    }

//    @Caching(evict = {
//        @CacheEvict(value = "users", key = "#id"),
//        @CacheEvict(value = "users", key = "'all'")
//    })
@Caching(evict = {
        @CacheEvict(value = "users", key = "#id"),
        @CacheEvict(value = "users-list", key = "'all'")
})
    @Transactional
public User updateStatus(Long id, Boolean newActive) {

    User user = findById(id);

    if (newActive) {
        user.activate();
    } else {
        user.deactivate();
    }

    return userRepository.save(user);
}

    // Очистить весь кэш пользователей (например, при массовом обновлении)
    @CacheEvict(value = "users", allEntries = true)
    public void clearUsersCache() {
        // просто инвалидируем кэш — ничего больше
    }


}

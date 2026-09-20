package com.example.bank.repository;

import com.example.bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUserName(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUserName(String username);

    boolean existsByEmail(String email);

    //загрузить позователей вместе со счетом(решаем проблему N+1)
    @Query("""
SELECT u 
FROM User u 
 LEFT JOIN FETCH u.accounts 
 WHERE u.id = :id""")
    Optional<User> findWithAccountsById(Long id);

    @Query("""
SELECT DISTINCT u
FROM User u
LEFT JOIN FETCH u.accounts
""")
    List<User> findAllWithAccounts();
}

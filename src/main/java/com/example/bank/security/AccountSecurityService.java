package com.example.bank.security;


import com.example.bank.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountSecurityService {

    private final AccountRepository accountRepository;

    public AccountSecurityService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public boolean isOwner(Long accountId, String username) {

        return accountRepository.findById(accountId)
                .map(account ->
                        account.getUser() != null
                                && account.getUser().getUserName().equals(username)
                )
                .orElse(false);
    }


}
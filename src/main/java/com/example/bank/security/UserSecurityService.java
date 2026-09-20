package com.example.bank.security;

import com.example.bank.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service("userSecurityService")
public class UserSecurityService {

    private final UserRepository userRepository;

    public UserSecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isOwner(Long userId, String userName) {
        return userRepository.findById(userId)
                .map(user -> user.getUserName().equals(userName))
                .orElse(false);
    }
}
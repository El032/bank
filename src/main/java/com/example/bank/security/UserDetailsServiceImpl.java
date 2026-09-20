package com.example.bank.security;

import com.example.bank.model.User;
import com.example.bank.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userName)
            throws UsernameNotFoundException {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь не найден: " + userName));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUserName())
                .password(user.getPasswordHash())
                .authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                .disabled(!user.isActive())
                .build();
    }
}
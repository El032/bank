package com.example.bank.service;

import com.example.bank.actuator.UserMetrics;
import com.example.bank.exception.UserAlreadyExistsException;
import com.example.bank.exception.UserInactiveException;
import com.example.bank.model.User;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.JwtService;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.DisabledException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMetrics userMetrics;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserMetrics userMetrics) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMetrics = userMetrics;
    }

    public User register(String userName, String email,
                         String password, String fullName) {
        if (userRepository.existsByUserName(userName)) {
            throw new UserAlreadyExistsException("Username занят: " + userName);
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email занят: " + email);
        }
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(userName, email, fullName, hashedPassword);
        User savedUser = userRepository.save(user);
        userMetrics.userCreated();
        return savedUser;

        //если не будет работать удалить  User savedUser = userRepository.save(user);
        //        userMetrics.userCreated();
        //        return savedUser;
        // и вернуть ретурн что ниже
        //return userRepository.save(user);
    }

    // Теперь возвращаем JWT токен
    public String login(String userName, String password) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userName, password));

            return jwtService.generateToken(userName);

        } catch (DisabledException e) {
            throw new UserInactiveException();
        }
    }
}
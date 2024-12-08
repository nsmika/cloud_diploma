package ru.netology.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.netology.repository.ClientRepository;
import ru.netology.security.JwtTokenProvider;
import ru.netology.security.JwtTokenStore;

@Slf4j
@Service
public class AuthService {

    private final ClientRepository clientRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenStore jwtTokenStore;
    private final PasswordEncoder passwordEncoder;

    public AuthService(ClientRepository clientRepository, JwtTokenProvider jwtTokenProvider, JwtTokenStore jwtTokenStore, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtTokenStore = jwtTokenStore;
        this.passwordEncoder = passwordEncoder;
    }

    public String authenticate(String username, String password) {
        log.info("Authenticating user: {}", username);
        var user = clientRepository.findByLogin(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new BadCredentialsException("Invalid login or password");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Invalid password for user: {}", username);
            throw new BadCredentialsException("Invalid login or password");
        }

        String token = jwtTokenProvider.createToken(username);
        jwtTokenStore.saveToken(username, token);
        log.info("Authentication successful for user: {}", username);
        return token;
    }

    public void logout(String username) {
        log.info("Logging out user: {}", username);
        jwtTokenStore.deleteToken(username);
    }
}


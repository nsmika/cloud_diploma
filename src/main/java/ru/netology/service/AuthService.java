package ru.netology.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.netology.exception.UnauthorizedException;
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
                    return new UnauthorizedException("Invalid login or password");
                });
// Проверка: если пароль не зашифрован, зашифровать и обновить его
        if (!user.getPassword().startsWith("$2a$")) {
            log.warn("Unencrypted password found for user: {}. Encrypting now...", username);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            clientRepository.save(user);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Invalid password for user: {}", username);
            throw new UnauthorizedException("Invalid login or password");
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


package ru.netology.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.netology.entity.Client;
import ru.netology.exception.UnauthorizedException;
import ru.netology.repository.ClientRepository;
import ru.netology.security.JwtTokenProvider;
import ru.netology.security.JwtTokenStore;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtTokenStore jwtTokenStore;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticate_ShouldReturnToken_WhenCredentialsAreValid() {
        String username = "user@test.org";
        String password = "password";
        String token = "validToken";

        Client client = new Client(username, password);
        when(clientRepository.findByLogin(username)).thenReturn(Optional.of(client));
        when(passwordEncoder.matches(password, client.getPassword())).thenReturn(true);
        when(jwtTokenProvider.createToken(username)).thenReturn(token);

        String result = authService.authenticate(username, password);

        assertEquals(token, result);
        verify(jwtTokenStore).saveToken(username, token);
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserNotFound() {
        String username = "user@test.org";
        String password = "password";

        when(clientRepository.findByLogin(username)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.authenticate(username, password));
    }

    @Test
    void logout_ShouldRemoveToken() {
        String username = "user@test.org";

        authService.logout(username);

        verify(jwtTokenStore).deleteToken(username);
    }
}

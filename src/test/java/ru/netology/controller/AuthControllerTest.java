package ru.netology.controller;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import ru.netology.dto.LoginRequest;
import ru.netology.service.AuthService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    AuthControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_ShouldReturnToken() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user@test.org", "password");
        String expectedToken = "validToken";
        when(authService.authenticate(loginRequest.getLogin(), loginRequest.getPassword())).thenReturn(expectedToken);

        // Act
        ResponseEntity<Map<String, String>> response = authController.login(loginRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedToken, response.getBody().get("auth-token"));
    }

    @Test
    void logout_ShouldReturnSuccessMessage() {
        // Arrange
        String token = "validToken";

        // Act
        ResponseEntity<String> response = authController.logout(token);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Logged out successfully", response.getBody());
    }
}

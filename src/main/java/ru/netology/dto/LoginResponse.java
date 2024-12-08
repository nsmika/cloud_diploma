package ru.netology.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String authToken;

    public LoginResponse(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
    }
}
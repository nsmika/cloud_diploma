package ru.netology.security;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtTokenStore {

    private final ConcurrentHashMap<String, String> tokenStore = new ConcurrentHashMap<>();

    public void saveToken(String username, String token) {
        tokenStore.put(username, token);
    }

    public String getToken(String username) {
        return tokenStore.get(username);
    }

    public void deleteToken(String username) {
        tokenStore.remove(username);
    }

    public boolean isTokenValid(String username, String token) {
        return token.equals(tokenStore.get(username));
    }
}

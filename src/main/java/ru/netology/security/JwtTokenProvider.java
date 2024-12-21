package ru.netology.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey secretKey;

    @Value("${jwt.secret:}") // Если значение не задано в конфигурации, генерируем новый ключ
    private String secretKeyConfig;

    @PostConstruct
    public void init() {
        if (secretKeyConfig != null && !secretKeyConfig.isBlank()) {
            // Если ключ задан в конфигурации, используем его
            secretKey = Keys.hmacShaKeyFor(secretKeyConfig.getBytes(StandardCharsets.UTF_8));
            log.info("Secret key initialized from configuration.");
        } else {
            // Если ключ не задан, генерируем новый при каждом запуске
            secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            log.info("Generated a new unique secret key for this session.");
        }
    }

    public String createToken(String username) {
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey)
                .compact();
        //log.info("Generated token: {}", token); // Логируем сгенерированный токен, в продакшене удалить!
        return token;
    }


    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}



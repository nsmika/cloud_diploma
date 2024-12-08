package ru.netology.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenStore jwtTokenStore;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Логирование всех заголовков
        log.info("Incoming headers: {}", Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, request::getHeader)));

        String token = null;
        String authHeader = request.getHeader("Authorization");
        String authTokenHeader = request.getHeader("auth-token"); // Поддержка auth-token

        // Пробуем извлечь токен из обоих заголовков
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (authTokenHeader != null) {
            token = authTokenHeader;
        }

        if (token != null) {
            // Временное решение: убираем пробелы и обрабатываем двойной Bearer
            token = token.trim().replaceAll("\\s+", "");
            if (token.startsWith("Bearer")) {
                token = token.replaceFirst("Bearer", "").trim();
            }
            log.info("Processed token after cleaning: '{}'", token);

            if (jwtTokenProvider.validateToken(token)) {
                log.info("Extracted token: '{}'", token);
                String username = jwtTokenProvider.getUsernameFromToken(token);
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(username);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("Token is valid. Authenticated username: {}", username);
            } else {
                log.warn("Token validation failed for token: '{}'", token);
            }
        } else {
            log.warn("Invalid or missing token in the headers. Authorization: '{}', auth-token: '{}'", authHeader, authTokenHeader);
        }

        filterChain.doFilter(request, response);
    }
}
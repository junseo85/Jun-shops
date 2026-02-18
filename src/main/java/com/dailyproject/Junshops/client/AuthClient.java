package com.dailyproject.Junshops.client;

import com.dailyproject.Junshops.request.LoginRequest;
import com.dailyproject.Junshops.response.ApiResponse;
import com.dailyproject.Junshops.response.JwtResponse;
import com.fasterxml.jackson.databind.ObjectMapper;  // ✅ Changed from tools.jackson
import com.vaadin.flow.server.VaadinSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SESSION_TOKEN_KEY = "jwt_token";
    private static final String SESSION_USER_ID_KEY = "user_id";

    /**
     * Authenticate user and store JWT token in session
     */
    public JwtResponse login(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        try {
            log.info("Attempting login for user: {}", email);

            ApiResponse response = webClient.post()
                    .uri("/auth/login")
                    .body(Mono.just(request), LoginRequest.class)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            if (response != null && response.getData() != null) {
                JwtResponse jwtResponse = convertToJwtResponse(response.getData());

                if (jwtResponse != null && jwtResponse.getToken() != null) {
                    VaadinSession session = VaadinSession.getCurrent();
                    if (session != null) {
                        session.setAttribute(SESSION_TOKEN_KEY, jwtResponse.getToken());
                        session.setAttribute(SESSION_USER_ID_KEY, jwtResponse.getId());
                        log.info("Login successful for user: {}", email);
                    }
                    return jwtResponse;
                }
            }

            throw new RuntimeException("Login failed: Invalid response");
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", email, e);
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get JWT token from current session
     */
    public String getToken() {
        VaadinSession session = VaadinSession.getCurrent();
        return session != null ? (String) session.getAttribute(SESSION_TOKEN_KEY) : null;
    }

    /**
     * Get user ID from current session
     */
    public Long getUserId() {
        VaadinSession session = VaadinSession.getCurrent();
        return session != null ? (Long) session.getAttribute(SESSION_USER_ID_KEY) : null;
    }

    /**
     * Clear authentication
     */
    public void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(SESSION_TOKEN_KEY, null);
            session.setAttribute(SESSION_USER_ID_KEY, null);
            log.info("User logged out");
        }
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return getToken() != null;
    }

    /**
     * Helper method to convert API response data to JwtResponse
     */
    private JwtResponse convertToJwtResponse(Object data) {
        try {
            if (data instanceof LinkedHashMap) {
                return objectMapper.convertValue(data, JwtResponse.class);
            } else if (data instanceof JwtResponse) {
                return (JwtResponse) data;
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to convert response to JwtResponse", e);
            return null;
        }
    }
}
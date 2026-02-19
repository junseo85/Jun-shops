package com.dailyproject.Junshops.security.config;

import com.dailyproject.Junshops.security.jwt.AuthTokenFilter;
import com.dailyproject.Junshops.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    private final AuthTokenFilter authTokenFilter;

    /**
     * REST API Security - JWT based, stateless
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/products/**").permitAll()
                        .requestMatchers("/api/v1/categories/**").permitAll()
                        .requestMatchers("/api/v1/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Vaadin UI Security - Using Vaadin's built-in configuration
     */

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Allow register route
        http.authorizeHttpRequests(auth ->
                auth.requestMatchers("/register", "/register/**").permitAll()
        );

        // Call parent to set up Vaadin security
        super.configure(http);

        // Set login view
        setLoginView(http, LoginView.class);

        System.out.println("🔐 Using VaadinWebSecurity");
    }
}
package com.dailyproject.Junshops.controller;

import com.dailyproject.Junshops.request.LoginRequest;
import com.dailyproject.Junshops.response.ApiResponse;
import com.dailyproject.Junshops.response.JwtResponse;
import com.dailyproject.Junshops.security.jwt.JwtUtils;
import com.dailyproject.Junshops.security.user.ShopUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints.
 *
 * <p>Currently supports login and issues a JWT on success.</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Authenticates a user and returns a signed JWT token when credentials are valid.
     *
     * @param request login request containing email and password
     * @return {@link JwtResponse} wrapped in {@link ApiResponse} on success; 401 otherwise
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request){
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Store authentication in SecurityContext for downstream components (filters/controllers).
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Token carries subject (email) and claims (id/roles) as configured in JwtUtils.
            String jwt = jwtUtils.generateTokenForUser(authentication);

            ShopUserDetails userDetails = (ShopUserDetails) authentication.getPrincipal();
            JwtResponse jwtResponse = new JwtResponse(userDetails.getId(), jwt);

            return ResponseEntity.ok(new ApiResponse("Login successful", jwtResponse));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }
}

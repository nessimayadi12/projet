package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.auth.LoginRequest;
import com.banque.abc.tpe.dto.auth.LoginResponse;
import com.banque.abc.tpe.dto.auth.RegisterRequest;
import com.banque.abc.tpe.security.UserPrincipal;
import com.banque.abc.tpe.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    public static final String ACCESS_TOKEN_COOKIE = "TPE_ACCESS_TOKEN";

    private final AuthService authService;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${security.cookie.secure:false}")
    private boolean forceSecureCookie;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        LoginResponse response = authService.login(loginRequest);
        String jwt = response.getToken();

        response.setToken(null);
        response.setType(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildAccessTokenCookie(jwt, request).toString())
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Set<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        LoginResponse response = new LoginResponse(
                null,
                principal.getId(),
                principal.getUsername(),
                principal.getEmail(),
                roles
        );
        response.setType(null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expireAccessTokenCookie(request).toString())
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Utilisateur cree avec succes");
    }

    private ResponseCookie buildAccessTokenCookie(String jwt, HttpServletRequest request) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, jwt)
                .httpOnly(true)
                .secure(isSecureRequest(request))
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMillis(jwtExpirationMs))
                .build();
    }

    private ResponseCookie expireAccessTokenCookie(HttpServletRequest request) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(isSecureRequest(request))
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }

    private boolean isSecureRequest(HttpServletRequest request) {
        return forceSecureCookie || request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
    }
}

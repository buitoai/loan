package com.loan.decision.controller;

import com.loan.decision.model.dto.LoginRequest;
import com.loan.decision.model.dto.LoginResponse;
import com.loan.decision.model.entity.User;
import com.loan.decision.repository.UserRepository;
import com.loan.decision.security.JwtTokenProvider;
import com.loan.decision.security.LoginAttemptService;
import com.loan.decision.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST controller for authentication endpoints.
 *
 * <p>Provides:
 * <ul>
 *   <li>{@code POST /api/auth/login} — authenticate with username/password and receive a JWT token.</li>
 *   <li>{@code GET /api/auth/me} — retrieve the currently authenticated user's profile.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;
    private final AuditService auditService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          UserRepository userRepository,
                          LoginAttemptService loginAttemptService,
                          AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.loginAttemptService = loginAttemptService;
        this.auditService = auditService;
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * <p>If the account is temporarily locked due to too many failed attempts, an
     * {@link AuthenticationException} is thrown immediately (before attempting authentication).
     * On a successful login the failure counter is cleared and an audit record is written.
     * On a failed login the failure counter is incremented and an audit record is written.
     *
     * @param loginRequest the credentials supplied by the client
     * @param request      the HTTP request (used to capture the client IP address)
     * @return a {@link LoginResponse} containing the JWT token and user metadata
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                               HttpServletRequest request) {
        String username = loginRequest.getUsername();
        String ipAddress = request.getRemoteAddr();

        if (loginAttemptService.isLocked(username)) {
            auditService.logLogin(username, false, ipAddress);
            throw new org.springframework.security.authentication.LockedException(
                    "Account is temporarily locked due to too many failed login attempts");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            loginAttemptService.recordSuccessfulLogin(username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                            "User not found: " + username));

            String token = jwtTokenProvider.generateToken(userDetails);

            auditService.logLogin(username, true, ipAddress);

            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(28800)
                    .role(user.getRole().name())
                    .username(username)
                    .build();

            return ResponseEntity.ok(response);

        } catch (AuthenticationException ex) {
            loginAttemptService.recordFailedAttempt(username);
            auditService.logLogin(username, false, ipAddress);
            throw ex;
        }
    }

    /**
     * Returns the profile of the currently authenticated user.
     *
     * <p>The username is resolved from the {@link SecurityContextHolder} and the full
     * user record is loaded from the database.
     *
     * @return a map containing {@code userId}, {@code username}, {@code email},
     *         {@code fullName}, {@code role}, and {@code status}
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "User not found: " + authentication.getName()));

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("role", user.getRole().name());
        userInfo.put("status", user.getStatus().name());

        return ResponseEntity.ok(userInfo);
    }
}

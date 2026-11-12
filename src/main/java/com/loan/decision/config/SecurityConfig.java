package com.loan.decision.config;

import com.loan.decision.security.JwtAuthenticationFilter;
import com.loan.decision.security.JwtTokenProvider;
import com.loan.decision.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration for the Loan Decision System.
 *
 * <p>Configures a stateless, JWT-based security setup:
 * <ul>
 *   <li>CSRF disabled — REST API with stateless sessions does not need it.</li>
 *   <li>{@code POST /api/auth/login} is publicly accessible (no authentication required).</li>
 *   <li>All other {@code /api/**} endpoints require a valid JWT token.</li>
 *   <li>Session management is set to {@link SessionCreationPolicy#STATELESS}.</li>
 *   <li>{@link JwtAuthenticationFilter} runs before {@link UsernamePasswordAuthenticationFilter}.</li>
 * </ul>
 *
 * <p>Method-level security is enabled via {@code @EnableMethodSecurity}, allowing
 * {@code @PreAuthorize} / {@code @PostAuthorize} annotations on service and controller methods.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider,
                          UserDetailsServiceImpl userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Creates the {@link JwtAuthenticationFilter} bean, wiring it with the
     * {@link JwtTokenProvider} and {@link UserDetailsServiceImpl}.
     *
     * @return a configured {@link JwtAuthenticationFilter}
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    /**
     * Defines the main {@link SecurityFilterChain}:
     * <ul>
     *   <li>CORS enabled — delegates to {@link #corsConfigurationSource()}.</li>
     *   <li>CSRF protection disabled (stateless REST API).</li>
     *   <li>{@code POST /api/auth/login} — permitted without authentication.</li>
     *   <li>All other {@code /api/**} requests — require authentication.</li>
     *   <li>Session policy — {@link SessionCreationPolicy#STATELESS}.</li>
     *   <li>{@link JwtAuthenticationFilter} inserted before
     *       {@link UsernamePasswordAuthenticationFilter}.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS to allow the React frontend (running on {@code http://localhost:3000})
     * to communicate with this API.
     *
     * <p>Permitted:
     * <ul>
     *   <li>Origin: {@code http://localhost:3000}</li>
     *   <li>Methods: GET, POST, PUT, DELETE, OPTIONS</li>
     *   <li>Headers: Authorization, Content-Type</li>
     *   <li>Credentials: allowed (required for Authorization header)</li>
     * </ul>
     *
     * @return a {@link CorsConfigurationSource} applied to all {@code /**} paths
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Exposes the {@link AuthenticationManager} bean so it can be injected into
     * {@code AuthController} for programmatic authentication during login.
     *
     * @param authenticationConfiguration Spring's auto-configured {@link AuthenticationConfiguration}
     * @return the application-wide {@link AuthenticationManager}
     * @throws Exception if the manager cannot be obtained
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Defines a {@link DaoAuthenticationProvider} that uses {@link UserDetailsServiceImpl}
     * to load users and {@link BCryptPasswordEncoder} to verify passwords.
     *
     * @return a configured {@link AuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Provides a {@link BCryptPasswordEncoder} bean used for hashing and verifying passwords.
     *
     * @return a {@link BCryptPasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

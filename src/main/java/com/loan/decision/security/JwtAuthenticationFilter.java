package com.loan.decision.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter that intercepts every HTTP request exactly once.
 *
 * <p>Extracts the Bearer token from the {@code Authorization} header, validates it
 * using {@link JwtTokenProvider}, loads the corresponding {@link UserDetails} and
 * sets the {@link org.springframework.security.core.Authentication} in the
 * {@link SecurityContextHolder} so that downstream security checks can proceed.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Core filter logic executed once per request.
     *
     * <ol>
     *   <li>Extracts the Bearer token from the {@code Authorization} header.</li>
     *   <li>Validates the token via {@link JwtTokenProvider#validateToken(String)}.</li>
     *   <li>Loads {@link UserDetails} for the username embedded in the token.</li>
     *   <li>Sets a {@link UsernamePasswordAuthenticationToken} in the
     *       {@link SecurityContextHolder} when all checks pass.</li>
     * </ol>
     *
     * <p>Any exception during processing is caught and logged; the filter chain
     * continues regardless so that Spring Security can return the appropriate
     * HTTP 401 response.</p>
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = extractBearerToken(request);

            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromToken(token);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Set authentication for user '{}' in SecurityContext", username);
            }
        } catch (Exception ex) {
            // Log the error but do not propagate — Spring Security will handle the 401 response.
            logger.error("Could not set user authentication in security context: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw JWT string from the {@code Authorization: Bearer <token>} header.
     *
     * @param request the incoming HTTP request
     * @return the token string, or {@code null} if the header is absent or not a Bearer token
     */
    private String extractBearerToken(HttpServletRequest request) {
        String headerValue = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(headerValue) && headerValue.startsWith(BEARER_PREFIX)) {
            return headerValue.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}

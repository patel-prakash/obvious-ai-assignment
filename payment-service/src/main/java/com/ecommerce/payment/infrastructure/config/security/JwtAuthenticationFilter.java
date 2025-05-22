package com.ecommerce.payment.infrastructure.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Filter to authenticate users based on JWT tokens.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    /**
     * Creates a new JWT authentication filter.
     *
     * @param jwtDecoder The JWT decoder to use for validating tokens
     */
    public JwtAuthenticationFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = new JwtAuthenticationConverter();
        this.jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtRoleConverter());
    }

    /**
     * Processes the request, extracts and validates the JWT token, and sets authentication in the security context.
     *
     * @param request     The incoming HTTP request
     * @param response    The HTTP response
     * @param filterChain The filter chain for processing the request
     * @throws ServletException If a servlet-related error occurs
     * @throws IOException      If an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Jwt jwt = jwtDecoder.decode(token);

                AbstractAuthenticationToken authentication =
                        (AbstractAuthenticationToken) jwtAuthenticationConverter.convert(jwt);

                if (authentication != null) {
                    // Log roles for debugging
                    String authorities = authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(", "));

                    logger.debug("User roles: " + authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                logger.error("JWT Authentication failed", e);
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Converter to transform JWT claims into Spring Security GrantedAuthorities.
     */
    static class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            return extractAuthorities(jwt);
        }

        private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
            Map<String, Object> claims = jwt.getClaims();

            List<String> roles = extractRoles(claims);

            // Map each role string to a SimpleGrantedAuthority with ROLE_ prefix
            return roles.stream()
                    .map(role -> (GrantedAuthority) () -> "ROLE_" + role.toUpperCase())
                    .collect(Collectors.toList());
        }

        @SuppressWarnings("unchecked")
        private List<String> extractRoles(Map<String, Object> claims) {
            try {
                // First try standard Auth0 permissions claim
                if (claims.containsKey("permissions")) {
                    return (List<String>) claims.get("permissions");
                }

                // Then try Auth0 namespaced claims
                for (String key : claims.keySet()) {
                    if (key.endsWith("/roles") || key.endsWith("/permissions")) {
                        return (List<String>) claims.get(key);
                    }
                }

                // Return empty list if no roles found
                return List.of();
            } catch (Exception e) {
                return List.of();
            }
        }
    }
} 
package com.ecommerce.inventory.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration for Spring Security.
 * It sets up the security filter chain and defines the authorization rules.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain with proper authorization rules.
     *
     * <p>This method:
     * <ul>
     *   <li>Configures API endpoints authorization</li>
     *   <li>Configures stateless session management</li>
     *   <li>Disables CSRF for API requests</li>
     *   <li>Adds the JWT filter for authentication</li>
     * </ul>
     *
     * @param http       The HttpSecurity to configure
     * @param jwtDecoder The JWT decoder bean provided by the application context
     * @return The configured SecurityFilterChain
     * @throws Exception If configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtDecoder);

        http
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints
                        .requestMatchers("/actuator/**", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
                // Stateless session management for REST API
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Disable CSRF for REST API
                .csrf(AbstractHttpConfigurer::disable)
                // Add JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
} 
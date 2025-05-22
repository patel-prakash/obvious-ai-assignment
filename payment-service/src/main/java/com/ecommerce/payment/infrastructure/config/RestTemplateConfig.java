package com.ecommerce.payment.infrastructure.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for REST clients.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a RestTemplate for general use.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(3000))
                .setReadTimeout(Duration.ofMillis(5000))
                .build();
    }
} 
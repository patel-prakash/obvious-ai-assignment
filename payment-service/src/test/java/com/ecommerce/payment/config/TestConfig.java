package com.ecommerce.payment.config;

import org.apache.kafka.clients.producer.Producer;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test configuration for Spring unit and integration tests.
 * This configuration provides mock beans that are necessary for testing,
 * especially focusing on security and external service dependencies.
 */
@TestConfiguration
public class TestConfig {

    /**
     * Creates a mock JwtDecoder bean for testing.
     * This prevents the need for a real JWT token during tests.
     *
     * @return A mocked JwtDecoder
     */
    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return Mockito.mock(JwtDecoder.class);
    }

    /**
     * Creates a mock KafkaTemplate for testing.
     * This allows tests to run without an actual Kafka instance.
     *
     * @return A mocked KafkaTemplate
     */
    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate mockTemplate = Mockito.mock(KafkaTemplate.class);
        // Configure the mock to return a pre-configured future to avoid actual Kafka calls
        when(mockTemplate.send(anyString(), any())).thenReturn(null);
        return mockTemplate;
    }

    /**
     * Creates a mock Producer for testing.
     * This prevents any attempt to connect to a real Kafka broker.
     *
     * @return A mocked Kafka Producer
     */
    @Bean
    @Primary
    public ProducerFactory<String, Object> producerFactory() {
        ProducerFactory mockFactory = Mockito.mock(ProducerFactory.class);
        Producer mockProducer = Mockito.mock(Producer.class);
        when(mockFactory.createProducer()).thenReturn(mockProducer);
        return mockFactory;
    }
} 
package com.driftstay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Performance configuration for JPA/Hibernate.
 * Enables batch fetching, read-only optimizations, and proper transaction management.
 *
 * For production, add these to application.yml:
 *   spring.jpa.properties.hibernate.jdbc.batch_size: 25
 *   spring.jpa.properties.hibernate.order_inserts: true
 *   spring.jpa.properties.hibernate.order_updates: true
 *   spring.jpa.properties.hibernate.batch_versioned_data: true
 *   spring.jpa.properties.hibernate.jdbc.fetch_size: 200
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.driftstay")
@EnableTransactionManagement
public class PerformanceConfig {

    // Batch fetching and other optimizations are configured via application.yml
    // This class enables the repository scanning and transaction management
}

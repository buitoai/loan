package com.loan.decision.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async execution configuration for the Loan Decision System.
 *
 * <p>Enables Spring's annotation-driven async processing via {@code @EnableAsync} and
 * defines two dedicated thread pools:
 * <ul>
 *   <li>{@code auditExecutor} — a small pool for non-blocking audit log writes.</li>
 *   <li>{@code taskExecutor} — a larger general-purpose pool for other async tasks.</li>
 * </ul>
 *
 * <p>Async methods can target a specific executor by name, e.g.
 * {@code @Async("auditExecutor")}.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    /**
     * Thread pool dedicated to audit logging.
     *
     * <ul>
     *   <li>Core pool size: 2</li>
     *   <li>Max pool size: 5</li>
     *   <li>Queue capacity: 100</li>
     *   <li>Thread name prefix: {@code audit-}</li>
     * </ul>
     *
     * @return a configured {@link ThreadPoolTaskExecutor} for audit tasks
     */
    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("audit-");
        executor.initialize();
        return executor;
    }

    /**
     * General-purpose thread pool for async tasks.
     *
     * <ul>
     *   <li>Core pool size: 4</li>
     *   <li>Max pool size: 10</li>
     *   <li>Queue capacity: 200</li>
     *   <li>Thread name prefix: {@code async-}</li>
     * </ul>
     *
     * @return a configured {@link ThreadPoolTaskExecutor} for general async tasks
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

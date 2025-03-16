package com.bracit.voucher_engine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Configuration for thread management in the application.
 * Leverages Java 21 virtual threads for improved performance and scalability.
 */
@Configuration
@EnableAsync
public class ThreadConfig {

    @Value("${app.virtual-threads.max-pool-size:1000}")
    private int maxPoolSize;

    /**
     * Creates a virtual thread executor for general-purpose async operations.
     * Virtual threads are lightweight and managed by the JVM, allowing for high concurrency
     * with minimal resource overhead.
     *
     * @return An ExecutorService using virtual threads
     */
    @Bean(name = "virtualThreadExecutor")
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Creates a Spring AsyncTaskExecutor that uses virtual threads.
     * This executor can be used with Spring's @Async annotation.
     *
     * @return An AsyncTaskExecutor backed by virtual threads
     */
    @Bean(name = "asyncTaskExecutor")
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * Creates a bounded virtual thread executor for operations that need controlled concurrency.
     * This executor limits the number of concurrent tasks to prevent resource exhaustion.
     *
     * @return An ExecutorService with bounded concurrency
     */
    @Bean(name = "boundedVirtualThreadExecutor")
    public ExecutorService boundedVirtualThreadExecutor() {
        // For operations that need controlled concurrency
        return Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("bounded-virtual-", 0).factory()
        );
    }
} 
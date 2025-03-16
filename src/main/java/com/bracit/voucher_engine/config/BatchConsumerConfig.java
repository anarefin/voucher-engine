package com.bracit.voucher_engine.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.amqp.rabbit.batch.BatchingStrategy;
import org.springframework.amqp.rabbit.batch.SimpleBatchingStrategy;

import java.util.concurrent.Executors;

/**
 * Configuration for batch processing of RabbitMQ messages
 */
@Configuration
public class BatchConsumerConfig {

    @Value("${spring.rabbitmq.template.batch-size:100}")
    private int batchSize;
    
    @Value("${spring.rabbitmq.template.receive-timeout:30000}")
    private int receiveTimeout;
    
    /**
     * Creates a batching RabbitMQ template for efficient message publishing
     * @param connectionFactory The RabbitMQ connection factory
     * @return A configured BatchingRabbitTemplate
     */
    @Bean
    public BatchingRabbitTemplate batchingRabbitTemplate(ConnectionFactory connectionFactory) {
        BatchingStrategy batchingStrategy = new SimpleBatchingStrategy(
                batchSize,  // max messages per batch
                1024 * 1024,  // max bytes per batch (1MB)
                receiveTimeout  // timeout for incomplete batches
        );
        
        TaskScheduler taskScheduler = new ConcurrentTaskScheduler(
                Executors.newSingleThreadScheduledExecutor()
        );
        
        BatchingRabbitTemplate template = new BatchingRabbitTemplate(
                batchingStrategy, taskScheduler
        );
        template.setConnectionFactory(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        
        return template;
    }
    
    /**
     * Configures a RabbitMQ listener container factory with batch capabilities
     * @param connectionFactory The RabbitMQ connection factory
     * @return A configured SimpleRabbitListenerContainerFactory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory batchListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setBatchListener(true);  // Enable batch message reception
        factory.setBatchSize(batchSize);
        factory.setConsumerBatchEnabled(true);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        
        return factory;
    }
} 
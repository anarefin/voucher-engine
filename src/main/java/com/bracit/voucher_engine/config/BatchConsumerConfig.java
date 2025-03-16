package com.bracit.voucher_engine.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.amqp.rabbit.batch.BatchingStrategy;
import org.springframework.amqp.rabbit.batch.SimpleBatchingStrategy;

import java.util.concurrent.Executors;

/**
 * Configuration for batch message consumption from RabbitMQ
 * Uses virtual threads for improved performance and scalability
 */
@Configuration
public class BatchConsumerConfig {

    @Value("${app.batch-consumer.batch-size:100}")
    private int batchSize;
    
    @Value("${app.batch-consumer.receive-timeout:1000}")
    private long receiveTimeout;
    
    @Autowired
    @Qualifier("asyncTaskExecutor")
    private AsyncTaskExecutor taskExecutor;
    
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
     * Creates a RabbitMQ listener container factory configured for batch processing
     * Uses virtual threads for improved concurrency and performance
     * 
     * @param connectionFactory The RabbitMQ connection factory
     * @return A configured SimpleRabbitListenerContainerFactory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory batchListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        
        // Configure batch processing
        factory.setBatchListener(true);
        factory.setBatchSize(batchSize);
        factory.setConsumerBatchEnabled(true);
        factory.setReceiveTimeout(receiveTimeout);
        
        // Use virtual threads for message processing
        factory.setTaskExecutor(taskExecutor);
        
        // Configure acknowledgment mode
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        
        // Configure prefetch count (how many messages to fetch at once)
        factory.setPrefetchCount(batchSize * 2);
        
        return factory;
    }
} 
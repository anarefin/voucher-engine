package com.bracit.voucher_engine.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String VOUCHER_QUEUE = "voucher.queue";
    public static final String DEBIT_VOUCHER_QUEUE = "debit.voucher.queue";
    
    // Exchange names
    public static final String VOUCHER_EXCHANGE = "voucher.exchange";
    public static final String DEBIT_VOUCHER_EXCHANGE = "debit.voucher.exchange";
    
    // Routing keys
    public static final String VOUCHER_ROUTING_KEY = "voucher.routingkey";
    public static final String DEBIT_VOUCHER_ROUTING_KEY = "debit.voucher.routingkey";

    @Bean
    public Queue voucherQueue() {
        return new Queue(VOUCHER_QUEUE, true);
    }

    @Bean
    public Queue debitVoucherQueue() {
        return new Queue(DEBIT_VOUCHER_QUEUE, true);
    }

    @Bean
    public TopicExchange voucherExchange() {
        return new TopicExchange(VOUCHER_EXCHANGE);
    }

    @Bean
    public TopicExchange debitVoucherExchange() {
        return new TopicExchange(DEBIT_VOUCHER_EXCHANGE);
    }

    @Bean
    public Binding voucherBinding(Queue voucherQueue, TopicExchange voucherExchange) {
        return BindingBuilder
                .bind(voucherQueue)
                .to(voucherExchange)
                .with(VOUCHER_ROUTING_KEY);
    }

    @Bean
    public Binding debitVoucherBinding(Queue debitVoucherQueue, TopicExchange debitVoucherExchange) {
        return BindingBuilder
                .bind(debitVoucherQueue)
                .to(debitVoucherExchange)
                .with(DEBIT_VOUCHER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
} 
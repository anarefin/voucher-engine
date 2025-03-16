package com.bracit.voucher_engine.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bracit.voucher_engine.config.RabbitMQConfig;

@Service
public class RabbitMQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(Object message) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.VOUCHER_EXCHANGE, 
            RabbitMQConfig.VOUCHER_ROUTING_KEY, 
            message
        );
        System.out.println("Message sent: " + message);
    }
} 
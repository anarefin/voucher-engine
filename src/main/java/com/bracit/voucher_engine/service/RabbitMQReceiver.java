package com.bracit.voucher_engine.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.bracit.voucher_engine.config.RabbitMQConfig;

@Service
public class RabbitMQReceiver {

    @RabbitListener(queues = RabbitMQConfig.VOUCHER_QUEUE)
    public void receiveMessage(Object message) {
        System.out.println("Received message: " + message);
        // Process the message here
    }
} 
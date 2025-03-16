package com.bracit.voucher_engine.service;

import com.bracit.voucher_engine.config.RabbitMQConfig;
import com.bracit.voucher_engine.dto.DebitVoucherDto;
import com.bracit.voucher_engine.dto.DebitVoucherMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DebitVoucherProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Sends a debit voucher message to RabbitMQ
     * @param debitVoucherDto The debit voucher to send
     */
    public void sendDebitVoucher(DebitVoucherDto debitVoucherDto) {
        // Convert DTO to message
        DebitVoucherMessage message = DebitVoucherMessage.builder()
                .voucherNumber(debitVoucherDto.getVoucherNumber())
                .voucherDate(debitVoucherDto.getVoucherDate())
                .amount(debitVoucherDto.getAmount())
                .description(debitVoucherDto.getDescription())
                .createdBy(debitVoucherDto.getCreatedBy())
                .createdDate(debitVoucherDto.getCreatedDate())
                .status(debitVoucherDto.getStatus())
                .debitAccount(debitVoucherDto.getDebitAccount())
                .creditAccount(debitVoucherDto.getCreditAccount())
                .build();

        // Send message to RabbitMQ
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DEBIT_VOUCHER_EXCHANGE,
                RabbitMQConfig.DEBIT_VOUCHER_ROUTING_KEY,
                message
        );
        
        System.out.println("Debit voucher sent to RabbitMQ: " + message.getVoucherNumber());
    }
} 
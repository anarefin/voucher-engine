package com.bracit.voucher_engine.service;

import com.bracit.voucher_engine.config.RabbitMQConfig;
import com.bracit.voucher_engine.dto.DebitVoucherDto;
import com.bracit.voucher_engine.dto.DebitVoucherMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DebitVoucherConsumer {

    @Autowired
    private DebitVoucherService debitVoucherService;

    /**
     * Consumes debit voucher messages from RabbitMQ and stores them in the database
     * @param message The debit voucher message received from RabbitMQ
     */
    @RabbitListener(queues = RabbitMQConfig.DEBIT_VOUCHER_QUEUE)
    public void receiveDebitVoucher(DebitVoucherMessage message) {
        System.out.println("Received debit voucher from RabbitMQ: " + message.getVoucherNumber());
        
        try {
            // Convert message to DTO
            DebitVoucherDto debitVoucherDto = DebitVoucherDto.builder()
                    .voucherNumber(message.getVoucherNumber())
                    .voucherDate(message.getVoucherDate())
                    .amount(message.getAmount())
                    .description(message.getDescription())
                    .createdBy(message.getCreatedBy())
                    .createdDate(message.getCreatedDate())
                    .status(message.getStatus())
                    .debitAccount(message.getDebitAccount())
                    .creditAccount(message.getCreditAccount())
                    .build();
            
            // Save to database using the service
            DebitVoucherDto savedVoucher = debitVoucherService.createVoucher(debitVoucherDto);
            System.out.println("Debit voucher saved to database: " + savedVoucher.getVoucherNumber());
        } catch (Exception e) {
            System.err.println("Error processing debit voucher message: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
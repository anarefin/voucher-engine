package com.bracit.voucher_engine.service;

import com.bracit.voucher_engine.config.RabbitMQConfig;
import com.bracit.voucher_engine.dto.DebitVoucherDto;
import com.bracit.voucher_engine.dto.DebitVoucherMessage;
import com.bracit.voucher_engine.model.VoucherStatus;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for generating and publishing bulk debit vouchers to RabbitMQ
 */
@Service
public class BulkDebitVoucherProducer {

    @Autowired
    private DebitVoucherProducer debitVoucherProducer;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired(required = false)
    @Qualifier("batchingRabbitTemplate")
    private BatchingRabbitTemplate batchingRabbitTemplate;
    
    @Value("${spring.rabbitmq.template.batch-size:100}")
    private int rabbitBatchSize;
    
    @Value("${app.bulk-generation.use-batching:true}")
    private boolean useBatching;

    private final Random random = new Random();
    private final String[] debitAccounts = {
            "1001", "1002", "1003", "1004", "1005",
            "1006", "1007", "1008", "1009", "1010"
    };
    private final String[] creditAccounts = {
            "2001", "2002", "2003", "2004", "2005",
            "2006", "2007", "2008", "2009", "2010"
    };
    private final String[] descriptions = {
            "Salary payment", "Vendor payment", "Utility bill", "Office supplies",
            "Rent payment", "Insurance premium", "Maintenance cost", "Travel expense",
            "Training cost", "Miscellaneous expense"
    };

    /**
     * Generate and publish a specified number of debit vouchers to RabbitMQ
     * @param count Number of vouchers to generate and publish
     * @param batchSize Size of each batch for processing
     * @param threadCount Number of threads to use for parallel processing
     * @return The number of vouchers successfully published
     */
    public long generateAndPublishBulkVouchers(long count, int batchSize, int threadCount) {
        System.out.println("Starting bulk voucher generation: " + count + " vouchers");
        System.out.println("Using batching mode: " + (useBatching && batchingRabbitTemplate != null));
        
        AtomicLong successCount = new AtomicLong(0);
        AtomicLong processedCount = new AtomicLong(0);
        LocalDate today = LocalDate.now();
        
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        
        for (long i = 0; i < count; i += batchSize) {
            final long batchStart = i;
            final long batchEnd = Math.min(i + batchSize, count);
            
            executorService.submit(() -> {
                try {
                    // Use batch publishing for better performance
                    List<DebitVoucherDto> batchVouchers = new ArrayList<>();
                    
                    for (long j = batchStart; j < batchEnd; j++) {
                        try {
                            DebitVoucherDto voucher = generateRandomDebitVoucher(j, today);
                            batchVouchers.add(voucher);
                        } catch (Exception e) {
                            System.err.println("Error generating voucher: " + e.getMessage());
                        }
                    }
                    
                    // Publish the batch
                    long batchSuccessCount = publishVoucherBatch(batchVouchers);
                    successCount.addAndGet(batchSuccessCount);
                    
                    long processed = processedCount.addAndGet(batchEnd - batchStart);
                    if (processed % 10000 == 0 || processed == count) {
                        System.out.println("Processed " + processed + " vouchers out of " + count);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing batch: " + e.getMessage());
                }
            });
        }
        
        executorService.shutdown();
        try {
            // Wait for all tasks to complete with a timeout
            if (!executorService.awaitTermination(30, TimeUnit.MINUTES)) {
                System.err.println("Timeout occurred while waiting for voucher generation to complete");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Voucher generation was interrupted: " + e.getMessage());
            executorService.shutdownNow();
        }
        
        System.out.println("Bulk voucher generation completed. Successfully published: " + successCount.get());
        return successCount.get();
    }
    
    /**
     * Publish a batch of vouchers to RabbitMQ
     * @param vouchers List of vouchers to publish
     * @return Number of successfully published vouchers
     */
    private long publishVoucherBatch(List<DebitVoucherDto> vouchers) {
        long successCount = 0;
        
        // For smaller batches or when batching is not available, use individual publishing
        if (vouchers.size() <= 10 || !useBatching || batchingRabbitTemplate == null) {
            for (DebitVoucherDto voucher : vouchers) {
                try {
                    debitVoucherProducer.sendDebitVoucher(voucher);
                    successCount++;
                } catch (Exception e) {
                    System.err.println("Error publishing voucher: " + e.getMessage());
                }
            }
            return successCount;
        }
        
        // For larger batches, use optimized batch publishing
        try {
            List<DebitVoucherMessage> messages = new ArrayList<>(vouchers.size());
            
            // Convert all DTOs to messages
            for (DebitVoucherDto dto : vouchers) {
                DebitVoucherMessage message = DebitVoucherMessage.builder()
                        .voucherNumber(dto.getVoucherNumber())
                        .voucherDate(dto.getVoucherDate())
                        .amount(dto.getAmount())
                        .description(dto.getDescription())
                        .createdBy(dto.getCreatedBy())
                        .createdDate(dto.getCreatedDate())
                        .status(dto.getStatus())
                        .debitAccount(dto.getDebitAccount())
                        .creditAccount(dto.getCreditAccount())
                        .build();
                messages.add(message);
            }
            
            // Send messages in sub-batches using BatchingRabbitTemplate
            for (DebitVoucherMessage message : messages) {
                batchingRabbitTemplate.convertAndSend(
                        RabbitMQConfig.DEBIT_VOUCHER_EXCHANGE,
                        RabbitMQConfig.DEBIT_VOUCHER_ROUTING_KEY,
                        message
                );
            }
            
            // All messages are considered successful when using batching
            successCount = messages.size();
            
        } catch (Exception e) {
            System.err.println("Error in batch publishing: " + e.getMessage());
            
            // Fallback to individual publishing if batching fails
            System.out.println("Falling back to individual publishing...");
            for (DebitVoucherDto voucher : vouchers) {
                try {
                    debitVoucherProducer.sendDebitVoucher(voucher);
                    successCount++;
                } catch (Exception ex) {
                    System.err.println("Error publishing voucher: " + ex.getMessage());
                }
            }
        }
        
        return successCount;
    }
    
    /**
     * Generate a random debit voucher
     * @param index The index of the voucher (used for voucher number generation)
     * @param voucherDate The date for the voucher
     * @return A randomly generated DebitVoucherDto
     */
    private DebitVoucherDto generateRandomDebitVoucher(long index, LocalDate voucherDate) {
        String voucherNumber = "DV" + String.format("%010d", index);
        
        BigDecimal amount = BigDecimal.valueOf(random.nextInt(1000000) / 100.0);
        String description = descriptions[random.nextInt(descriptions.length)];
        String debitAccount = debitAccounts[random.nextInt(debitAccounts.length)];
        String creditAccount = creditAccounts[random.nextInt(creditAccounts.length)];
        
        return DebitVoucherDto.builder()
                .voucherNumber(voucherNumber)
                .voucherDate(voucherDate)
                .amount(amount)
                .description(description)
                .createdBy("SYSTEM")
                .createdDate(voucherDate)
                .status(VoucherStatus.PENDING_APPROVAL)
                .debitAccount(debitAccount)
                .creditAccount(creditAccount)
                .build();
    }
} 
package com.bracit.voucher_engine.service;

import com.bracit.voucher_engine.config.RabbitMQConfig;
import com.bracit.voucher_engine.dto.DebitVoucherDto;
import com.bracit.voucher_engine.dto.DebitVoucherMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service for consuming and processing bulk debit vouchers from RabbitMQ
 * Uses virtual threads for improved performance and scalability
 */
@Service
public class BulkDebitVoucherConsumer {

    @Autowired
    private DebitVoucherService debitVoucherService;
    
    @Autowired
    @Qualifier("virtualThreadExecutor")
    private ExecutorService virtualThreadExecutor;
    
    @Value("${app.bulk-consumption.batch-size:100}")
    private int batchSize;
    
    @Value("${app.bulk-consumption.report-interval:1000}")
    private int reportInterval;
    
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    private final ConcurrentHashMap<String, String> failedVouchers = new ConcurrentHashMap<>();
    
    /**
     * Consumes batches of debit voucher messages from RabbitMQ
     * @param messages The batch of debit voucher messages received from RabbitMQ
     */
    @RabbitListener(queues = RabbitMQConfig.DEBIT_VOUCHER_QUEUE, 
                   containerFactory = "batchListenerContainerFactory",
                   concurrency = "${app.bulk-consumption.listener-concurrency:4}")
    public void receiveBatchDebitVouchers(List<DebitVoucherMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        
        System.out.println("Received batch of " + messages.size() + " debit vouchers");
        
        // Process the batch
        processBatch(messages);
    }
    
    /**
     * Process a batch of debit voucher messages using virtual threads
     * @param messages List of debit voucher messages to process
     */
    public void processBatch(List<DebitVoucherMessage> messages) {
        System.out.println("Processing batch of " + messages.size() + " debit vouchers");
        
        // Reset counters for this batch
        processedCount.set(0);
        successCount.set(0);
        failureCount.set(0);
        failedVouchers.clear();
        
        // Convert messages to DTOs
        List<DebitVoucherDto> dtos = messages.stream()
                .map(this::convertMessageToDto)
                .collect(Collectors.toList());
        
        // Process in smaller batches for better throughput
        List<List<DebitVoucherDto>> batches = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i += batchSize) {
            batches.add(dtos.subList(i, Math.min(i + batchSize, dtos.size())));
        }
        
        // Process each batch with virtual threads
        batches.forEach(batch -> {
            virtualThreadExecutor.submit(() -> {
                try {
                    processBatchInBulk(batch);
                } catch (Exception e) {
                    System.err.println("Error processing batch: " + e.getMessage());
                    // Fall back to individual processing
                    processIndividually(batch, e);
                }
                
                // Report progress periodically
                if (processedCount.incrementAndGet() % reportInterval == 0) {
                    reportProgress();
                }
            });
        });
    }
    
    /**
     * Process a batch of vouchers using bulk insert
     * @param batchDtos List of voucher DTOs to process in bulk
     */
    private void processBatchInBulk(List<DebitVoucherDto> batchDtos) {
        try {
            // Save all vouchers in a single database operation
            List<DebitVoucherDto> savedVouchers = debitVoucherService.createVouchersBulk(batchDtos);
            
            // Update success count
            successCount.addAndGet(savedVouchers.size());
            
            // Log success
            System.out.println("Successfully processed " + savedVouchers.size() + 
                    " vouchers in bulk (Total success: " + successCount.get() + ")");
            
        } catch (Exception e) {
            // If bulk operation fails, try to process individually to identify problematic vouchers
            processIndividually(batchDtos, e);
        }
    }
    
    /**
     * Fallback method to process vouchers individually when bulk processing fails
     * @param batchDtos List of voucher DTOs to process individually
     * @param bulkException The exception that occurred during bulk processing
     */
    private void processIndividually(List<DebitVoucherDto> batchDtos, Exception bulkException) {
        System.err.println("Bulk processing failed with error: " + bulkException.getMessage() + 
                ". Falling back to individual processing for " + batchDtos.size() + " vouchers.");
        
        for (DebitVoucherDto dto : batchDtos) {
            try {
                debitVoucherService.createVoucher(dto);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
                failedVouchers.put(dto.getVoucherNumber(), e.getMessage());
                System.err.println("Error processing voucher " + dto.getVoucherNumber() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Convert a message to DTO
     * @param message The debit voucher message to convert
     * @return The converted DTO
     */
    private DebitVoucherDto convertMessageToDto(DebitVoucherMessage message) {
        return DebitVoucherDto.builder()
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
    }
    
    /**
     * Report the current processing progress
     */
    private void reportProgress() {
        long processed = processedCount.get();
        long success = successCount.get();
        long failure = failureCount.get();
        
        System.out.println("=== Bulk Voucher Processing Progress ===");
        System.out.println("Total processed: " + processed);
        System.out.println("Successful: " + success);
        System.out.println("Failed: " + failure);
        System.out.println("Success rate: " + (processed > 0 ? (success * 100.0 / processed) : 0) + "%");
        System.out.println("========================================");
        
        // If there are too many failures, log some of the failed vouchers
        if (failure > 0 && failure % 100 == 0) {
            System.err.println("Sample of failed vouchers:");
            failedVouchers.entrySet().stream()
                    .limit(5)
                    .forEach(entry -> System.err.println("  - " + entry.getKey() + ": " + entry.getValue()));
            
            if (failedVouchers.size() > 5) {
                System.err.println("  ... and " + (failedVouchers.size() - 5) + " more");
            }
        }
    }
    
    /**
     * Get the current processing statistics
     * @return A string containing the current processing statistics
     */
    public String getProcessingStats() {
        long processed = processedCount.get();
        long success = successCount.get();
        long failure = failureCount.get();
        
        return String.format(
                "Processed: %d, Successful: %d, Failed: %d, Success Rate: %.2f%%",
                processed, success, failure,
                (processed > 0 ? (success * 100.0 / processed) : 0)
        );
    }
    
    /**
     * Reset the processing statistics
     */
    public void resetStats() {
        processedCount.set(0);
        successCount.set(0);
        failureCount.set(0);
        failedVouchers.clear();
        System.out.println("Bulk debit voucher consumer statistics reset");
    }
} 
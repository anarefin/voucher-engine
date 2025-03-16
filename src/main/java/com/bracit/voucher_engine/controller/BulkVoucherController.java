package com.bracit.voucher_engine.controller;

import com.bracit.voucher_engine.service.BulkDebitVoucherConsumer;
import com.bracit.voucher_engine.service.BulkDebitVoucherProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bulk-vouchers")
public class BulkVoucherController {

    @Autowired
    private BulkDebitVoucherProducer bulkDebitVoucherProducer;
    
    @Autowired
    private BulkDebitVoucherConsumer bulkDebitVoucherConsumer;
    
    /**
     * Generate and publish a specified number of debit vouchers
     * @param count Number of vouchers to generate
     * @param batchSize Size of each batch for processing
     * @param threadCount Number of threads to use
     * @return Response with the number of vouchers published
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateVouchers(
            @RequestParam(defaultValue = "1000") long count,
            @RequestParam(defaultValue = "100") int batchSize,
            @RequestParam(defaultValue = "4") int threadCount) {
        
        long publishedCount = bulkDebitVoucherProducer.generateAndPublishBulkVouchers(count, batchSize, threadCount);
        
        Map<String, Object> response = new HashMap<>();
        response.put("requested", count);
        response.put("published", publishedCount);
        response.put("success_rate", count > 0 ? (publishedCount * 100.0 / count) : 0);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get the current processing statistics for the bulk consumer
     * @return Response with the current processing statistics
     */
    @GetMapping("/consumer/stats")
    public ResponseEntity<Map<String, Object>> getConsumerStats() {
        String stats = bulkDebitVoucherConsumer.getProcessingStats();
        
        Map<String, Object> response = new HashMap<>();
        response.put("stats", stats);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reset the processing statistics for the bulk consumer
     * @return Response indicating the statistics were reset
     */
    @PostMapping("/consumer/reset-stats")
    public ResponseEntity<Map<String, Object>> resetConsumerStats() {
        bulkDebitVoucherConsumer.resetStats();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Consumer statistics reset successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Shutdown the bulk consumer
     * @return Response indicating the consumer was shutdown
     */
    @PostMapping("/consumer/shutdown")
    public ResponseEntity<Map<String, Object>> shutdownConsumer() {
        bulkDebitVoucherConsumer.shutdown();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Consumer shutdown successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Initialize the bulk consumer
     * @return Response indicating the consumer was initialized
     */
    @PostMapping("/consumer/init")
    public ResponseEntity<Map<String, Object>> initConsumer() {
        bulkDebitVoucherConsumer.init();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Consumer initialized successfully");
        
        return ResponseEntity.ok(response);
    }
} 
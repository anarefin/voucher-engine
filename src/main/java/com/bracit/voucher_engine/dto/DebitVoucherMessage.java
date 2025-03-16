package com.bracit.voucher_engine.dto;

import com.bracit.voucher_engine.model.VoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for sending debit voucher messages via RabbitMQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitVoucherMessage {
    private String voucherNumber;
    private LocalDate voucherDate;
    private BigDecimal amount;
    private String description;
    private String createdBy;
    private LocalDate createdDate;
    private VoucherStatus status;
    private String debitAccount;
    private String creditAccount;
} 
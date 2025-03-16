package com.bracit.voucher_engine.service.impl;

import com.bracit.voucher_engine.dto.DebitVoucherDto;
import com.bracit.voucher_engine.exception.ResourceNotFoundException;
import com.bracit.voucher_engine.model.DebitVoucher;
import com.bracit.voucher_engine.model.VoucherStatus;
import com.bracit.voucher_engine.repository.DebitVoucherRepository;
import com.bracit.voucher_engine.service.DebitVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DebitVoucherServiceImpl implements DebitVoucherService {

    private final DebitVoucherRepository debitVoucherRepository;

    @Autowired
    public DebitVoucherServiceImpl(DebitVoucherRepository debitVoucherRepository) {
        this.debitVoucherRepository = debitVoucherRepository;
    }

    @Override
    public DebitVoucherDto createVoucher(DebitVoucherDto voucherDto) {
        DebitVoucher voucher = mapToEntity(voucherDto);
        voucher.setStatus(VoucherStatus.DRAFT);
        voucher.setCreatedDate(LocalDate.now());
        DebitVoucher savedVoucher = debitVoucherRepository.save(voucher);
        return mapToDto(savedVoucher);
    }

    @Override
    @Transactional
    public List<DebitVoucherDto> createVouchersBulk(List<DebitVoucherDto> voucherDtos) {
        if (voucherDtos == null || voucherDtos.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Map all DTOs to entities
        List<DebitVoucher> vouchers = voucherDtos.stream()
                .map(this::mapToEntity)
                .peek(voucher -> {
                    // Set default values if not already set
                    if (voucher.getStatus() == null) {
                        voucher.setStatus(VoucherStatus.DRAFT);
                    }
                    if (voucher.getCreatedDate() == null) {
                        voucher.setCreatedDate(LocalDate.now());
                    }
                })
                .collect(Collectors.toList());
        
        // Save all entities in a single batch operation
        List<DebitVoucher> savedVouchers = debitVoucherRepository.saveAll(vouchers);
        
        // Map saved entities back to DTOs
        return savedVouchers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public DebitVoucherDto getVoucherById(Long id) {
        DebitVoucher voucher = debitVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Debit Voucher not found with id: " + id));
        return mapToDto(voucher);
    }

    @Override
    public DebitVoucherDto getVoucherByNumber(String voucherNumber) {
        DebitVoucher voucher = debitVoucherRepository.findByVoucherNumber(voucherNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Debit Voucher not found with number: " + voucherNumber));
        return mapToDto(voucher);
    }

    @Override
    public List<DebitVoucherDto> getAllVouchers() {
        List<DebitVoucher> vouchers = debitVoucherRepository.findAll();
        return vouchers.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<DebitVoucherDto> getVouchersByStatus(VoucherStatus status) {
        List<DebitVoucher> vouchers = debitVoucherRepository.findByStatus(status);
        return vouchers.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<DebitVoucherDto> getVouchersByDateRange(LocalDate startDate, LocalDate endDate) {
        List<DebitVoucher> vouchers = debitVoucherRepository.findByVoucherDateBetween(startDate, endDate);
        return vouchers.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public DebitVoucherDto updateVoucher(Long id, DebitVoucherDto voucherDto) {
        DebitVoucher voucher = debitVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Debit Voucher not found with id: " + id));
        
        if (voucher.getStatus() == VoucherStatus.APPROVED) {
            throw new IllegalStateException("Cannot update an approved voucher");
        }
        
        voucher.setVoucherNumber(voucherDto.getVoucherNumber());
        voucher.setVoucherDate(voucherDto.getVoucherDate());
        voucher.setAmount(voucherDto.getAmount());
        voucher.setDescription(voucherDto.getDescription());
        voucher.setDebitAccount(voucherDto.getDebitAccount());
        voucher.setCreditAccount(voucherDto.getCreditAccount());
        
        DebitVoucher updatedVoucher = debitVoucherRepository.save(voucher);
        return mapToDto(updatedVoucher);
    }

    @Override
    public DebitVoucherDto approveVoucher(Long id, String approvedBy) {
        DebitVoucher voucher = debitVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Debit Voucher not found with id: " + id));
        
        voucher.setStatus(VoucherStatus.APPROVED);
        voucher.setApprovedBy(approvedBy);
        voucher.setApprovedDate(LocalDate.now());
        
        DebitVoucher approvedVoucher = debitVoucherRepository.save(voucher);
        return mapToDto(approvedVoucher);
    }

    @Override
    public DebitVoucherDto rejectVoucher(Long id, String rejectedBy) {
        DebitVoucher voucher = debitVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Debit Voucher not found with id: " + id));
        
        voucher.setStatus(VoucherStatus.REJECTED);
        voucher.setApprovedBy(rejectedBy); // Using approvedBy field to store rejectedBy
        voucher.setApprovedDate(LocalDate.now());
        
        DebitVoucher rejectedVoucher = debitVoucherRepository.save(voucher);
        return mapToDto(rejectedVoucher);
    }

    @Override
    public void deleteVoucher(Long id) {
        DebitVoucher voucher = debitVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Debit Voucher not found with id: " + id));
        
        if (voucher.getStatus() == VoucherStatus.APPROVED) {
            throw new IllegalStateException("Cannot delete an approved voucher");
        }
        
        debitVoucherRepository.delete(voucher);
    }
    
    // Helper methods for mapping between entity and DTO
    private DebitVoucherDto mapToDto(DebitVoucher voucher) {
        return DebitVoucherDto.builder()
                .id(voucher.getId())
                .voucherNumber(voucher.getVoucherNumber())
                .voucherDate(voucher.getVoucherDate())
                .amount(voucher.getAmount())
                .description(voucher.getDescription())
                .createdBy(voucher.getCreatedBy())
                .createdDate(voucher.getCreatedDate())
                .approvedBy(voucher.getApprovedBy())
                .approvedDate(voucher.getApprovedDate())
                .status(voucher.getStatus())
                .debitAccount(voucher.getDebitAccount())
                .creditAccount(voucher.getCreditAccount())
                .build();
    }
    
    private DebitVoucher mapToEntity(DebitVoucherDto dto) {
        return DebitVoucher.builder()
                .id(dto.getId())
                .voucherNumber(dto.getVoucherNumber())
                .voucherDate(dto.getVoucherDate())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .createdBy(dto.getCreatedBy())
                .createdDate(dto.getCreatedDate())
                .approvedBy(dto.getApprovedBy())
                .approvedDate(dto.getApprovedDate())
                .status(dto.getStatus())
                .debitAccount(dto.getDebitAccount())
                .creditAccount(dto.getCreditAccount())
                .build();
    }
} 
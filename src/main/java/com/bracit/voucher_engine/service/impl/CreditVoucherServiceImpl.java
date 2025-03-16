package com.bracit.voucher_engine.service.impl;

import com.bracit.voucher_engine.dto.CreditVoucherDto;
import com.bracit.voucher_engine.exception.ResourceNotFoundException;
import com.bracit.voucher_engine.model.CreditVoucher;
import com.bracit.voucher_engine.model.VoucherStatus;
import com.bracit.voucher_engine.repository.CreditVoucherRepository;
import com.bracit.voucher_engine.service.CreditVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreditVoucherServiceImpl implements CreditVoucherService {

    private final CreditVoucherRepository creditVoucherRepository;

    @Autowired
    public CreditVoucherServiceImpl(CreditVoucherRepository creditVoucherRepository) {
        this.creditVoucherRepository = creditVoucherRepository;
    }

    @Override
    public CreditVoucherDto createVoucher(CreditVoucherDto voucherDto) {
        CreditVoucher voucher = mapToEntity(voucherDto);
        voucher.setStatus(VoucherStatus.DRAFT);
        voucher.setCreatedDate(LocalDate.now());
        CreditVoucher savedVoucher = creditVoucherRepository.save(voucher);
        return mapToDto(savedVoucher);
    }

    @Override
    public CreditVoucherDto getVoucherById(Long id) {
        CreditVoucher voucher = creditVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit Voucher not found with id: " + id));
        return mapToDto(voucher);
    }

    @Override
    public CreditVoucherDto getVoucherByNumber(String voucherNumber) {
        CreditVoucher voucher = creditVoucherRepository.findByVoucherNumber(voucherNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Credit Voucher not found with number: " + voucherNumber));
        return mapToDto(voucher);
    }

    @Override
    public List<CreditVoucherDto> getAllVouchers() {
        List<CreditVoucher> vouchers = creditVoucherRepository.findAll();
        return vouchers.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<CreditVoucherDto> getVouchersByStatus(VoucherStatus status) {
        List<CreditVoucher> vouchers = creditVoucherRepository.findByStatus(status);
        return vouchers.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<CreditVoucherDto> getVouchersByDateRange(LocalDate startDate, LocalDate endDate) {
        List<CreditVoucher> vouchers = creditVoucherRepository.findByVoucherDateBetween(startDate, endDate);
        return vouchers.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public CreditVoucherDto updateVoucher(Long id, CreditVoucherDto voucherDto) {
        CreditVoucher voucher = creditVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit Voucher not found with id: " + id));
        
        if (voucher.getStatus() == VoucherStatus.APPROVED) {
            throw new IllegalStateException("Cannot update an approved voucher");
        }
        
        voucher.setVoucherNumber(voucherDto.getVoucherNumber());
        voucher.setVoucherDate(voucherDto.getVoucherDate());
        voucher.setAmount(voucherDto.getAmount());
        voucher.setDescription(voucherDto.getDescription());
        voucher.setCreditAccount(voucherDto.getCreditAccount());
        voucher.setDebitAccount(voucherDto.getDebitAccount());
        
        CreditVoucher updatedVoucher = creditVoucherRepository.save(voucher);
        return mapToDto(updatedVoucher);
    }

    @Override
    public CreditVoucherDto approveVoucher(Long id, String approvedBy) {
        CreditVoucher voucher = creditVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit Voucher not found with id: " + id));
        
        voucher.setStatus(VoucherStatus.APPROVED);
        voucher.setApprovedBy(approvedBy);
        voucher.setApprovedDate(LocalDate.now());
        
        CreditVoucher approvedVoucher = creditVoucherRepository.save(voucher);
        return mapToDto(approvedVoucher);
    }

    @Override
    public CreditVoucherDto rejectVoucher(Long id, String rejectedBy) {
        CreditVoucher voucher = creditVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit Voucher not found with id: " + id));
        
        voucher.setStatus(VoucherStatus.REJECTED);
        voucher.setApprovedBy(rejectedBy); // Using approvedBy field to store rejectedBy
        voucher.setApprovedDate(LocalDate.now());
        
        CreditVoucher rejectedVoucher = creditVoucherRepository.save(voucher);
        return mapToDto(rejectedVoucher);
    }

    @Override
    public void deleteVoucher(Long id) {
        CreditVoucher voucher = creditVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit Voucher not found with id: " + id));
        
        if (voucher.getStatus() == VoucherStatus.APPROVED) {
            throw new IllegalStateException("Cannot delete an approved voucher");
        }
        
        creditVoucherRepository.delete(voucher);
    }
    
    // Helper methods for mapping between entity and DTO
    private CreditVoucherDto mapToDto(CreditVoucher voucher) {
        return CreditVoucherDto.builder()
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
                .creditAccount(voucher.getCreditAccount())
                .debitAccount(voucher.getDebitAccount())
                .build();
    }
    
    private CreditVoucher mapToEntity(CreditVoucherDto dto) {
        return CreditVoucher.builder()
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
                .creditAccount(dto.getCreditAccount())
                .debitAccount(dto.getDebitAccount())
                .build();
    }
} 
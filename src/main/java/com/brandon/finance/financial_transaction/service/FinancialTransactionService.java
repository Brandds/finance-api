package com.brandon.finance.financial_transaction.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.financial_transaction.dto.FinancialTransactionDTO;
import com.brandon.finance.financial_transaction.entity.FinancialTransaction;
import com.brandon.finance.financial_transaction.entity.FinancialTransaction.TransactionType;
import com.brandon.finance.financial_transaction.mapper.FinancialTransactionMapper;
import com.brandon.finance.financial_transaction.repository.FinancialTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FinancialTransactionService {

    private final FinancialTransactionRepository transactionRepository;
    private final FinancialTransactionMapper transactionMapper;

    @Transactional
    public FinancialTransactionDTO create(FinancialTransactionDTO dto) {
        FinancialTransaction transaction = transactionMapper.toEntity(dto);
        FinancialTransaction saved = transactionRepository.save(transaction);
        return transactionMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public FinancialTransactionDTO getById(Long id) {
        return transactionRepository.findById(id)
            .map(transactionMapper::toDTO)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<FinancialTransactionDTO> getByUserId(Long userId, Pageable pageable) {
        Page<FinancialTransaction> transactions = transactionRepository.findByUserId(userId, pageable);
        List<FinancialTransactionDTO> dtos = transactions.getContent()
            .stream()
            .map(transactionMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, transactions.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<FinancialTransactionDTO> getByUserIdAndType(Long userId, TransactionType type, Pageable pageable) {
        Page<FinancialTransaction> transactions = transactionRepository.findByUserIdAndType(userId, type, pageable);
        List<FinancialTransactionDTO> dtos = transactions.getContent()
            .stream()
            .map(transactionMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, transactions.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<FinancialTransactionDTO> getByUserIdAndCategory(Long userId, Long categoryId) {
        return transactionRepository.findByUserIdAndCategoryId(userId, categoryId)
            .stream()
            .map(transactionMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FinancialTransactionDTO> getByUserIdAndAccount(Long userId, Long accountId) {
        return transactionRepository.findByUserIdAndAccountId(userId, accountId)
            .stream()
            .map(transactionMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<FinancialTransactionDTO> getByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<FinancialTransaction> transactions = transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate, pageable);
        List<FinancialTransactionDTO> dtos = transactions.getContent()
            .stream()
            .map(transactionMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, transactions.getTotalElements());
    }

    @Transactional
    public FinancialTransactionDTO update(Long id, FinancialTransactionDTO dto) {
        return transactionRepository.findById(id)
            .map(transaction -> {
                transaction.setDescription(dto.getDescription());
                transaction.setAmount(dto.getAmount());
                transaction.setDate(dto.getDate());
                transaction.setType(dto.getType());
                FinancialTransaction updated = transactionRepository.save(transaction);
                return transactionMapper.toDTO(updated);
            })
            .orElse(null);
    }

    @Transactional
    public void delete(Long id) {
        transactionRepository.deleteById(id);
    }
}

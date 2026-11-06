package com.brandon.finance.financial_transaction.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.account.repository.AccountRepository;
import com.brandon.finance.category.repository.CategoryRepository;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
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
    private final AuthenticatedUserService authenticationService;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public FinancialTransactionDTO create(FinancialTransactionDTO dto) {
        FinancialTransaction transaction = transactionMapper.toEntity(dto);
        transaction.setUser(authenticationService.getUser());
        Long userId = transaction.getUser().getId();
        transaction.setCategory(dto.getCategoryId() == null ? null
            : categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada")));
        transaction.setAccount(dto.getAccountId() == null ? null
            : accountRepository.findByIdAndUserId(dto.getAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada")));
        FinancialTransaction saved = transactionRepository.save(transaction);
        return transactionMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public FinancialTransactionDTO getById(Long id) {
        return transactionMapper.toDTO(getOwnedTransaction(id));
    }

    @Transactional(readOnly = true)
    public Page<FinancialTransactionDTO> getAllPage(Pageable pageable) {
        Page<FinancialTransaction> transactions = transactionRepository.findByUserId(authenticationService.getUserId(), pageable);
        List<FinancialTransactionDTO> dtos = transactions.getContent()
            .stream()
            .map(transactionMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, transactions.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<FinancialTransactionDTO> getByType(TransactionType type, Pageable pageable) {
        Page<FinancialTransaction> transactions = transactionRepository.findByUserIdAndType(authenticationService.getUserId(), type, pageable);
        List<FinancialTransactionDTO> dtos = transactions.getContent()
            .stream()
            .map(transactionMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, transactions.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<FinancialTransactionDTO> getByCategoryId(Long categoryId) {
        return transactionRepository.findByUserIdAndCategoryId(authenticationService.getUserId(), categoryId)
            .stream()
            .map(transactionMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FinancialTransactionDTO> getByAccountId(Long accountId) {
        return transactionRepository.findByUserIdAndAccountId(authenticationService.getUserId(), accountId)
            .stream()
            .map(transactionMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<FinancialTransactionDTO> getByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<FinancialTransaction> transactions = transactionRepository.findByUserIdAndDateBetween(authenticationService.getUserId(), startDate, endDate, pageable);
        List<FinancialTransactionDTO> dtos = transactions.getContent()
            .stream()
            .map(transactionMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, transactions.getTotalElements());
    }

    @Transactional
    public FinancialTransactionDTO update(Long id, FinancialTransactionDTO dto) {
        return transactionRepository.findByIdAndUserId(id, authenticationService.getUserId())
            .map(transaction -> {
                transaction.setDescription(dto.getDescription());
                transaction.setAmount(dto.getAmount());
                transaction.setDate(dto.getDate());
                transaction.setType(dto.getType());
                FinancialTransaction updated = transactionRepository.save(transaction);
                return transactionMapper.toDTO(updated);
            })
            .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada"));
    }

    @Transactional
    public void delete(Long id) {
        transactionRepository.delete(getOwnedTransaction(id));
    }

    private FinancialTransaction getOwnedTransaction(Long id) {
        return transactionRepository.findByIdAndUserId(id, authenticationService.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada"));
    }
}

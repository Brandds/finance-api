package com.brandon.finance.account.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.account.dto.AccountDTO;
import com.brandon.finance.account.entity.Account;
import com.brandon.finance.account.entity.Account.AccountType;
import com.brandon.finance.account.mapper.AccountMapper;
import com.brandon.finance.account.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountDTO create(AccountDTO dto) {
        Account account = accountMapper.toEntity(dto);
        Account saved = accountRepository.save(account);
        return accountMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public AccountDTO getById(Long id) {
        return accountRepository.findById(id)
            .map(accountMapper::toDTO)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<AccountDTO> getByUserId(Long userId, Pageable pageable) {
        Page<Account> accounts = accountRepository.findByUserId(userId, pageable);
        List<AccountDTO> dtos = accounts.getContent()
            .stream()
            .map(accountMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, accounts.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getByUserIdList(Long userId) {
        return accountRepository.findByUserId(userId)
            .stream()
            .map(accountMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getByUserIdAndType(Long userId, AccountType type) {
        return accountRepository.findByUserIdAndType(userId, type)
            .stream()
            .map(accountMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountDTO getByUserIdAndName(Long userId, String name) {
        Account account = accountRepository.findByUserIdAndName(userId, name);
        return accountMapper.toDTO(account);
    }

    @Transactional
    public AccountDTO update(Long id, AccountDTO dto) {
        return accountRepository.findById(id)
            .map(account -> {
                account.setName(dto.getName());
                account.setType(dto.getType());
                Account updated = accountRepository.save(account);
                return accountMapper.toDTO(updated);
            })
            .orElse(null);
    }

    @Transactional
    public void delete(Long id) {
        accountRepository.deleteById(id);
    }
}

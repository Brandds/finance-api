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
import com.brandon.finance.authentication.service.AuthenticatedUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AuthenticatedUserService authenticationService;


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
    public Page<AccountDTO> getAllPage(Pageable pageable) {
        Page<Account> accounts = accountRepository.findByUserId(authenticationService.getUserId(), pageable);
        List<AccountDTO> dtos = accounts.getContent()
            .stream()
            .map(accountMapper::toDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, accounts.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getAll() {
        return accountRepository.findByUserId(authenticationService.getUserId())
            .stream()
            .map(accountMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getByType(AccountType type) {
        return accountRepository.findByUserIdAndType(authenticationService.getUserId(), type)
            .stream()
            .map(accountMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountDTO getByName(String name) {
        Account account = accountRepository.findByUserIdAndName(authenticationService.getUserId(), name);
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

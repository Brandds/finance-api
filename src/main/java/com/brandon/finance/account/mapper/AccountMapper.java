package com.brandon.finance.account.mapper;

import org.springframework.stereotype.Component;

import com.brandon.finance.account.dto.AccountDTO;
import com.brandon.finance.account.entity.Account;
import com.brandon.finance.user.entity.User;

@Component
public class AccountMapper {

    public AccountDTO toDTO(Account account) {
        if (account == null) {
            return null;
        }

        return new AccountDTO(
            account.getId(),
            account.getName(),
            account.getType(),
            account.getUser() != null ? account.getUser().getId() : null
        );
    }

    public Account toEntity(AccountDTO dto) {
        if (dto == null) {
            return null;
        }

        Account account = new Account();
        account.setName(dto.getName());
        account.setType(dto.getType());
        account.setUser(new User(dto.getUserId()));

        return account;
    }
}

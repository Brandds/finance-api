package com.brandon.finance.account.dto;

import com.brandon.finance.account.entity.Account.AccountType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

    private Long id;
    private String name;
    private AccountType type;
    private Long userId;
}

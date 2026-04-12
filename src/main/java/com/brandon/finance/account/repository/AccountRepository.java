package com.brandon.finance.account.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brandon.finance.account.entity.Account;
import com.brandon.finance.account.entity.Account.AccountType;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserId(Long userId);

    Page<Account> findByUserId(Long userId, Pageable pageable);

    List<Account> findByUserIdAndType(Long userId, AccountType type);

    Account findByUserIdAndName(Long userId, String name);
}

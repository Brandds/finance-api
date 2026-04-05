package com.brandon.finance.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.brandon.finance.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
}

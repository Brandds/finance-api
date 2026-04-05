package com.brandon.finance.helper;

import org.springframework.security.core.context.SecurityContextHolder;

import com.brandon.finance.user.entity.User;

public class SecurityUtils {
    
    public static User getAuthenticatedUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }    
}

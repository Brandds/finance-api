package com.brandon.finance.email.service.impl;

import com.brandon.finance.user.entity.User;

public interface EmailService {
    void sendVerificationEmail(User user, String token);
}

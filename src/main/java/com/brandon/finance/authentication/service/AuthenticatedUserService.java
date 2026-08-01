package com.brandon.finance.authentication.service;

import com.brandon.finance.auth.model.AuthenticatedUser;
import com.brandon.finance.user.entity.User;

public interface AuthenticatedUserService {

    AuthenticatedUser getAuthenticatedUser();

    User getUser();

    Long getUserId();

    String getEmail();
}
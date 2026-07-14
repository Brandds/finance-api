package com.brandon.finance.helper;

import java.util.UUID;

public class TokenUtils {
    
    public static String generateToken() {
        String token = UUID.randomUUID().toString();
        return token;
    }
}

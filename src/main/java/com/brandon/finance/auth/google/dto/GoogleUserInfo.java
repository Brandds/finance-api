package com.brandon.finance.auth.google.dto;


public record GoogleUserInfo(
    String sub,
    String email,
    String name,
    String picture,
    Boolean emailVerified
) {}

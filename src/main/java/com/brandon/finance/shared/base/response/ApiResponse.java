package com.brandon.finance.shared.base.response;

public record ApiResponse<T> (
    T data,
    String message
) {}

package com.brandon.finance.shared.base.excepetion.record;

import java.time.LocalDateTime;

public record ApiError(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {}
    

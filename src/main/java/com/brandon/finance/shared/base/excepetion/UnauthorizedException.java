package com.brandon.finance.shared.base.excepetion;

import com.brandon.finance.shared.base.enums.ErrorCode;

public class UnauthorizedException extends BusinessException {
    
    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
    
}

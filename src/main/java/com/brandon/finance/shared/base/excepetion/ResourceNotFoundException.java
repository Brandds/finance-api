package com.brandon.finance.shared.base.excepetion;

import com.brandon.finance.shared.base.enums.ErrorCode;

public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
    
}

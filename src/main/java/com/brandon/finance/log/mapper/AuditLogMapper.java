package com.brandon.finance.log.mapper;

import org.springframework.stereotype.Component;

import com.brandon.finance.log.entity.AuditLog;
import com.brandon.finance.log.response.AuditLogResponse;

@Component
public class AuditLogMapper {
    
    public AuditLogResponse toResponse(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }
        
        return new AuditLogResponse(
            auditLog.getId(),
            auditLog.getTableName(),
            auditLog.getRecordId(),
            auditLog.getAction(),
            auditLog.getOldData(),
            auditLog.getNewData(),
            auditLog.getChangedAt()
        );
    }
}

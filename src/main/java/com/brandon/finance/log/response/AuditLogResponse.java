package com.brandon.finance.log.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    
    private Long id;
    private String tableName;
    private Long recordId;
    private String action;
    private String oldData;
    private String newData;
    private LocalDateTime changedAt;
}

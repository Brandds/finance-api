package com.brandon.finance.log.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brandon.finance.log.entity.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByTableNameAndRecordId(String tableName, Long recordId);
    
    List<AuditLog> findByTableName(String tableName);
    
    List<AuditLog> findByAction(String action);
    
    Page<AuditLog> findByTableName(String tableName, Pageable pageable);
    
    Page<AuditLog> findByChangedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    Page<AuditLog> findByTableNameAndChangedAtBetween(String tableName, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}

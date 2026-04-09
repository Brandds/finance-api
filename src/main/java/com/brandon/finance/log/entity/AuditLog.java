package com.brandon.finance.log.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "log", schema = "audit")
public class AuditLog {
    
    @Id
    @Column(name = "id")
    private Long id;
    
    @Column(name = "table_name", nullable = false)
    private String tableName;
    
    @Column(name = "record_id")
    private Long recordId;
    
    @Column(name = "action", nullable = false, length = 10)
    private String action;
    
    @Column(name = "old_data", columnDefinition = "jsonb")
    private String oldData;
    
    @Column(name = "new_data", columnDefinition = "jsonb")
    private String newData;
    
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}

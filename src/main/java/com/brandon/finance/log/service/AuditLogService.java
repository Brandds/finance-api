package com.brandon.finance.log.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.brandon.finance.log.entity.AuditLog;
import com.brandon.finance.log.mapper.AuditLogMapper;
import com.brandon.finance.log.repository.AuditLogRepository;
import com.brandon.finance.log.response.AuditLogResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;
    
    /**
     * Busca o histórico completo de mudanças para um registro específico
     * @param tableName Nome da tabela
     * @param recordId ID do registro
     * @return Lista com histórico de mudanças
     */
    public List<AuditLogResponse> getChangeHistory(String tableName, Long recordId) {
        return auditLogRepository.findByTableNameAndRecordId(tableName, recordId)
            .stream()
            .map(auditLogMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Busca todas as mudanças de uma tabela
     * @param tableName Nome da tabela
     * @param pageable Paginação
     * @return Página com logs de auditoria
     */
    public Page<AuditLogResponse> getTableChanges(String tableName, Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findByTableName(tableName, pageable);
        List<AuditLogResponse> content = page.getContent()
            .stream()
            .map(auditLogMapper::toResponse)
            .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }
    
    /**
     * Busca logs por período
     * @param startDate Data inicial
     * @param endDate Data final
     * @param pageable Paginação
     * @return Página com logs de auditoria
     */
    public Page<AuditLogResponse> getChangesByPeriod(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findByChangedAtBetween(startDate, endDate, pageable);
        List<AuditLogResponse> content = page.getContent()
            .stream()
            .map(auditLogMapper::toResponse)
            .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }
    
    /**
     * Busca mudanças de uma tabela em um período específico
     * @param tableName Nome da tabela
     * @param startDate Data inicial
     * @param endDate Data final
     * @param pageable Paginação
     * @return Página com logs de auditoria
     */
    public Page<AuditLogResponse> getTableChangesByPeriod(String tableName, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findByTableNameAndChangedAtBetween(tableName, startDate, endDate, pageable);
        List<AuditLogResponse> content = page.getContent()
            .stream()
            .map(auditLogMapper::toResponse)
            .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }
    
    /**
     * Busca todos os logs de um tipo de ação (INSERT, UPDATE, DELETE)
     * @param action Tipo de ação
     * @return Lista com logs de auditoria
     */
    public List<AuditLogResponse> getLogsByAction(String action) {
        return auditLogRepository.findByAction(action)
            .stream()
            .map(auditLogMapper::toResponse)
            .collect(Collectors.toList());
    }
}

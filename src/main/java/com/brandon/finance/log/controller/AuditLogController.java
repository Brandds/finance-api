package com.brandon.finance.log.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brandon.finance.log.response.AuditLogResponse;
import com.brandon.finance.log.service.AuditLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Endpoints para consultar logs de auditoria")
public class AuditLogController {
    
    private final AuditLogService auditLogService;
    
    /**
     * Obtém o histórico completo de mudanças para um registro específico
     * Exemplo: GET /api/v1/audit-logs/users/1
     */
    @Operation(summary = "Obtém histórico de mudanças de um registro", description = "Retorna todas as mudanças realizadas em um registro específico")
    @GetMapping("/{tableName}/{recordId}")
    public ResponseEntity<List<AuditLogResponse>> getChangeHistory(
            @PathVariable String tableName,
            @PathVariable Long recordId) {
        List<AuditLogResponse> history = auditLogService.getChangeHistory(tableName, recordId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Obtém todas as mudanças de uma tabela com paginação
     * Exemplo: GET /api/v1/audit-logs/table/users?page=0&size=10&sort=changedAt,desc
     */
    @Operation(summary = "Obtém mudanças de uma tabela", description = "Retorna todas as mudanças realizadas em uma tabela específica com suporte a paginação")
    @GetMapping("/table/{tableName}")
    public ResponseEntity<Page<AuditLogResponse>> getTableChanges(
            @PathVariable String tableName,
            Pageable pageable) {
        Page<AuditLogResponse> changes = auditLogService.getTableChanges(tableName, pageable);
        return ResponseEntity.ok(changes);
    }
    
    /**
     * Obtém logs por período de tempo
     * Exemplo: GET /api/v1/audit-logs/period?startDate=2026-04-01T00:00:00&endDate=2026-04-09T23:59:59&page=0&size=10
     */
    @Operation(summary = "Obtém logs por período", description = "Retorna logs de auditoria em um período de tempo específico")
    @GetMapping("/period")
    public ResponseEntity<Page<AuditLogResponse>> getChangesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<AuditLogResponse> changes = auditLogService.getChangesByPeriod(startDate, endDate, pageable);
        return ResponseEntity.ok(changes);
    }
    
    /**
     * Obtém mudanças de uma tabela em um período específico
     * Exemplo: GET /api/v1/audit-logs/table/users/period?startDate=2026-04-01T00:00:00&endDate=2026-04-09T23:59:59&page=0&size=10
     */
    @Operation(summary = "Obtém mudanças de uma tabela por período", description = "Retorna mudanças de uma tabela específica em um período de tempo")
    @GetMapping("/table/{tableName}/period")
    public ResponseEntity<Page<AuditLogResponse>> getTableChangesByPeriod(
            @PathVariable String tableName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<AuditLogResponse> changes = auditLogService.getTableChangesByPeriod(tableName, startDate, endDate, pageable);
        return ResponseEntity.ok(changes);
    }
    
    /**
     * Obtém logs por tipo de ação
     * Exemplo: GET /api/v1/audit-logs/action/INSERT
     * Ações possíveis: INSERT, UPDATE, DELETE
     */
    @Operation(summary = "Obtém logs por tipo de ação", description = "Retorna todos os logs de um tipo de ação específico (INSERT, UPDATE, DELETE)")
    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLogResponse>> getLogsByAction(
            @PathVariable String action) {
        List<AuditLogResponse> logs = auditLogService.getLogsByAction(action);
        return ResponseEntity.ok(logs);
    }
}

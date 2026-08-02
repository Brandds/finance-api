package com.brandon.finance.financial_transaction.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brandon.finance.financial_transaction.dto.FinancialTransactionDTO;
import com.brandon.finance.financial_transaction.entity.FinancialTransaction.TransactionType;
import com.brandon.finance.financial_transaction.service.FinancialTransactionService;
import com.brandon.finance.shared.base.response.ApiResponse;
import com.brandon.finance.shared.base.response.ResponseUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.brandon.finance.shared.constants.ApiPaths;
@RestController
@RequestMapping(ApiPaths.FINANCIAL_TRANSACTIONS)
@RequiredArgsConstructor
@Tag(name = "Financial Transactions", description = "Operações relacionadas a transações financeiras")
public class FinancialTransactionController {

    private final FinancialTransactionService transactionService;

    @PostMapping
    @Operation(summary = "Criar transação financeira", description = "Cria uma nova transação financeira")
    public ResponseEntity<ApiResponse<FinancialTransactionDTO>> create(@Valid @RequestBody FinancialTransactionDTO dto) {
        FinancialTransactionDTO created = transactionService.create(dto);
        return ResponseUtil.created(created, "Transação criada com sucesso");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter transação por ID", description = "Retorna uma transação específica")
    public ResponseEntity<ApiResponse<FinancialTransactionDTO>> getById(@PathVariable Long id) {
        FinancialTransactionDTO dto = transactionService.getById(id);
        if (dto != null) {
            return ResponseUtil.ok(dto, "Transação encontrada com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping()
    @Operation(summary = "Listar transações do usuário", description = "Retorna todas as transações de um usuário")
    public ResponseEntity<ApiResponse<Page<FinancialTransactionDTO>>> getAllPage(Pageable pageable) {
        Page<FinancialTransactionDTO> transactions = transactionService.getAllPage(pageable);
        return ResponseUtil.ok(transactions, "Transações encontradas com sucesso");
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Listar transações por tipo", description = "Retorna transações filtradas por tipo (EXPENSE ou INCOME)")
    public ResponseEntity<ApiResponse<Page<FinancialTransactionDTO>>> getByType(@PathVariable TransactionType type,
            Pageable pageable) {
        Page<FinancialTransactionDTO> transactions = transactionService.getByType(type, pageable);
        return ResponseUtil.ok(transactions, "Transações encontradas com sucesso");
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Listar transações por categoria", description = "Retorna transações de um usuário em uma categoria específica")
    public ResponseEntity<ApiResponse<List<FinancialTransactionDTO>>> getByCategoryId(@PathVariable Long categoryId) {
        List<FinancialTransactionDTO> transactions = transactionService.getByCategoryId(categoryId);
        return ResponseUtil.ok(transactions, "Transações encontradas com sucesso");
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Listar transações por conta", description = "Retorna transações de um usuário em uma conta específica")
    public ResponseEntity<ApiResponse<List<FinancialTransactionDTO>>> getByAccountId(@PathVariable Long accountId) {
        List<FinancialTransactionDTO> transactions = transactionService.getByAccountId(accountId);
        return ResponseUtil.ok(transactions, "Transações encontradas com sucesso");
    }

    @GetMapping("/date-range")
    @Operation(summary = "Listar transações por período", description = "Retorna transações em um período de datas")
    public ResponseEntity<ApiResponse<Page<FinancialTransactionDTO>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        Page<FinancialTransactionDTO> transactions = transactionService.getByDateRange(startDate, endDate, pageable);
        return ResponseUtil.ok(transactions, "Transações encontradas com sucesso");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar transação", description = "Atualiza uma transação existente")
    public ResponseEntity<ApiResponse<FinancialTransactionDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody FinancialTransactionDTO dto) {
        FinancialTransactionDTO updated = transactionService.update(id, dto);
        if (updated != null) {
            return ResponseUtil.ok(updated, "Transação atualizada com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar transação", description = "Remove uma transação")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseUtil.noContent("Transação removida com sucesso");
    }
}

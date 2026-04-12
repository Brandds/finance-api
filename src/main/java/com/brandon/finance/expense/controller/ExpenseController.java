package com.brandon.finance.expense.controller;

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

import com.brandon.finance.expense.dto.ExpenseDTO;
import com.brandon.finance.expense.service.ExpenseService;
import com.brandon.finance.shared.base.response.ApiResponse;
import com.brandon.finance.shared.base.response.ResponseUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Operações relacionadas a despesas")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Criar despesa", description = "Cria uma nova despesa")
    public ResponseEntity<ApiResponse<ExpenseDTO>> create(@Valid @RequestBody ExpenseDTO dto) {
        ExpenseDTO created = expenseService.create(dto);
        return ResponseUtil.created(created, "Despesa criada com sucesso");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter despesa por ID", description = "Retorna uma despesa específica")
    public ResponseEntity<ApiResponse<ExpenseDTO>> getById(@PathVariable Long id) {
        ExpenseDTO dto = expenseService.getById(id);
        if (dto != null) {
            return ResponseUtil.ok(dto, "Despesa encontrada com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Listar despesas do usuário", description = "Retorna todas as despesas de um usuário")
    public ResponseEntity<ApiResponse<Page<ExpenseDTO>>> getByUserId(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<ExpenseDTO> expenses = expenseService.getByUserId(userId, pageable);
        return ResponseUtil.ok(expenses, "Despesas encontradas com sucesso");
    }

    @GetMapping("/user/{userId}/category/{categoryId}")
    @Operation(summary = "Listar despesas por categoria", description = "Retorna despesas de um usuário em uma categoria específica")
    public ResponseEntity<ApiResponse<List<ExpenseDTO>>> getByUserIdAndCategory(
            @PathVariable Long userId,
            @PathVariable Long categoryId) {
        List<ExpenseDTO> expenses = expenseService.getByUserIdAndCategory(userId, categoryId);
        return ResponseUtil.ok(expenses, "Despesas encontradas com sucesso");
    }

    @GetMapping("/user/{userId}/date-range")
    @Operation(summary = "Listar despesas por período", description = "Retorna despesas em um período de datas")
    public ResponseEntity<ApiResponse<Page<ExpenseDTO>>> getByUserIdAndDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        Page<ExpenseDTO> expenses = expenseService.getByUserIdAndDateRange(userId, startDate, endDate, pageable);
        return ResponseUtil.ok(expenses, "Despesas encontradas com sucesso");
    }

    @GetMapping("/user/{userId}/account/{accountId}")
    @Operation(summary = "Listar despesas por conta", description = "Retorna despesas de um usuário em uma conta específica")
    public ResponseEntity<ApiResponse<List<ExpenseDTO>>> getByUserIdAndAccount(
            @PathVariable Long userId,
            @PathVariable Long accountId) {
        List<ExpenseDTO> expenses = expenseService.getByUserIdAndAccount(userId, accountId);
        return ResponseUtil.ok(expenses, "Despesas encontradas com sucesso");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar despesa", description = "Atualiza uma despesa existente")
    public ResponseEntity<ApiResponse<ExpenseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDTO dto) {
        ExpenseDTO updated = expenseService.update(id, dto);
        if (updated != null) {
            return ResponseUtil.ok(updated, "Despesa atualizada com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar despesa", description = "Remove uma despesa")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseUtil.noContent("Despesa removida com sucesso");
    }
}

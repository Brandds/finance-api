package com.brandon.finance.budget.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import com.brandon.finance.budget.dto.BudgetDTO;
import com.brandon.finance.budget.service.BudgetService;
import com.brandon.finance.shared.base.response.ApiResponse;
import com.brandon.finance.shared.base.response.ResponseUtil;
import com.brandon.finance.shared.constants.ApiPaths;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.BUDGETS)
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Operações relacionadas a orçamentos")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    @Operation(summary = "Criar orçamento", description = "Cria um novo orçamento")
    public ResponseEntity<ApiResponse<BudgetDTO>> create(@Valid @RequestBody BudgetDTO dto) {
        BudgetDTO created = budgetService.create(dto);
        return ResponseUtil.created(created, "Orçamento criado com sucesso");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter orçamento por ID", description = "Retorna um orçamento específico")
    public ResponseEntity<ApiResponse<BudgetDTO>> getById(@PathVariable Long id) {
        BudgetDTO dto = budgetService.getById(id);
        if (dto != null) {
            return ResponseUtil.ok(dto, "Orçamento encontrado com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping()
    @Operation(summary = "Listar orçamentos pagos", description = "Retorna todos os orçamentos de um usuário com paginação")
    public ResponseEntity<ApiResponse<Page<BudgetDTO>>> getAllPage(Pageable pageable) {
        Page<BudgetDTO> budgets = budgetService.getAllPage(pageable);
        return ResponseUtil.ok(budgets, "Orçamentos encontrados com sucesso");
    }

    @GetMapping("/user/list")
    @Operation(summary = "Listar orçamentos do usuário (sem paginação)", description = "Retorna todos os orçamentos de um usuário")
    public ResponseEntity<ApiResponse<List<BudgetDTO>>> getAll() {
        List<BudgetDTO> budgets = budgetService.getAll();
        return ResponseUtil.ok(budgets, "Orçamentos encontrados com sucesso");
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Listar orçamentos por categoria", description = "Retorna orçamentos de um usuário em uma categoria específica")
    public ResponseEntity<ApiResponse<List<BudgetDTO>>> getByCategoryId(@PathVariable Long categoryId) {
        List<BudgetDTO> budgets = budgetService.getByCategoryId(categoryId);
        return ResponseUtil.ok(budgets, "Orçamentos encontrados com sucesso");
    }

    @GetMapping("/month/{month}")
    @Operation(summary = "Listar orçamentos por mês", description = "Retorna orçamentos de um usuário em um mês específico")
    public ResponseEntity<ApiResponse<List<BudgetDTO>>> getByMonth(@PathVariable Integer month) {
        List<BudgetDTO> budgets = budgetService.getByMonth(month);
        return ResponseUtil.ok(budgets, "Orçamentos encontrados com sucesso");
    }

    @GetMapping("/year/{year}")
    @Operation(summary = "Listar orçamentos por ano", description = "Retorna orçamentos de um usuário em um ano específico")
    public ResponseEntity<ApiResponse<List<BudgetDTO>>> getByYear(@PathVariable Integer year) {
        List<BudgetDTO> budgets = budgetService.getByYear(year);
        return ResponseUtil.ok(budgets, "Orçamentos encontrados com sucesso");
    }

    @GetMapping("/user/{userId}/period")
    @Operation(summary = "Listar orçamentos por período", description = "Retorna orçamentos de um usuário em um mês e ano específicos")
    public ResponseEntity<ApiResponse<List<BudgetDTO>>> getByMonthAndYear(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        List<BudgetDTO> budgets = budgetService.getByMonthAndYear(month, year);
        return ResponseUtil.ok(budgets, "Orçamentos encontrados com sucesso");
    }

    @GetMapping("/user/{userId}/category/{categoryId}/period")
    @Operation(summary = "Buscar orçamento por categoria e período", description = "Retorna um orçamento específico por categoria, mês e ano")
    public ResponseEntity<ApiResponse<BudgetDTO>> getByCategoryIdAndMonthAndYear(
            @PathVariable Long categoryId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        BudgetDTO dto = budgetService.getByCategoryIdAndMonthAndYear(categoryId, month, year);
        if (dto != null) {
            return ResponseUtil.ok(dto, "Orçamento encontrado com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar orçamento", description = "Atualiza um orçamento existente")
    public ResponseEntity<ApiResponse<BudgetDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody BudgetDTO dto) {
        BudgetDTO updated = budgetService.update(id, dto);
        if (updated != null) {
            return ResponseUtil.ok(updated, "Orçamento atualizado com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar orçamento", description = "Remove um orçamento")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        budgetService.delete(id);
        return ResponseUtil.noContent("Orçamento removido com sucesso");
    }
}

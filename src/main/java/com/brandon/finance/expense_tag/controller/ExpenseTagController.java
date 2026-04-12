package com.brandon.finance.expense_tag.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brandon.finance.expense_tag.dto.ExpenseTagDTO;
import com.brandon.finance.expense_tag.service.ExpenseTagService;
import com.brandon.finance.shared.base.response.ApiResponse;
import com.brandon.finance.shared.base.response.ResponseUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/expense-tags")
@RequiredArgsConstructor
@Tag(name = "Expense Tags", description = "Operações relacionadas a tags de despesas")
public class ExpenseTagController {

    private final ExpenseTagService expenseTagService;

    @PostMapping
    @Operation(summary = "Adicionar tag a despesa", description = "Adiciona uma tag a uma despesa")
    public ResponseEntity<ApiResponse<ExpenseTagDTO>> create(@Valid @RequestBody ExpenseTagDTO dto) {
        ExpenseTagDTO created = expenseTagService.create(dto);
        if (created != null) {
            return ResponseUtil.created(created, "Tag adicionada com sucesso");
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/expense/{expenseId}")
    @Operation(summary = "Listar tags da despesa", description = "Retorna todas as tags de uma despesa")
    public ResponseEntity<ApiResponse<List<ExpenseTagDTO>>> getByExpenseId(
            @PathVariable Long expenseId) {
        List<ExpenseTagDTO> tags = expenseTagService.getByExpenseId(expenseId);
        return ResponseUtil.ok(tags, "Tags encontradas com sucesso");
    }

    @GetMapping("/expense/{expenseId}/page")
    @Operation(summary = "Listar tags da despesa (com paginação)", description = "Retorna tags de uma despesa com paginação")
    public ResponseEntity<ApiResponse<Page<ExpenseTagDTO>>> getByExpenseIdPage(
            @PathVariable Long expenseId,
            Pageable pageable) {
        Page<ExpenseTagDTO> tags = expenseTagService.getByExpenseIdPage(expenseId, pageable);
        return ResponseUtil.ok(tags, "Tags encontradas com sucesso");
    }

    @GetMapping("/tag/{tagId}")
    @Operation(summary = "Listar despesas com tag", description = "Retorna todas as despesas que possuem uma tag específica")
    public ResponseEntity<ApiResponse<List<ExpenseTagDTO>>> getByTagId(
            @PathVariable Long tagId) {
        List<ExpenseTagDTO> expenses = expenseTagService.getByTagId(tagId);
        return ResponseUtil.ok(expenses, "Despesas encontradas com sucesso");
    }

    @GetMapping("/tag/{tagId}/page")
    @Operation(summary = "Listar despesas com tag (com paginação)", description = "Retorna despesas que possuem uma tag com paginação")
    public ResponseEntity<ApiResponse<Page<ExpenseTagDTO>>> getByTagIdPage(
            @PathVariable Long tagId,
            Pageable pageable) {
        Page<ExpenseTagDTO> expenses = expenseTagService.getByTagIdPage(tagId, pageable);
        return ResponseUtil.ok(expenses, "Despesas encontradas com sucesso");
    }

    @DeleteMapping("/expense/{expenseId}/tag/{tagId}")
    @Operation(summary = "Remover tag de despesa", description = "Remove uma tag de uma despesa")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long expenseId,
            @PathVariable Long tagId) {
        expenseTagService.delete(expenseId, tagId);
        return ResponseUtil.noContent("Tag removida com sucesso");
    }
}

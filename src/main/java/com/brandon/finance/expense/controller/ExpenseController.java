package com.brandon.finance.expense.controller;

import java.time.LocalDate;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brandon.finance.expense.dto.ExpenseDTO;
import com.brandon.finance.expense.dto.ExpenseFilterRequest;
import com.brandon.finance.expense.dto.ExpenseAnalysisDTO;
import com.brandon.finance.expense.dto.ExpenseSummaryRequest;
import com.brandon.finance.expense.service.ExpenseService;
import com.brandon.finance.shared.base.response.ApiResponse;
import com.brandon.finance.shared.base.response.ResponseUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.brandon.finance.shared.constants.ApiPaths;

@RestController
@RequestMapping(ApiPaths.EXPENSES)
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Operações relacionadas a despesas")
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Entrada inválida: errorCode VALIDATION_ERROR e mensagem de validação",
    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(
        implementation = com.brandon.finance.shared.base.excepetion.record.ApiError.class)))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso não encontrado para o usuário autenticado",
    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(
        implementation = com.brandon.finance.shared.base.excepetion.record.ApiError.class)))
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Criar despesa", description = "Descrição obrigatória após trim, até 255 caracteres; valor positivo com até duas casas decimais; data ISO válida obrigatória. Proprietário definido pela autenticação.")
    public ResponseEntity<ApiResponse<ExpenseDTO>> create(@Valid @RequestBody ExpenseDTO dto) {
        ExpenseDTO created = expenseService.create(dto);
        return ResponseUtil.created(created, "Despesa criada com sucesso");
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Obter despesa por ID", description = "Retorna uma despesa específica")
    public ResponseEntity<ApiResponse<ExpenseDTO>> getById(@PathVariable Long id) {
        ExpenseDTO dto = expenseService.getById(id);
        if (dto != null) {
            return ResponseUtil.ok(dto, "Despesa encontrada com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping()
    @Operation(summary = "Listar despesas do usuário", description = "Retorna todas as despesas de um usuário")
    public ResponseEntity<ApiResponse<Page<ExpenseDTO>>> getAllPage(Pageable pageable) {
        Page<ExpenseDTO> expenses = expenseService.getAllPage(pageable);
        return ResponseUtil.ok(expenses, "Despesas encontradas com sucesso");
    }

    @GetMapping("/filter")
    @Operation(summary = "Filtrar despesas do usuário autenticado",
        description = "Combina description (trecho literal sem caixa/acentos, vazio ignorado), categorias (IDs separados por vírgula), período, valores, ordenação e paginação. Limites inclusivos; filtro aplicado antes da paginação. "
            + "Períodos predefinidos substituem as datas informadas; CUSTOM exige ambas as datas.")
    public ResponseEntity<ApiResponse<Page<ExpenseDTO>>> filter(
            @Valid @ModelAttribute @ParameterObject ExpenseFilterRequest filter, @ParameterObject Pageable pageable) {
        return ResponseUtil.ok(expenseService.filter(filter, pageable), "Despesas filtradas com sucesso");
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Listar despesas por categoria", description = "Retorna despesas de um usuário em uma categoria específica")
    public ResponseEntity<ApiResponse<List<ExpenseDTO>>> getByCategoryId(@PathVariable Long categoryId) {
        List<ExpenseDTO> expenses = expenseService.getByCategoryId(categoryId);
        return ResponseUtil.ok(expenses, "Despesas encontradas com sucesso");
    }

    @GetMapping("/date-range")
    @Operation(summary = "Listar despesas por período", description = "Retorna despesas em um período de datas")
    public ResponseEntity<ApiResponse<Page<ExpenseDTO>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        Page<ExpenseDTO> expenses = expenseService.getByDateRange(startDate, endDate, pageable);
        return ResponseUtil.ok(expenses, "Despesas encontradas com sucesso");
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Listar despesas por conta", description = "Retorna despesas de um usuário em uma conta específica")
    public ResponseEntity<ApiResponse<List<ExpenseDTO>>> getByAccountId(@PathVariable Long accountId) {
        List<ExpenseDTO> expenses = expenseService.getByAccountId(accountId);
        return ResponseUtil.ok(expenses, "Despesas encontradas com sucesso");
    }

    @PutMapping("/{id:\\d+}")
    @Operation(summary = "Atualizar despesa", description = "Atualiza descrição, valor, data, categoria e conta. Mesmas validações da criação. categoryId/accountId null ou ausentes removem os vínculos; vínculos devem pertencer ao usuário autenticado. id/userId enviados não alteram identidade ou proprietário.")
    public ResponseEntity<ApiResponse<ExpenseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDTO dto) {
        ExpenseDTO updated = expenseService.update(id, dto);
        if (updated != null) {
            return ResponseUtil.ok(updated, "Despesa atualizada com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "Deletar despesa", description = "Remove uma despesa")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseUtil.noContent("Despesa removida com sucesso");
    }

    @GetMapping({"/get-expense-analysis", "/summary"})
    @Operation(
        summary = "Analisar gastos por período",
        description = "Resumo para Home e Resumo Mensal do usuário autenticado. Datas inclusivas em formato ISO. "
            + "Retorna total, comparação com o intervalo imediatamente anterior com o mesmo número de dias, "
            + "categorias com valor/percentual e insight da categoria com maior gasto. "
            + "Sem base anterior, percentageChange é zero; sem gastos, categories é vazio e insight é nulo.")
    public ResponseEntity<ApiResponse<ExpenseAnalysisDTO>> getExpenseAnalysis(
            @Valid @ModelAttribute @ParameterObject ExpenseSummaryRequest request) {
        ExpenseAnalysisDTO analysis = expenseService.getExpenseAnalysis(request.getStartDate(), request.getEndDate());
        return ResponseUtil.ok(analysis, "Análise de despesas realizada com sucesso");
    }
    
}

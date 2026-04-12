package com.brandon.finance.account.controller;

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

import com.brandon.finance.account.dto.AccountDTO;
import com.brandon.finance.account.entity.Account.AccountType;
import com.brandon.finance.account.service.AccountService;
import com.brandon.finance.shared.base.response.ApiResponse;
import com.brandon.finance.shared.base.response.ResponseUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Operações relacionadas a contas")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Criar conta", description = "Cria uma nova conta")
    public ResponseEntity<ApiResponse<AccountDTO>> create(@Valid @RequestBody AccountDTO dto) {
        AccountDTO created = accountService.create(dto);
        return ResponseUtil.created(created, "Conta criada com sucesso");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter conta por ID", description = "Retorna uma conta específica")
    public ResponseEntity<ApiResponse<AccountDTO>> getById(@PathVariable Long id) {
        AccountDTO dto = accountService.getById(id);
        if (dto != null) {
            return ResponseUtil.ok(dto, "Conta encontrada com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Listar contas do usuário", description = "Retorna todas as contas de um usuário")
    public ResponseEntity<ApiResponse<Page<AccountDTO>>> getByUserId(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<AccountDTO> accounts = accountService.getByUserId(userId, pageable);
        return ResponseUtil.ok(accounts, "Contas encontradas com sucesso");
    }

    @GetMapping("/user/{userId}/list")
    @Operation(summary = "Listar contas do usuário (sem paginação)", description = "Retorna todas as contas de um usuário sem paginação")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getByUserIdList(
            @PathVariable Long userId) {
        List<AccountDTO> accounts = accountService.getByUserIdList(userId);
        return ResponseUtil.ok(accounts, "Contas encontradas com sucesso");
    }

    @GetMapping("/user/{userId}/type")
    @Operation(summary = "Listar contas por tipo", description = "Retorna contas de um usuário filtradas por tipo")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getByUserIdAndType(
            @PathVariable Long userId,
            @RequestParam AccountType type) {
        List<AccountDTO> accounts = accountService.getByUserIdAndType(userId, type);
        return ResponseUtil.ok(accounts, "Contas encontradas com sucesso");
    }

    @GetMapping("/user/{userId}/name")
    @Operation(summary = "Buscar conta por nome", description = "Retorna uma conta específica pelo nome do usuário")
    public ResponseEntity<ApiResponse<AccountDTO>> getByUserIdAndName(
            @PathVariable Long userId,
            @RequestParam String name) {
        AccountDTO dto = accountService.getByUserIdAndName(userId, name);
        if (dto != null) {
            return ResponseUtil.ok(dto, "Conta encontrada com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar conta", description = "Atualiza uma conta existente")
    public ResponseEntity<ApiResponse<AccountDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody AccountDTO dto) {
        AccountDTO updated = accountService.update(id, dto);
        if (updated != null) {
            return ResponseUtil.ok(updated, "Conta atualizada com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar conta", description = "Remove uma conta")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseUtil.noContent("Conta removida com sucesso");
    }
}

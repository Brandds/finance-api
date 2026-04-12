package com.brandon.finance.category.controller;

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

import com.brandon.finance.category.dto.CategoryDTO;
import com.brandon.finance.category.service.CategoryService;
import com.brandon.finance.shared.base.response.ApiResponse;
import com.brandon.finance.shared.base.response.ResponseUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/categorys")
@RequiredArgsConstructor
@Tag(name = "Categorys", description = "Operações relacionadas a categorias")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Criar categoria", description = "Cria uma nova categoria")
    public ResponseEntity<ApiResponse<CategoryDTO>> create(@Valid @RequestBody CategoryDTO dto) {
        CategoryDTO created = categoryService.create(dto);
        return ResponseUtil.created(created, "Categoria criada com sucesso");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter categoria por ID", description = "Retorna uma categoria específica")
    public ResponseEntity<ApiResponse<CategoryDTO>> getById(@PathVariable Long id) {
        CategoryDTO dto = categoryService.getById(id);
        if (dto != null) {
            return ResponseUtil.ok(dto, "Categoria encontrada com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Listar categorias do usuário", description = "Retorna todas as categorias de um usuário")
    public ResponseEntity<ApiResponse<Page<CategoryDTO>>> getByUserId(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<CategoryDTO> categories = categoryService.getByUserId(userId, pageable);
        return ResponseUtil.ok(categories, "Categorias encontradas com sucesso");
    }

    @GetMapping("/user/{userId}/list")
    @Operation(summary = "Listar categorias do usuário (sem paginação)", description = "Retorna todas as categorias de um usuário sem paginação")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getByUserIdList(
            @PathVariable Long userId) {
        List<CategoryDTO> categories = categoryService.getByUserIdList(userId);
        return ResponseUtil.ok(categories, "Categorias encontradas com sucesso");
    }

    @GetMapping("/user/{userId}/name")
    @Operation(summary = "Buscar categoria por nome", description = "Retorna uma categoria específica pelo nome do usuário")
    public ResponseEntity<ApiResponse<CategoryDTO>> getByUserIdAndName(
            @PathVariable Long userId,
            @RequestParam String name) {
        CategoryDTO dto = categoryService.getByUserIdAndName(userId, name);
        if (dto != null) {
            return ResponseUtil.ok(dto, "Categoria encontrada com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria", description = "Atualiza uma categoria existente")
    public ResponseEntity<ApiResponse<CategoryDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO dto) {
        CategoryDTO updated = categoryService.update(id, dto);
        if (updated != null) {
            return ResponseUtil.ok(updated, "Categoria atualizada com sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar categoria", description = "Remove uma categoria")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseUtil.noContent("Categoria removida com sucesso");
    }
}

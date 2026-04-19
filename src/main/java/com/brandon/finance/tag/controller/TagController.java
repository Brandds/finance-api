package com.brandon.finance.tag.controller;


import com.brandon.finance.tag.dto.TagDTO;
import com.brandon.finance.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.brandon.finance.shared.base.response.ApiResponse;
import com.brandon.finance.shared.base.response.ResponseUtil;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@Tag(name = "Tag", description = "Endpoints para gerenciar tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    @Operation(summary = "Criar nova tag", description = "Cria uma nova tag para o usuário autenticado")
    public ResponseEntity<ApiResponse<TagDTO>> create(@RequestBody TagDTO dto, @RequestParam Long userId) {
        TagDTO created = tagService.create(dto, userId);
        return ResponseUtil.created(created, "Tag created successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tag por ID", description = "Retorna os detalhes de uma tag específica")
    public ResponseEntity<ApiResponse<TagDTO>> getById(@PathVariable Long id) {
        TagDTO tag = tagService.getById(id);
        return ResponseUtil.ok(tag, "Tag retrieved successfully");
    }

    @GetMapping
    @Operation(summary = "Listar tags paginado", description = "Retorna todas as tags do usuário (paginado)")
    public ResponseEntity<ApiResponse<Page<TagDTO>>> getByUserId(@RequestParam Long userId, Pageable pageable) {
        Page<TagDTO> tags = tagService.getByUserId(userId, pageable);
        return ResponseUtil.ok(tags, "Tags retrieved successfully");
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Listar todas as tags do usuário", description = "Retorna todas as tags sem paginação")
    public ResponseEntity<ApiResponse<List<TagDTO>>> getByUserIdList(@PathVariable Long userId) {
        List<TagDTO> tags = tagService.getByUserIdList(userId);
        return ResponseUtil.ok(tags, "Tags retrieved successfully");
    }

    @GetMapping("/search/name")
    @Operation(summary = "Buscar tag por nome", description = "Retorna uma tag específica pelo nome do usuário")
    public ResponseEntity<ApiResponse<TagDTO>> getByName(@RequestParam Long userId, @RequestParam String name) {
        TagDTO tag = tagService.getByName(userId, name);
        return ResponseUtil.ok(tag, "Tag retrieved successfully");
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar tags por nome parcial", description = "Busca tags que contenham o texto fornecido no nome")
    public ResponseEntity<ApiResponse<List<TagDTO>>> searchByName(@RequestParam Long userId, @RequestParam String search) {
        List<TagDTO> tags = tagService.searchByName(userId, search);
        return ResponseUtil.ok(tags, "Tags retrieved successfully");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tag", description = "Atualiza os dados de uma tag existente")
    public ResponseEntity<ApiResponse<TagDTO>> update(@PathVariable Long id, @RequestBody TagDTO dto, @RequestParam Long userId) {
        TagDTO updated = tagService.update(id, dto, userId);
        return ResponseUtil.ok(updated, "Tag updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar tag", description = "Remove uma tag existente")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, @RequestParam Long userId) {
        tagService.delete(id, userId);
        return ResponseUtil.noContent("Tag deleted successfully");
    }
}

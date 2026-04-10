package com.brandon.finance.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brandon.finance.shared.base.response.ApiResponse;
import com.brandon.finance.shared.base.response.ResponseUtil;
import com.brandon.finance.user.request.CreateUserRequest;
import com.brandon.finance.user.response.UserResponse;
import com.brandon.finance.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Operações relacionadas a usuários")
public class UserController {
    
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Criar novo usuário", description = "Registra um novo usuário no sistema")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuário criado com sucesso",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Usuário já existe")
    })
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @RequestBody @Valid CreateUserRequest request) {
        UserResponse response = userService.create(request);
        return ResponseUtil.ok(response, "Usuario criado com sucesso");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter usuário por ID", description = "Retorna os dados de um usuário específico")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuário encontrado",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseUtil.ok(response, "Usuario encontrado com sucesso");
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos os usuários", description = "Retorna lista de todos os usuários")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de usuários retornada",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> response = userService.getAllUsers();
        return ResponseUtil.ok(response, "Usuarios encontrados com sucesso");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário específico")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long id,
            @RequestBody @Valid CreateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseUtil.ok(response, "Usuario atualizado com sucesso");
    }

}

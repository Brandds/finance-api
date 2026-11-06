package com.brandon.finance.notification.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brandon.finance.notification.dto.NotificationDTO;
import com.brandon.finance.notification.service.NotificationService;
import com.brandon.finance.shared.base.response.ApiResponse;
import com.brandon.finance.shared.base.response.ResponseUtil;
import com.brandon.finance.shared.constants.ApiPaths;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.NOTIFICATIONS)
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notificações do usuário autenticado")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Listar minhas notificações", description = "Retorna página de notificações próprias, das mais recentes para as mais antigas por padrão")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getByUser(
            @ParameterObject @PageableDefault(size = 20, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseUtil.ok(notificationService.getByUser(pageable), "Notificações encontradas com sucesso");
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Contar notificações não lidas", description = "Retorna o número de notificações próprias ainda não lidas")
    public ResponseEntity<ApiResponse<Long>> countUnread() {
        return ResponseUtil.ok(notificationService.countUnread(), "Contador de notificações não lidas");
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar notificação como lida", description = "Operação idempotente, permitida apenas para o dono da notificação")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notificação lida"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notificação inexistente ou de outro usuário")
    })
    public ResponseEntity<ApiResponse<NotificationDTO>> markAsRead(@PathVariable Long id) {
        return ResponseUtil.ok(notificationService.markAsRead(id), "Notificação marcada como lida");
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Marcar todas as minhas notificações como lidas", description = "Atualiza somente as notificações próprias ainda não lidas")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseUtil.ok(null, "Notificações marcadas como lidas");
    }
}

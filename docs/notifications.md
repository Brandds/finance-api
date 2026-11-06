# Notificações — Issue #3

Todas as rotas abaixo exigem `Authorization: Bearer <JWT>` e usam exclusivamente
o usuário autenticado. O cliente não escolhe o destinatário por parâmetro.
Os endpoints também estão documentados no Swagger, na tag `Notifications`.

| Método | Rota | `data` da resposta |
|---|---|---|
| GET | `/api/v1/notifications?page=0&size=20` | Página com `content`, `totalElements`, `totalPages`, `number`, `size` e demais metadados de paginação existentes |
| GET | `/api/v1/notifications/unread-count` | Número inteiro de notificações não lidas |
| PATCH | `/api/v1/notifications/{id}/read` | Notificação atualizada |
| PATCH | `/api/v1/notifications/read-all` | `null`; todas as notificações próprias não lidas são atualizadas |

As respostas mantêm o envelope `{"data": ..., "message": "..."}`.
A listagem usa `createdAt DESC, id DESC` por padrão e aceita os parâmetros
habituais de `Pageable`, inclusive `sort`.

Uma notificação contém `id`, `type`, `title`, `message`, `read`, `createdAt` e
`readAt`. Os tipos disponíveis são `BUDGET_ALERT` e `SECURITY_ALERT`. Uma nova
notificação começa com `read = false` e `readAt = null`.

Marcar como lida novamente preserva a data de leitura. Marcar todas atualiza
somente as notificações próprias ainda não lidas. Após qualquer operação de
leitura, consulte `/unread-count` para atualizar o contador da interface.

Notificações inexistentes ou de outro usuário retornam HTTP 404 com
`RESOURCE_NOT_FOUND`, evitando revelar a existência de dados alheios. Ausência
de autenticação retorna HTTP 401. IDs numéricos menores que 1 retornam HTTP 400
com `VALIDATION_ERROR`.

## Publicação de eventos internos

Serviços de orçamento e segurança podem injetar `ApplicationEventPublisher`
e publicar `NotificationRequestedEvent`:

```java
publisher.publishEvent(new NotificationRequestedEvent(
    userId,
    NotificationType.BUDGET_ALERT,
    "Orçamento próximo do limite",
    "Confira seu orçamento mensal."
));
```

Para alertas de segurança, use `NotificationType.SECURITY_ALERT`. O usuário
destinatário precisa existir; o título aceita até 150 caracteres e a mensagem
até 1000. Ambos são obrigatórios. As mensagens devem conter apenas conteúdo
adequado para exibição ao usuário.

O listener é síncrono. Quando o evento é publicado dentro de uma transação,
a notificação participa da mesma transação e acompanha seu commit ou rollback.
Fora de uma transação, o serviço de notificações abre a própria transação.
Os eventos estão preparados para os produtores desses domínios; limiares de
orçamento, rotinas automáticas e integrações push não são definidos nesta Issue.

## Banco e validação

O changeset `001-create-notification`, incluído pelo `changelog-03.xml`, cria a
tabela, vínculo com o usuário, restrições de tipo/leitura e índices por usuário
para paginação e contagem de não lidas. Deve ser aplicado pelo Liquibase ao
subir a aplicação.

Testes focados:

```bash
./mvnw -Dtest=NotificationServiceTest,NotificationWebTest,NotificationEventListenerTest,NotificationMigrationTest test
```

`NotificationRepositoryTest` valida persistência, contador, isolamento entre
usuários e publicação de eventos no PostgreSQL. Segue o ambiente descartável
existente: `TEST_DATABASE_URL=jdbc:postgresql://<host>:<porta>/finance_contracts_test`,
usuário `contracts_test` e senha de teste `test-only-password`. O teste recria
o schema com Hibernate e não deve apontar para um banco com dados reais.
O SQL registrado no changelog é verificado separadamente por `NotificationMigrationTest`.

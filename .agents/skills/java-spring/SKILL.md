---
name: java-spring
description: Implementar ou alterar funcionalidades da API Java/Spring deste repositório seguindo seus fluxos e contratos existentes. Use para pedidos de funcionalidade; para diagnóstico sem solução conhecida, use bug-investigation.
---

# Implementação Java/Spring

Use esta skill quando a tarefa pedir um endpoint, regra de negócio, persistência ou integração nova ou alterada.

- Encontre um fluxo análogo no mesmo domínio e identifique seu contrato HTTP, DTO, mapper, serviço, repositório e entidade antes de editar.
- Defina os arquivos estritamente necessários. Preserve formato de `ApiResponse`, códigos de erro, validação e transações observados no fluxo equivalente.
- Se houver persistência nova, modele a mudança em um novo changeset Liquibase e confira sua inclusão no changelog mestre.
- Verifique o comportamento com testes proporcionais à mudança e relate qualquer diferença de contrato ou requisito ainda ambíguo.

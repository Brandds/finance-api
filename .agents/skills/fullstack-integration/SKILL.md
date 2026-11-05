---
name: fullstack-integration
description: Coordenar uma Issue que exige análise ou alterações integradas nos repositórios de front-end e back-end do workspace. Use para fluxos que atravessam a interface e a API; não use para tarefas restritas a um único repositório.
---

# Integração front-end e back-end

Conduza a Issue em duas fases: análise e plano; depois, somente com aprovação explícita do usuário para o plano, implementação e validação.

## Análise e plano

1. Identifique a Issue e seus critérios de aceite a partir do contexto fornecido ou de uma fonte acessível. Se a Issue não puder ser identificada, peça o identificador ou a descrição necessária. Registre ambiguidades que afetem o contrato ou o escopo.
2. Localize os repositórios envolvidos e leia o `AGENTS.md` aplicável em cada um. Confira o estado do Git antes de planejar branches ou edições; preserve alterações locais. Busque primeiro os fluxos relacionados à Issue e reutilize os achados ao longo da tarefa.
3. Trace o fluxo entre interface e API: endpoints e métodos HTTP, DTOs, formato dos payloads e respostas, validações, enums, autenticação/autorização e mensagens/códigos de erro. Compare o que cada lado envia, aceita e exibe; identifique ausências e incompatibilidades com evidência nos arquivos ou na especificação.
4. Avalie a compatibilidade de qualquer mudança em contrato público e os consumidores conhecidos. Não inclua regras legadas fora dos critérios de aceite.
5. Apresente um plano por repositório com arquivos ou áreas afetadas, mudanças propostas, impacto no contrato, testes essenciais e dependências entre os lados. Indique quais repositórios não precisam de alteração e as pendências que exigem decisão. Aguarde a aprovação explícita do plano antes de editar código-fonte.

## Após a aprovação do plano

- Crie uma branch independente somente em cada repositório que precise de alterações, a partir da base adequada. Confira nomes e estado local antes da criação; se a troca de branch afetar trabalho existente, preserve-o em um worktree ou resolva o conflito de forma não destrutiva.
- Implemente o escopo aprovado em cada repositório, seguindo seu `AGENTS.md`. Se surgir uma mudança material de escopo ou contrato, atualize o plano e obtenha aprovação antes de executá-la.
- Acrescente testes essenciais para o comportamento alterado. Execute primeiro os testes pertinentes de cada repositório e valide a integração entre payloads, respostas e estados de erro quando o ambiente permitir. Registre verificações inviáveis e o motivo.
- Entregue um resumo por repositório: branch, alterações, testes executados e resultados, compatibilidade e pendências. Não faça commit, push ou merge sem autorização explícita; não descarte alterações locais.

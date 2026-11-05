---
name: issue-workflow
description: Gerenciar a implementação de Issues neste repositório, da análise à validação final, preservando o contexto local, os contratos existentes e as autorizações do usuário.
---

# Issue Workflow

Use esta Skill quando o usuário pedir para implementar, corrigir ou validar uma Issue deste repositório. Ela pode ser invocada automaticamente ou explicitamente como `$issue-workflow`.

## Regras de escopo e segurança

- Leia o `AGENTS.md` aplicável antes de alterar qualquer arquivo e siga as Skills de domínio disponíveis.
- Consulte a Issue pelo provedor configurado somente quando houver conexão e autorização. Se o acesso não existir ou falhar, peça a descrição, o número, o título e os critérios de aceite; não invente requisitos.
- Antes de trabalhar, inspecione o estado do repositório e as alterações locais com comandos somente de leitura. Preserve alterações existentes e não as misture à Issue sem confirmação.
- Não execute `reset`, `clean`, force push ou qualquer descarte de alterações. Não execute commit, push, merge ou rebase sem autorização explícita.
- Não instale dependências sem autorização. Não modifique `AGENTS.md` nem arquivos fora do escopo identificado.

## Fluxo

1. **Analisar.** Registre número, título, critérios de aceite, origem dos requisitos, estado Git, branch atual e arquivos/funcionalidades potencialmente afetados. Leia apenas os arquivos relevantes; use buscas direcionadas e reutilize descobertas.
2. **Planejar.** Classifique a complexidade como baixa, média ou alta e o risco como baixo, médio ou alto. Para mudanças médias/altas, apresente um plano curto com impactos, dependências, validações e alternativa mínima. Pare e solicite aprovação antes de ações de alto risco, fora do escopo ou com mudança de contrato, banco, segurança ou comportamento público.
3. **Preparar o Git.** Identifique a branch base pela configuração e pelo histórico do repositório; nunca presuma `main` ou `develop`. Se a implementação foi autorizada, crie uma branch exclusiva no padrão `feat/<numero>-<descricao>`, `fix/<numero>-<descricao>` ou `refactor/<numero>-<descricao>`, usando slug curto e seguro. Se não houver autorização para criar branch, permaneça na branch atual e informe isso.
4. **Implementar.** Faça alterações incrementais e mínimas, respeitando a arquitetura, os contratos e os padrões existentes. Reutilize componentes, serviços, mappers e utilitários antes de criar abstrações. Separe regras de negócio, acesso a dados, apresentação e testes conforme a organização do projeto.
5. **Selecionar Skills.** Use apenas Skills relevantes: `frontend-debugging` para bugs reproduzíveis; `frontend-implementation` para alterações de frontend; `form-validation` para formulários ou schemas; `frontend-testing` para testes de frontend. Para outros domínios, use a Skill disponível correspondente ou siga o `AGENTS.md`; não carregue Skills desnecessárias.
6. **Validar.** Execute primeiro as verificações mais próximas da alteração. Confira TypeScript quando aplicável, testes, lint/build existentes, critérios de aceite e regressões plausíveis. Informe comandos não executados e o motivo. Não trate uma validação impossível como sucesso.

## Branch e autorização

Criar uma branch é uma mudança de estado: confirme que a ação está autorizada no pedido atual. Antes de criá-la, verifique se o nome já existe e se a branch base está limpa o suficiente para a operação; nunca force a troca ou descarte arquivos. Se a base correta não puder ser determinada, solicite orientação.

## Entrega

Finalize com um resumo objetivo contendo:

- Issue (número, título e origem) e branch/base usadas;
- arquivos alterados e o propósito de cada grupo;
- resumo da implementação;
- testes/verificações executados e não executados;
- riscos, pendências e autorizações ainda necessárias;
- sugestão de mensagem de commit, sem criar o commit;
- status de cada critério de aceite: atendido, não atendido ou não verificável, com evidência curta.

Em uma Issue real, aplique o fluxo acima somente até o limite autorizado pelo usuário. Para uma análise sem pedido de implementação, não crie branch nem altere arquivos; entregue diagnóstico, plano e validações propostas.

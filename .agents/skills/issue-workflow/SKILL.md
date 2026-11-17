---
name: issue-workflow
description: Gerenciar a implementação de Issues neste repositório, da análise e planejamento à validação final, preservando alterações locais, o AGENTS.md e os limites de autorização.
metadata:
  short-description: Padronizar o ciclo de implementação de Issues
---

# Workflow de Issues

Use quando o usuário pedir para implementar, corrigir ou concluir uma Issue deste repositório. Também pode ser invocada explicitamente com `$issue-workflow`.

## Limites e contexto

- Leia o `AGENTS.md` aplicável antes de agir e siga suas regras como autoridade local.
- Consulte a Issue pelo provedor configurado somente quando houver integração e acesso autorizado. Não invente número, título, critérios de aceite ou conteúdo.
- Se não houver acesso à Issue, peça ao usuário sua descrição, número, critérios de aceite e contexto suficiente para continuar.
- Preserve alterações locais existentes. Comece por `git status --short` e investigue apenas os arquivos relevantes.
- Não instale dependências, não altere o `AGENTS.md` e não crie arquivos fora do escopo da Issue.
- Não execute `git reset`, `git clean`, `git checkout` para descartar trabalho, `git push --force` ou qualquer ação destrutiva.

## 1. Analisar a Issue

Registre:

- número, título e descrição;
- critérios de aceite e casos-limite;
- tipo provável: bug, feature, refatoração ou manutenção;
- funcionalidades, rotas, componentes, hooks, serviços, schemas, tipos e documentação diretamente afetados;
- riscos, dependências e alterações locais que não pertencem à Issue.

Use `rg` e leitura direcionada; não faça varredura completa do repositório sem necessidade. Reaproveite descobertas já obtidas durante a execução.

### Dependências e implementações existentes

- Identifique se a Issue depende de alterações em outros repositórios.
  Caso exista dependência, registre os contratos necessários e informe
  o usuário. Não altere outro repositório automaticamente.

- Verifique se a funcionalidade já existe total ou parcialmente na
  branch base ou em Pull Requests relacionados. Não duplique
  implementações existentes.

- Caso encontre sobreposição com outras Issues, registre o conflito
  e proponha uma solução antes de implementar.

## 2. Planejar

Classifique a complexidade e o risco como baixo, médio ou alto, considerando impacto em API, autenticação, persistência, navegação, múltiplas features e migrações.

- Para baixo risco, prossiga com um plano curto e verificável.
- Para médio risco, apresente arquivos, sequência de mudanças e validações antes de editar quando isso melhorar a segurança.
- Para alto risco, mudança fora do escopo, contrato público ou impacto irreversível, pare e solicite aprovação explícita antes de implementar.
- Priorize a menor alteração compatível com os critérios de aceite; não inclua refatorações oportunistas.
- Quando a Issue exigir alterações em outro repositório,
  separe as responsabilidades por projeto e identifique
  a ordem de implementação.

- Não considere a Issue concluída apenas porque o código
  local foi implementado, caso existam critérios de aceite
  dependentes de integrações ainda não validadas.

- Se uma dependência externa bloquear um critério de aceite,
  registre o status como parcial ou bloqueado e informe
  o usuário antes de finalizar.

## 3. Preparar o Git

Antes de editar:

1. Verifique branch atual, estado do repositório e alterações locais.
2. Identifique a branch base correta a partir do contexto do repositório, documentação ou provedor; nunca presuma `main` ou `develop`.
3. Se a execução for de implementação e a criação de branch estiver autorizada pelo pedido, crie uma branch exclusiva com o formato `feat/<numero>-<descricao>`, `fix/<numero>-<descricao>` ou `refactor/<numero>-<descricao>`.
4. Normalize `<descricao>` em slug curto, minúsculo, sem espaços ou caracteres ambíguos.

Não crie branch durante uma análise, planejamento ou revisão que não peça implementação. Não faça commit, push, merge ou rebase sem autorização explícita.

## 4. Implementar

Selecione somente as Skills necessárias:

- `frontend-debugging` para investigar bugs e causas-raiz;
- `frontend-implementation` para alterações em telas, componentes, hooks, serviços, navegação ou integrações;
- `form-validation` para formulários, React Hook Form, schemas Zod e submissões;
- `frontend-testing` para testes e proteção contra regressões.

Respeite a arquitetura por feature, os componentes e tokens existentes, o alias `@/`, `src/services/api.ts`, as rotas tipadas e as stores já adotadas. Preserve estados de loading, erro, vazio, sucesso, sessão expirada, permissões e temas quando aplicável. Não duplique autenticação nem mova lógica sem necessidade.

## 5. Validar

Depois de implementar:

- execute as verificações existentes e a checagem TypeScript compatível;
- verifique cada critério de aceite e pelo menos um caminho adjacente com risco de regressão;
- para mudanças de UI, navegação, teclado ou responsividade, faça validação visual quando possível;
- para API/autenticação, verifique sucesso, erro, sessão expirada, payload, resposta e persistência;
- não instale dependências sem autorização;
- informe claramente testes não executados, falhas preexistentes e limitações de ambiente.

## 6. Finalizar

Entregue um resumo objetivo contendo:

- Issue, título e branch;
- arquivos alterados e comportamento implementado;
- verificações e testes executados, com resultados;
- critérios de aceite atendidos, parciais ou não verificados;
- riscos, pendências e limitações;
- sugestão de mensagem de commit, sem executar o commit;
- status final da Issue, sem fechá-la ou alterar o provedor sem autorização.
- Dependências externas: contratos necessários, funcionalidades
  pendentes em outros repositórios e compatibilidade com
  consumidores existentes.


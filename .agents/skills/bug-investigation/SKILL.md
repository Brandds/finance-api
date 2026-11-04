---
name: bug-investigation
description: Investigar e corrigir falhas reproduzíveis da API com evidência de causa raiz e alteração mínima. Use para bugs, regressões ou erros observados; não acione para funcionalidade nova sem falha identificada.
---

# Investigação de bugs

Use esta skill quando houver sintoma, erro, resultado incorreto ou regressão.

- Registre comportamento esperado e observado, entrada e condição que reproduz a falha. Se faltarem dados, comece pelo caminho de execução indicado pelo sintoma.
- Siga o fluxo afetado até localizar a primeira divergência; confronte a hipótese com código, logs ou um teste que falha. Diferencie causa de efeito antes de corrigir.
- Aplique a menor correção no ponto responsável e adicione um teste de regressão quando ele demonstrar a falha sem infraestrutura desnecessária.
- Reexecute a reprodução e os testes afetados. Na entrega, indique causa, evidência, correção e limites da verificação; se a causa não foi confirmada, deixe isso explícito.

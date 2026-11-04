---
name: code-review
description: Revisar diffs da API Java/Spring para encontrar regressões, falhas de autorização ou exposição de dados e problemas concretos de performance. Use em pedidos explícitos de revisão; não acione automaticamente para toda edição.
---

# Revisão de código

Use esta skill quando o usuário solicitar revisão de alterações, commit ou pull request.

- Leia o diff e o contexto imediato dos métodos afetados; compare contratos, chamadas e consultas anteriores para identificar consequências verificáveis.
- Priorize riscos de acesso entre usuários, autenticação, validação, vazamento de dados, migrações incompatíveis e consultas repetidas ou sem paginação quando houver impacto concreto.
- Para cada achado, informe severidade, arquivo/linha, cenário que dispara o problema e consequência. Separe fatos de hipóteses e não liste preferências de estilo como defeitos.
- Se não houver achados, diga isso e aponte somente lacunas relevantes de teste ou verificação que limitem a confiança da revisão.

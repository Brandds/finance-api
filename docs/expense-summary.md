# Resumo de despesas para Home e Resumo Mensal

Implementa a [issue #1](https://github.com/Brandds/finance-api/issues/1).

`GET /api/v1/expenses/summary?startDate=2026-03-01&endDate=2026-03-31`

Exige `Authorization: Bearer <token>`. As datas são obrigatórias, inclusivas e
seguem o formato ISO `yyyy-MM-dd`. Para o Resumo Mensal, envie o primeiro e o
último dia do mês; a Home pode enviar qualquer intervalo. Não há parâmetro de
usuário: ambas as consultas usam exclusivamente o usuário autenticado.

A rota existente `/api/v1/expenses/get-expense-analysis` continua disponível
como alias, com os três campos originais e os campos adicionais abaixo.
O período anterior agora tem a mesma duração em dias, corrigindo a comparação
anterior que simplesmente subtraía um mês de cada limite.

Exemplo do campo `data` do envelope `ApiResponse` (mensagem:
`Análise de despesas realizada com sucesso`):

```json
{
  "totalSpent": 100.00,
  "previousPeriodTotal": 80.00,
  "percentageChange": 25.00,
  "startDate": "2026-03-01",
  "endDate": "2026-03-31",
  "previousStartDate": "2026-01-29",
  "previousEndDate": "2026-02-28",
  "categories": [
    {"categoryId": 1, "categoryName": "Alimentação", "amount": 75.00, "percentage": 75.00},
    {"categoryId": null, "categoryName": "Sem categoria", "amount": 25.00, "percentage": 25.00}
  ],
  "insight": {
    "type": "LARGEST_CATEGORY",
    "categoryId": 1,
    "categoryName": "Alimentação",
    "amount": 75.00,
    "percentage": 75.00
  }
}
```

- O intervalo anterior termina no dia anterior a `startDate` e contém o mesmo
  número de dias inclusivos. Março/2026 tem 31 dias: a comparação é com
  29/janeiro a 28/fevereiro, e não apenas com fevereiro.
- Variação: `(totalSpent - previousPeriodTotal) / previousPeriodTotal * 100`,
  com duas casas decimais e arredondamento `HALF_UP`. Se a base anterior for
  zero, retorna zero por convenção existente, sem inventar uma taxa de crescimento.
- Categorias são agrupadas por ID e ordenadas por valor decrescente; empates
  usam nome e ID. Sem categoria, ou com referência legada a uma categoria de
  outro usuário, o gasto entra em `Sem categoria`, sem expor dados alheios.
- Percentual: `amount / totalSpent * 100`, com duas casas decimais, `HALF_UP`.
  A soma fica aproximadamente em 100% para gastos positivos (três parcelas iguais
  dão 99,99%). Totais zero retornam percentuais zero, sem divisão por zero.
- `LARGEST_CATEGORY` representa a maior categoria do período, com os mesmos
  dados da primeira categoria da lista; empates seguem a ordenação documentada.
  Sem total positivo, `insight` é nulo.
- Sem despesas: `totalSpent = 0`, `categories = []`, `insight = null`.
  Se havia gastos antes, `percentageChange = -100`; ambos vazios retornam zero.
- Datas ausentes, inválidas, invertidas ou cujo intervalo anterior não pode
  ser representado retornam `400 / VALIDATION_ERROR`; ausência de autenticação,
  `401`. Nenhuma estrutura de banco nova é necessária.

Validação focada:

```sh
./mvnw -Dtest=ExpenseSummaryServiceTest,ExpenseSummaryWebTest,ExpenseSummaryQueryTest,ExpenseServiceTest test
```

`ExpenseSummaryQueryTest` valida as consultas no Hibernate real usando o dialeto
PostgreSQL sem conexão ao banco. A execução das agregações também é coberta por
`ExpenseFilterRepositoryTest`, que
exige PostgreSQL **descartável** chamado `finance_contracts_test`, indicado por
`TEST_DATABASE_URL`, com usuário `contracts_test` e senha `test-only-password`.
Esse teste usa `create-drop` e recria o esquema; não aponte para banco de trabalho.

```sh
./mvnw -Dtest=ExpenseFilterRepositoryTest test
```

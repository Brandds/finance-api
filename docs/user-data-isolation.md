# Isolamento de dados por usuário

Implementação da [issue #5](https://github.com/Brandds/finance-api/issues/5).

Todos os recursos financeiros usam a identidade do JWT, obtida por
`AuthenticatedUserService`. O cliente não escolhe o proprietário dos dados.

| Recurso | Leitura, alteração e exclusão por ID | Criação e associações |
| --- | --- | --- |
| Contas | `findByIdAndUserId` | Usuário definido no servidor |
| Categorias | `findByIdAndUserId` | Usuário definido no servidor |
| Despesas | `findByIdAndUserId` | Usuário do servidor; conta/categoria, quando informadas, precisam ser próprias |
| Orçamentos | `findByIdAndUserId` | Usuário do servidor; categoria precisa ser própria |
| Transações | `findByIdAndUserId` | Usuário do servidor; conta/categoria, quando informadas, precisam ser próprias |
| Tags | `findByIdAndUserId` | Usuário do servidor; listas e buscas também usam exclusivamente o JWT |
| Vínculos despesa–tag | Filtros pelos donos da despesa e da tag | Os dois recursos precisam pertencer ao usuário autenticado |
| Notificações | Já usam ID e usuário do JWT | Criação interna por eventos; não existe criação pública |

Regras HTTP:

- Recursos financeiros inexistentes e pertencentes a outro usuário retornam
  igualmente `404 / RESOURCE_NOT_FOUND`, com o envelope `ApiError` existente.
- Exclusões buscam primeiro a entidade por ID e proprietário e só então a removem.
- `userId` enviado no corpo ou query string é ignorado. Nas rotas de tags,
  deixou de ser obrigatório; URLs e formatos de resposta continuam os mesmos.
- Um ID de conta/categoria alheia durante criação retorna 404 antes de salvar.
- A atualização preserva o proprietário e as associações existentes; IDs enviados
  no DTO não transferem o registro nem trocam suas associações.
- A consulta de vínculos por despesa/tag retorna 404 se o recurso pai não é próprio.
  Os resultados e os totais paginados exigem que ambos os lados sejam próprios,
  inclusive para vínculos legados inconsistentes.
- Referências legadas a categorias/contas alheias são omitidas dos DTOs de despesas,
  orçamentos e transações. O resumo agregado trata categorias alheias como
  `Sem categoria`. Não há migração ou remoção dos registros legados.
- Consultas globais de auditoria ficam restritas a `ADMIN` no serviço, com
  `403 / FORBIDDEN` para usuários comuns. A auditoria não tem proprietário próprio
  e inclui snapshots de diferentes domínios; não deve ser uma forma alternativa
  de consultar dados de terceiros.
- Requisições sem autenticação continuam retornando 401. As regras existentes
  de perfil do usuário, autenticação e listagem administrativa permanecem aplicáveis.

Não são necessárias novas dependências ou alterações de esquema.

Validação focada:

```sh
./mvnw -Dtest=FinancialResourceIsolationTest,FinancialIsolationWebTest,LegacyReferenceMapperTest,ExpenseSummaryQueryTest test
```

Os testes de serviço e HTTP usam serviços/mappers reais e repositórios simulados,
verificando o usuário nas consultas, a ausência de gravação após bloqueio,
o acesso legítimo e os códigos HTTP. `ExpenseSummaryQueryTest` constrói os sete
repositórios no Hibernate com dialeto PostgreSQL para validar as consultas sem
conectar ao banco.

`FinancialIsolationRepositoryTest` verifica as buscas por proprietário e os
vínculos legados cruzados em PostgreSQL real. Ele só é habilitado quando
`TEST_DATABASE_URL` aponta para um banco **descartável** chamado
`finance_contracts_test`, com usuário `contracts_test` e senha `test-only-password`.
Esse teste usa `create-drop`, recriando o esquema: nunca use o banco de trabalho.

```sh
./mvnw -Dtest=FinancialIsolationRepositoryTest test
```

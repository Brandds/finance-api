---
name: unit-tests
description: Criar ou ajustar testes unitários focados para classes Java deste projeto com JUnit Jupiter e Mockito. Use quando o pedido envolver testes de lógica isolável; para testes de contexto ou banco, escolha a abordagem necessária ao comportamento.
---

# Testes unitários

Use esta skill para testar regras de serviço, mappers ou outras classes que possam ser executadas sem subir o contexto Spring.

- O `spring-boot-starter-test` do projeto gerencia JUnit Jupiter 5.12.2 e Mockito 5.17.0; não fixe versões nem adicione dependências para esses testes.
- Escolha poucos casos que distingam sucesso, limite e erro relevante. Verifique saídas e efeitos observáveis, evitando testes que apenas repetem o código ou dependem da ordem interna de chamadas.
- Instancie a classe sob teste com dependências simuladas; use Mockito somente nas fronteiras necessárias. Evite `@SpringBootTest` para lógica isolada.
- Execute a classe de teste com `./mvnw -Dtest=NomeDoTeste test` e registre o resultado; se o caso exigir banco, filtros ou configuração Spring, trate-o como teste de integração e justifique o escopo.

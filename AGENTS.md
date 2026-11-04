# Instruções para agentes

## Contexto
- API Maven com Java 21 e Spring Boot 3.5.13 (`pom.xml`); use `./mvnw`.
- Código em `src/main/java/com/brandon/finance`, organizado por domínio (`account`, `expense`, `user`, `auth` etc.) e por `controller`, `service`, `repository`, `entity`, `dto` e `mapper`. Utilitários e respostas comuns ficam em `shared`.
- Dependências principais: Spring Web, Data JPA, Validation, Security, OAuth2, WebFlux, Mail e Thymeleaf; PostgreSQL, Liquibase, JJWT, Lombok e springdoc. Testes usam Spring Boot Test/JUnit.
- Configuração em `src/main/resources/application*.yml`; migrações em `db/changelog` e `db/scripts`. O esquema é gerido pelo Liquibase, com `ddl-auto: none`.

## Implementação
- Faça a menor alteração que resolva a tarefa. Siga o fluxo e os nomes do domínio existente; reutilize serviços, mappers, `ApiResponse`/`ResponseUtil`, `ApiPaths` e tratamento de erros antes de criar abstrações.
- Mantenha regras de negócio nos serviços, acesso a dados nos repositórios e conversão nos mappers; preserve contratos HTTP e transações existentes. Para mudanças no banco, acrescente changesets Liquibase, sem editar migrações já aplicadas.
- Evite novas dependências, refatorações amplas e correções não relacionadas à tarefa.

## Investigação e testes
- Comece pelo comportamento afetado: localize símbolos com `rg`, leia apenas controller, service, repository, mapper, configuração e teste pertinentes. Expanda a busca somente se a evidência exigir; formule e verifique a causa antes de alterar código.
- Para regras alteradas ou bugs, crie testes unitários focados em comportamento e casos de erro com JUnit/Mockito; use testes de contexto ou integração só quando a interação com Spring, segurança ou banco for essencial. Não replique a implementação em testes.
- Execute primeiro os testes relevantes (`./mvnw -Dtest=NomeDoTeste test`); rode a suíte completa (`./mvnw test`) quando o alcance da alteração justificar. Informe testes não executados e motivos; o teste atual de `contextLoads` pode depender de configuração externa e PostgreSQL.

## Segurança e economia de contexto
- Preserve autenticação e autorização de `SecurityConfig`, `JwtFilter` e serviços de auth. Em operações por ID, verifique o vínculo com o usuário autenticado; valide entradas e não exponha tokens, senhas, dados sensíveis ou segredos em código, logs e respostas. Use variáveis de ambiente para credenciais.
- Evite ler árvores inteiras, arquivos gerados (`target`) ou saídas extensas. Prefira buscas delimitadas, trechos relevantes e testes mínimos suficientes; resuma achados e pare de investigar quando houver evidência para a mudança.

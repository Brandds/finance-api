# Alteração e recuperação de senha — issue #8

Todos os endpoints usam POST e o prefixo `/api/v1/auth`. Respostas de sucesso
seguem `ApiResponse`; falhas seguem `ApiError`.

| Endpoint | Autenticação | Corpo JSON |
| --- | --- | --- |
| `/change-password` | Bearer JWT | `{"currentPassword":"senha-atual","newPassword":"nova-senha","confirmPassword":"nova-senha"}` |
| `/forgot-password` | Pública | `{"email":"usuario@example.com"}` |
| `/reset-password` | Pública | `{"token":"token-do-email","newPassword":"nova-senha","confirmPassword":"nova-senha"}` |

Nova senha: 8 a 72 caracteres, no máximo 72 bytes UTF-8 (limite BCrypt),
confirmação igual e diferente da senha atual. As senhas são armazenadas com BCrypt.

A solicitação retorna HTTP 200 com a mesma mensagem para e-mails existentes,
inexistentes, contas desabilitadas e contas sem senha local. Apenas contas habilitadas
com senha local recebem o e-mail. A configuração SMTP existente é reutilizada.
Falhas SMTP são registradas sem credenciais e mantêm a resposta genérica; o usuário
pode solicitar novamente. Não há alteração da senha ao solicitar a recuperação.

O e-mail contém um token aleatório de 256 bits para copiar na tela de recuperação.
Não depende de uma URL de frontend. O banco armazena somente SHA-256 do token,
seu prazo de 15 minutos e o hash da senha vigente na emissão.
Tokens podem coexistir até expirar; cada token é de uso único. Uma troca de senha,
autenticada ou por recuperação, invalida todos os tokens emitidos para a senha anterior.
Redefinições concorrentes são serializadas por bloqueios de banco do token e usuário.
Os JWTs já emitidos continuam válidos até sua expiração atual de uma hora.
A recuperação não habilita contas nem cria senha para contas exclusivamente OAuth.

| Situação | HTTP | Código |
| --- | --- | --- |
| Sem sessão na alteração autenticada | 401 | `UNAUTHORIZED` |
| Senha atual incorreta ou nova senha igual à atual | 400 | `BUSINESS_ERROR` |
| Entrada inválida, confirmação diferente ou limite BCrypt excedido | 400 | `VALIDATION_ERROR` |
| Token desconhecido, usado ou invalidado por troca de senha | 400 | `INVALID_TOKEN` |
| Token não utilizado fora do prazo | 400 | `TOKEN_EXPIRED` |

Aplicar o changelog mestre Liquibase adiciona `password_reset_token` pelo
`changelog-04.xml`. Migrações anteriores permanecem inalteradas.

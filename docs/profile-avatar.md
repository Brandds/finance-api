# Avatar de perfil

| Método | Rota | Resultado |
| --- | --- | --- |
| PUT | `/api/v1/users/me/avatar` | Envia um arquivo na parte multipart `image` e retorna 200 |
| DELETE | `/api/v1/users/me/avatar` | Remove o avatar personalizado do perfil e retorna 200 |
| GET | `/api/v1/users/me` | Retorna `data.displayPicture` com o avatar personalizado, a foto do Google ou `null` |

As rotas exigem o JWT do usuário. A API aceita JPEG, PNG e WebP de até 2 MB por padrão e limita cada dimensão a 2048 pixels. O servidor identifica o formato pelos bytes e decodifica a imagem. JPEG é regravado como JPEG, PNG e WebP são regravados como PNG. A regravação descarta os metadados e mantém apenas o primeiro quadro em imagens animadas que o decodificador aceitar.

O PostgreSQL guarda o identificador em `users.avatar_public_id`. A API monta a URL HTTPS pública do CDN a partir desse identificador. Qualquer pessoa com a URL pode acessar a foto. Este modelo é destinado a avatares públicos. Documentos financeiros exigem um fluxo de armazenamento e acesso privado. [Entrega de imagens no Cloudinary](https://cloudinary.com/documentation/advanced_url_delivery_options)

## Configuração local

1. Crie uma conta **Cloudinary Free** em Programmable Media. O cadastro dispensa cartão. O plano inclui 25 créditos mensais compartilhados entre armazenamento, tráfego e transformações. Acompanhe o consumo no painel de uso do Cloudinary. [Planos e créditos](https://cloudinary.com/documentation/billing_and_plans)
2. Obtenha o Cloud Name, API Key e API Secret no console do ambiente do produto. Configure `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY` e `CLOUDINARY_API_SECRET` conforme `.env.example`. Use credenciais próprias por ambiente. Só o backend envia uploads assinados e pedidos de exclusão. Mantenha presets de upload sem assinatura desabilitados para este fluxo. [Upload com o SDK Java](https://cloudinary.com/documentation/java_image_and_video_upload)
3. O backend gera identificadores no formato `finance-api/avatars/<id-do-usuario>/<uuid>`. Assim, os novos arquivos ficam organizados na pasta `finance-api/avatars`, com uma subpasta por usuário. A barra define o caminho público. Em modo de pastas dinâmicas, esse prefixo é independente da pasta exibida na Media Library.
4. Com o PostgreSQL e as demais variáveis configurados, execute `./mvnw spring-boot:run`. O Liquibase aplica a migração na inicialização. O Compose repassa as variáveis do Cloudinary.
5. Envie uma imagem usando o Swagger ou um cliente HTTP, consulte o perfil e abra a URL retornada. Repita o fluxo após a troca e a remoção para verificar a nova URL e o retorno à imagem do Google.

## Falhas e limpeza

A troca envia a nova imagem antes de alterar a referência. Após o commit, a API exclui a imagem anterior e solicita a invalidação do CDN. A remoção confirma a referência nula antes da exclusão. O perfil muda de imediato após o commit, e a invalidação de URLs antigas pode levar alguns minutos. Em caso de rollback, a API tenta excluir o novo identificador. [Invalidação do CDN](https://cloudinary.com/documentation/invalidate_cached_media_assets_on_the_cdn)

Falhas de armazenamento retornam 503. Se a limpeza falhar após o commit, a alteração já estará visível no perfil. O identificador pendente fica no log operacional. Verifique se ainda existe uma referência em `users.avatar_public_id` antes de repetir a exclusão. Após queda do processo ou commit incerto, compare os identificadores antigos no prefixo `finance-api/avatars/` com as referências no banco e preserve imagens recentes que possam integrar uploads em andamento.

O changeset anterior do R2 permanece no histórico do Liquibase. A aplicação usa `avatar_public_id` para novos avatares. Perfis com uma referência legada do R2 usam o fallback habitual até que o usuário envie outro avatar.

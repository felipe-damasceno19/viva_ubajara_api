# Reivindicação de estabelecimento

Este documento explica, em termos simples, a funcionalidade "Esse estabelecimento é seu?". Ela permite que uma conta empresarial peça para ser a responsável por um restaurante ou hospedagem que já está no site, mas ainda não tem dono.

## Como funciona

1. O restaurante ou a hospedagem aparece nas listagens públicas com o campo `claimable`. Ele é `true` quando o local não tem responsável, está aprovado e está ativo. Nenhum dado do responsável é exposto publicamente.
2. Uma conta empresarial envia a solicitação com uma mensagem explicando por que o local é dela e um telefone de contato.
3. Um administrador analisa a solicitação e aprova ou rejeita (com um motivo opcional).
4. Ao aprovar, a conta passa a ser a responsável pelo local e pode editá-lo pela área da empresa. O local continua aprovado e ativo no momento da aprovação.

A verificação é manual: o administrador avalia o texto e o telefone informados. Não há envio de documento nem aviso por e-mail; a empresa acompanha o andamento pela lista das próprias solicitações.

## Regras

- Só contas com perfil empresarial podem reivindicar. Quem tem conta comum ativa o acesso empresarial antes. Administradores e guias não reivindicam.
- Só restaurantes e hospedagens podem ser reivindicados.
- Cada conta pode ter apenas uma solicitação em análise por vez. Para enviar outra, é preciso cancelar a atual.
- Cada conta pode criar no máximo 5 solicitações em 24 horas. Acima disso a resposta é 429.
- Uma conta que já tem estabelecimento não pode reivindicar outro.
- Enquanto houver solicitação em análise, a conta não consegue cadastrar um novo estabelecimento (resposta 409).
- Ao aprovar uma solicitação, as outras que estavam em análise para o mesmo local são rejeitadas automaticamente.
- Cancelar uma solicitação só muda o status para cancelada; o registro continua no histórico.
- A aprovação confere de novo se o local ainda está sem responsável e se a conta ainda é empresarial e sem estabelecimento.

## Rotas

Empresa (perfil `EMPRESA`):

- `POST /companies/me/claims` cria a solicitação com `spotId`, `message` e `contactPhone`. Responde 201.
- `GET /companies/me/claims` lista as solicitações da própria conta, da mais recente para a mais antiga.
- `POST /companies/me/claims/{id}/cancel` cancela uma solicitação em análise. Responde 204.

Administrador (perfil `ADMIN`):

- `GET /establishment-claims?status=PENDING&page=0&size=10` lista as solicitações (sem `status`, lista todas).
- `GET /establishment-claims/{id}` mostra os detalhes, incluindo dados da conta solicitante e do local.
- `PATCH /establishment-claims/{id}/approve` aprova.
- `PATCH /establishment-claims/{id}/reject` rejeita, com `reason` opcional de até 500 caracteres.

Erros comuns: 400 (local de tipo não reivindicável), 403 (perfil sem permissão), 404 (local ou solicitação inexistente), 409 (local indisponível, solicitação já analisada, conta já com estabelecimento ou solicitação em análise), 422 (dados inválidos) e 429 (limite diário).

## Antes do deploy

O banco de produção usa `ddl-auto=validate`, então a tabela `tb_establishment_claim` precisa existir antes do backend subir. É preciso rodar manualmente, no Supabase, o script `migration_producao_reivindicacao.sql` (arquivo de apoio na pasta `backend`, que não é versionado). Ordem: SQL, depois backend, depois frontend.

O script também cria um índice que garante, no banco, uma única solicitação em análise por conta. Localmente, com `ddl-auto=update`, a tabela é criada pelo Hibernate e esse índice parcial não existe; o serviço faz a mesma checagem.

Se um novo valor for adicionado ao status da solicitação no futuro, a constraint de status do banco precisa ser recriada.

## Pontos de atenção

- Depois de aprovada, a primeira edição feita pelo novo responsável volta o local para "pendente" e o tira do site até nova aprovação. É a regra que já existia na edição pela empresa.
- Desvincular ou transferir o responsável de um local não faz parte desta entrega.
- Mensagem e telefone só aparecem para quem enviou e para administradores. O CNPJ da conta só aparece na visão do administrador.

## Arquivos principais

- `EstablishmentClaim`, `ClaimStatus` e `EstablishmentClaimRepository`: modelo e acesso aos dados.
- `EstablishmentClaimService`: regras de criação, cancelamento, aprovação e rejeição.
- `CompanyController` e `EstablishmentClaimController`: rotas da empresa e do administrador.
- `TouristSpot.isClaimable()` e os DTOs de restaurante e hospedagem: campo `claimable`.
- `ConflictException`, `TooManyRequestsException` e `GlobalExceptionHandler`: respostas 409 e 429.
- `CompanyService`: recusa o cadastro de estabelecimento quando há solicitação em análise.
- `EstablishmentClaimServiceTest`, `EstablishmentClaimSecurityTest` e `ClaimableMappingTest`: testes novos.

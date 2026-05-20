# Parque Ubajara API — Documentação de Integração Frontend

> Versão do documento: 1.0 — gerado em 2026-05-18  
> Baseado na análise estática do código-fonte (branch `main`)

---

## Sumário

1. [Visão Geral da API](#1-visão-geral-da-api)
2. [Autenticação](#2-autenticação)
3. [Fluxo de Login](#3-fluxo-de-login)
4. [Formato Padrão de Respostas](#4-formato-padrão-de-respostas)
5. [Endpoints por Módulo](#5-endpoints-por-módulo)
   - [Auth](#51-auth)
   - [Atrativos (Attractions)](#52-atrativos-attractions)
   - [Pontos Turísticos (Tourist Spots)](#53-pontos-turísticos-tourist-spots)
   - [Restaurantes (Restaurants)](#54-restaurantes-restaurants)
   - [Hospedagens (Host Points)](#55-hospedagens-host-points)
   - [Eventos (Events)](#56-eventos-events)
   - [Guias Turísticos (Tour Guides)](#57-guias-turísticos-tour-guides)
   - [Aeroportos (Airports)](#58-aeroportos-airports)
   - [Contatos (Contacts)](#59-contatos-contacts)
   - [Fotos (Photos)](#510-fotos-photos)
   - [Usuários (Users)](#511-usuários-users)
6. [Paginação](#6-paginação)
7. [Upload de Arquivos (Multipart)](#7-upload-de-arquivos-multipart)
8. [Tratamento de Erros](#8-tratamento-de-erros)
9. [Observações para Consumo no React](#9-observações-para-consumo-no-react)
10. [Endpoints Recomendados / Lacunas Identificadas](#10-endpoints-recomendados--lacunas-identificadas)
11. [Dependências entre Telas e Endpoints](#11-dependências-entre-telas-e-endpoints)

---

## 1. Visão Geral da API

| Propriedade           | Valor                                        |
|-----------------------|----------------------------------------------|
| **Base URL (dev)**    | `http://localhost:8081/api/v1`               |
| **Formato**           | JSON (`application/json`)                    |
| **Autenticação**      | JWT Bearer Token                             |
| **Documentação live** | `http://localhost:8081/api/v1/swagger-ui.html` |
| **API Docs (JSON)**   | `http://localhost:8081/api/v1/v3/api-docs`   |
| **CORS permitidos**   | `localhost:3000`, `localhost:5173`, `localhost:4200` |

> **Todas as requisições** devem usar o prefixo `/api/v1`. Os exemplos abaixo omitem a base URL por brevidade. Exemplo completo: `POST http://localhost:8081/api/v1/auth/login`.

---

## 2. Autenticação

A API usa **JWT Bearer Token** com sessão stateless. Não há cookies de sessão.

### Como enviar o token

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Propriedades do token

| Propriedade   | Valor                    |
|---------------|--------------------------|
| Algoritmo     | HMAC-SHA (JJWT)          |
| Subject       | e-mail do usuário        |
| Expiração     | 24 horas (86400000 ms)   |
| Claims extras | Nenhum (sem roles no token) |

> **Importante:** O token **não embute as roles** do usuário. O backend busca as permissões no banco de dados a cada requisição usando o e-mail extraído do token.

### Roles disponíveis

| Role    | Permissões                                                      |
|---------|-----------------------------------------------------------------|
| `USER`  | Leitura pública (GET) em todos os módulos, acesso ao próprio perfil |
| `ADMIN` | Todas as operações (CRUD completo, upload de fotos)             |

### Login social (Google OAuth2)

O fluxo de autenticação via Google é iniciado pelo **navegador** (não por requisição AJAX):

```
GET http://localhost:8081/api/v1/oauth2/authorization/google
```

Após autenticação bem-sucedida no Google, o backend redireciona para:

```
http://localhost:5173/login-success?token=<jwt_token>
```

O frontend deve ler o parâmetro `token` da URL nessa página e armazená-lo para uso futuro.

> **Atenção:** A URL de redirect (`localhost:5173`) está **hardcoded no código** do backend. Em produção isso precisará ser atualizado.

---

## 3. Fluxo de Login

### Fluxo padrão (e-mail + senha)

```
1. Usuário preenche formulário de login
2. Frontend: POST /auth/login  { email, password }
3. Backend: autentica → gera JWT → retorna { token, email, role }
4. Frontend: armazena o token (localStorage ou sessionStorage)
5. Frontend: inclui "Authorization: Bearer <token>" em todas as requisições protegidas
6. Se resposta 401 → redirecionar para tela de login
```

### Fluxo de registro

```
1. Usuário preenche formulário de cadastro
2. Frontend: POST /auth/register { firstName, lastName, username, email, password }
3. Backend: cria usuário com role USER → gera JWT → retorna { token, email, role }
4. Frontend: armazena token e direciona para área logada
```

### Fluxo OAuth2 (Google)

```
1. Usuário clica em "Entrar com Google"
2. Frontend: redireciona janela para GET /oauth2/authorization/google
3. Google: autentica o usuário → redireciona de volta ao backend
4. Backend: cria/atualiza usuário → gera JWT → redireciona para:
   http://localhost:5173/login-success?token=<jwt>
5. Frontend (página /login-success): extrai ?token= da URL → armazena → redireciona
```

---

## 4. Formato Padrão de Respostas

### Sucesso — objeto único

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Cachoeira do Pimenta",
  ...
}
```

### Sucesso — listagem paginada

```json
{
  "content": [ { ... }, { ... } ],
  "totalElements": 42,
  "totalPages": 5,
  "number": 0,
  "size": 10,
  "first": true,
  "last": false,
  "empty": false
}
```

### Erro de validação (422)

```json
{
  "timestamp": "2026-05-18T10:30:00",
  "status": 422,
  "error": "Erro de validação",
  "fields": [
    { "field": "name", "message": "O nome é obrigatório" },
    { "field": "email", "message": "E-mail inválido" }
  ]
}
```

### Erro padrão (400, 401, 403, 404, 500)

```json
{
  "timestamp": "2026-05-18T10:30:00",
  "status": 404,
  "error": "Não encontrado",
  "message": "Atração de ID: 3fa85f64-... não encontrada",
  "path": "/api/v1/attractions/3fa85f64-..."
}
```

### Erro de autenticação (401 — sem token ou token inválido)

```json
{
  "status": 401,
  "message": "Token inválido ou ausente"
}
```

---

## 5. Endpoints por Módulo

---

### 5.1 Auth

**Base:** `/auth`  
**Autenticação:** Nenhuma (todos os endpoints são públicos)

---

#### `POST /auth/register` — Registrar novo usuário

**Body (JSON):**

```json
{
  "firstName": "João",
  "lastName": "Silva",
  "username": "joaosilva",
  "email": "joao@email.com",
  "password": "senha123"
}
```

**Validações:**

| Campo       | Obrigatório | Regras                                |
|-------------|-------------|---------------------------------------|
| `firstName` | Sim         | 2–20 caracteres                       |
| `lastName`  | Não         | máx. 20 caracteres                    |
| `username`  | Sim         | 2–30 caracteres, único no sistema     |
| `email`     | Sim         | formato e-mail válido, único          |
| `password`  | Sim         | mínimo 6 caracteres                   |

**Resposta (201 Created):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "joao@email.com",
  "role": "USER"
}
```

**Códigos HTTP:**

| Código | Situação                                 |
|--------|------------------------------------------|
| 201    | Usuário criado e token gerado            |
| 400    | E-mail já cadastrado                     |
| 422    | Campos inválidos (validação de entrada)  |

---

#### `POST /auth/login` — Login

**Body (JSON):**

```json
{
  "email": "joao@email.com",
  "password": "senha123"
}
```

**Validações:**

| Campo      | Obrigatório | Regras              |
|------------|-------------|---------------------|
| `email`    | Sim         | formato e-mail      |
| `password` | Sim         | não vazio           |

**Resposta (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "joao@email.com",
  "role": "ADMIN"
}
```

**Códigos HTTP:**

| Código | Situação                       |
|--------|--------------------------------|
| 200    | Login bem-sucedido             |
| 401    | E-mail ou senha incorretos     |
| 422    | Campos obrigatórios ausentes   |

---

### 5.2 Atrativos (Attractions)

**Base:** `/attractions`  
**Modelo de herança:** Estende `TouristSpot` (herança `JOINED`)

> **Bug identificado:** A configuração de segurança do backend libera publicamente o path `/attractives/**` (com typo), quando deveria ser `/attractions/**`. Na prática, **os endpoints GET de atrativos podem exigir autenticação** dependendo de como o servidor estiver configurado. Verificar com o time de backend.

---

#### `GET /attractions` — Listar atrativos

**Parâmetros de query:**

| Parâmetro   | Tipo            | Obrigatório | Descrição                                        |
|-------------|-----------------|-------------|--------------------------------------------------|
| `category`  | `AttractionType`| Não         | Filtrar por categoria (ver enum abaixo)          |
| `page`      | integer         | Não         | Número da página (padrão: 0)                     |
| `size`      | integer         | Não         | Tamanho da página (padrão: 10)                   |
| `sort`      | string          | Não         | Campo de ordenação (padrão: `name`)              |

**Enum `AttractionType`:**

```
PARK | WATERFALL | MUSEUM | FARM | ROUTE | MARKET
```

**Exemplo de request:**

```
GET /attractions?category=WATERFALL&page=0&size=5&sort=name,asc
```

**Resposta (200 OK):** `Page<AttractionResponseDTO>`

```json
{
  "content": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "name": "Cachoeira do Pimenta",
      "description": "Linda cachoeira...",
      "address": "Estrada do Pimenta, km 5",
      "phone": "(88) 99999-0000",
      "email": "cachoeira@parqueubajara.com",
      "webUrl": "https://parqueubajara.com/cachoeira",
      "instagramUrl": "https://instagram.com/cachoeira",
      "active": true,
      "openingHours": "08:00–17:00",
      "entryPrice": 15.00,
      "hasGuide": true,
      "averageVisitDuration": 120,
      "category": "WATERFALL",
      "photos": [
        {
          "id": "uuid",
          "url": "https://parque-ubajara-fotos.s3.sa-east-1.amazonaws.com/chave",
          "description": "Vista aérea",
          "displayOrder": 1,
          "ownerType": "ATTRACTION",
          "ownerId": "3fa85f64-...",
          "createdAt": "2026-05-01T10:30:00"
        }
      ],
      "subAttractions": []
    }
  ],
  "totalElements": 12,
  "totalPages": 2,
  "number": 0,
  "size": 10
}
```

**Códigos HTTP:** `200`

---

#### `GET /attractions/{id}` — Buscar atrativo por ID

**Path param:** `id` (UUID)

**Resposta (200 OK):** `AttractionResponseDTO` (mesmo schema acima)

**Códigos HTTP:**

| Código | Situação                |
|--------|-------------------------|
| 200    | Encontrado              |
| 404    | Não encontrado          |

---

#### `POST /attractions` — Criar atrativo _(ADMIN)_

**Auth:** Bearer Token com role `ADMIN`  
**Content-Type:** `application/json`

**Body:**

```json
{
  "name": "Gruta de Ubajara",
  "description": "Gruta calcária com estalactites...",
  "address": "Parque Nacional de Ubajara",
  "phone": "(88) 3634-1388",
  "email": "gruta@parqueubajara.com",
  "webUrl": "https://parqueubajara.gov.br",
  "instagramUrl": "https://instagram.com/parqueubajara",
  "active": true,
  "openingHours": "09:00–16:00",
  "entryPrice": 20.00,
  "hasGuide": true,
  "averageVisitDuration": 90,
  "category": "PARK"
}
```

**Validações:**

| Campo                   | Obrigatório | Regras                                     |
|-------------------------|-------------|---------------------------------------------|
| `name`                  | Sim         | máx. 100 caracteres                         |
| `description`           | Sim         | —                                           |
| `address`               | Sim         | —                                           |
| `phone`                 | Não         | —                                           |
| `email`                 | Não         | formato e-mail válido                       |
| `webUrl`                | Não         | —                                           |
| `instagramUrl`          | Não         | —                                           |
| `active`                | Sim         | boolean                                     |
| `openingHours`          | Não         | texto livre                                 |
| `entryPrice`            | Não         | positivo                                    |
| `hasGuide`              | Não         | boolean                                     |
| `averageVisitDuration`  | Não         | inteiro (minutos)                           |
| `category`              | Sim         | `PARK`, `WATERFALL`, `MUSEUM`, `FARM`, `ROUTE`, `MARKET` |

**Resposta (201 Created):**

- Header: `Location: /api/v1/attractions/{id}`
- Body: `AttractionResponseDTO`

**Códigos HTTP:**

| Código | Situação                              |
|--------|---------------------------------------|
| 201    | Criado com sucesso                    |
| 400    | E-mail duplicado                      |
| 401    | Token ausente ou inválido             |
| 403    | Sem permissão (role USER)             |
| 422    | Campos inválidos                      |

---

#### `POST /attractions/{id}` — Vincular sub-atrativo _(ADMIN)_

Cria um atrativo filho (sub-atração) vinculado ao atrativo pai com o `{id}` informado.

**Path param:** `id` (UUID do atrativo pai)  
**Body:** Mesmo schema de criação (`AttractionRequestDTO`)

**Resposta (200 OK):** `AttractionResponseDTO` do sub-atrativo criado

**Codes HTTP:**

| Código | Situação                                   |
|--------|--------------------------------------------|
| 200    | Sub-atrativo criado e vinculado            |
| 404    | Atrativo pai não encontrado                |
| 401/403 | Sem autenticação ou sem permissão         |

---

#### `POST /attractions/{id}/photos` — Upload de foto _(ADMIN)_

**Content-Type:** `multipart/form-data`

| Part          | Tipo          | Obrigatório | Descrição                                      |
|---------------|---------------|-------------|------------------------------------------------|
| `file`        | arquivo       | Sim         | JPEG, PNG, WEBP ou GIF. Máx. 5MB              |
| `description` | texto         | Não         | Legenda da foto                                |
| `displayOrder`| texto (número)| Não         | Ordem de exibição (enviado como string no FormData) |

**Resposta (201 Created):** `PhotoResponseDTO`

```json
{
  "id": "uuid",
  "url": "https://parque-ubajara-fotos.s3.sa-east-1.amazonaws.com/uuid-chave",
  "description": "Vista frontal",
  "displayOrder": 1,
  "ownerType": "ATTRACTION",
  "ownerId": "uuid-do-atrativo",
  "createdAt": "2026-05-18T10:00:00"
}
```

**Códigos HTTP:**

| Código | Situação                          |
|--------|-----------------------------------|
| 201    | Foto enviada e salva              |
| 400    | Arquivo inválido ou > 5MB        |
| 404    | Atrativo não encontrado           |
| 401/403| Sem autenticação ou permissão    |

---

#### `PUT /attractions/{id}` — Atualizar atrativo _(ADMIN)_

Atualização parcial: campos `null` no body são ignorados (strategy `IGNORE` no MapStruct).

**Body (todos os campos opcionais):**

```json
{
  "name": "Novo Nome",
  "active": false,
  "entryPrice": 25.00
}
```

**Resposta (204 No Content):** Body vazio

---

#### `DELETE /attractions/{id}` — Excluir atrativo _(ADMIN)_

**Resposta (204 No Content):** Body vazio

---

### 5.3 Pontos Turísticos (Tourist Spots)

**Base:** `/tourist-spots`  
**Nota:** `TouristSpot` é a entidade base para `Attraction`, `Restaurant` e `HostPoint`. Endpoints diretos de `/tourist-spots` trabalham com a entidade genérica.

---

#### `GET /tourist-spots` — Listar pontos turísticos

**Query params:** `page`, `size`, `sort` (padrão: `name`)

**Resposta (200 OK):** `Page<TouristSpotResponseDTO>`

```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Parque Nacional de Ubajara",
      "description": "...",
      "address": "...",
      "phone": "(88) 3634-1388",
      "email": "parque@icmbio.gov.br",
      "webUrl": "https://...",
      "instagramUrl": "https://...",
      "active": true,
      "photos": [ ... ]
    }
  ],
  "totalElements": 20,
  ...
}
```

---

#### `GET /tourist-spots/{id}` — Buscar ponto turístico por ID

**Resposta (200 OK):** `TouristSpotResponseDTO`

---

#### `POST /tourist-spots` — Criar ponto turístico _(ADMIN)_

**Body:**

```json
{
  "name": "Mirante do Araticum",
  "description": "Vista panorâmica...",
  "address": "Estrada CE-187, km 12",
  "phone": "(88) 99888-7766",
  "email": null,
  "webUrl": null,
  "instagramUrl": null,
  "active": true
}
```

| Campo         | Obrigatório | Regras                  |
|---------------|-------------|-------------------------|
| `name`        | Sim         | máx. 100 caracteres     |
| `description` | Sim         | —                       |
| `address`     | Sim         | —                       |
| `phone`       | Não         | —                       |
| `email`       | Não         | formato e-mail          |
| `webUrl`      | Não         | —                       |
| `instagramUrl`| Não         | —                       |
| `active`      | Sim         | boolean                 |

**Resposta (201 Created):** `TouristSpotResponseDTO`  
> **Nota:** Este endpoint **não retorna o header `Location`** (diferente dos demais módulos).

---

#### `POST /tourist-spots/{id}/photos` — Upload de foto _(ADMIN)_

Mesmo padrão dos outros módulos (multipart/form-data).

---

#### `PUT /tourist-spots/{id}` — Atualizar _(ADMIN)_

**Resposta (204 No Content)**

---

#### `DELETE /tourist-spots/{id}` — Excluir _(ADMIN)_

**Resposta (204 No Content)**

---

### 5.4 Restaurantes (Restaurants)

**Base:** `/restaurants`  
**Herança:** Estende `TouristSpot`

---

#### `GET /restaurants` — Listar restaurantes

**Query params:** `page`, `size`, `sort`

**Resposta (200 OK):** `Page<RestaurantResponseDTO>`

```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Restaurante do Pé de Serra",
      "description": "Culinária nordestina...",
      "address": "Rua das Pedras, 100",
      "phone": "(88) 3634-0000",
      "email": "pe.de.serra@email.com",
      "webUrl": null,
      "instagramUrl": null,
      "active": true,
      "cuisineType": "Nordestina",
      "openingHours": "11:00–22:00",
      "avgPrice": 35.00,
      "acceptsReservation": true,
      "photos": [ ... ]
    }
  ]
}
```

---

#### `GET /restaurants/{id}` — Buscar restaurante por ID

**Resposta (200 OK):** `RestaurantResponseDTO`

---

#### `POST /restaurants` — Criar restaurante _(ADMIN)_

**Body:**

```json
{
  "name": "Restaurante do Pé de Serra",
  "description": "Culinária nordestina tradicional",
  "address": "Rua das Pedras, 100, Ubajara-CE",
  "phone": "(88) 3634-0000",
  "email": "pe.de.serra@email.com",
  "webUrl": null,
  "instagramUrl": null,
  "active": true,
  "cuisineType": "Nordestina",
  "openingHours": "11:00–22:00",
  "avgPrice": 35.00
}
```

| Campo           | Obrigatório | Regras              |
|-----------------|-------------|---------------------|
| `name`          | Sim         | máx. 100 caracteres |
| `description`   | Sim         | —                   |
| `address`       | Sim         | —                   |
| `cuisineType`   | Sim         | texto livre         |
| `openingHours`  | Não         | texto livre         |
| `avgPrice`      | Não         | positivo            |
| `active`        | Sim         | boolean             |

> **Bug identificado:** O campo `acceptsReservation` **não está presente no DTO de request** (`RestaurantRequestDTO`), portanto **não pode ser definido na criação**. Sempre será `null` até que o backend seja corrigido. O campo aparece normalmente na resposta.

**Resposta (201 Created):** `RestaurantResponseDTO` + header `Location`

---

#### `POST /restaurants/{id}/photos` — Upload de foto _(ADMIN)_

Padrão multipart/form-data.

---

#### `PUT /restaurants/{id}` — Atualizar _(ADMIN)_

**Resposta (204 No Content)**

---

#### `DELETE /restaurants/{id}` — Excluir _(ADMIN)_

**Resposta (204 No Content)**

---

### 5.5 Hospedagens (Host Points)

**Base:** `/host-points`  
**Herança:** Estende `TouristSpot`

---

#### `GET /host-points` — Listar hospedagens

**Query params:**

| Parâmetro | Tipo       | Obrigatório | Descrição                          |
|-----------|------------|-------------|------------------------------------|
| `type`    | `HostType` | Não         | Filtrar por tipo de hospedagem     |
| `page`    | integer    | Não         | Padrão: 0                          |
| `size`    | integer    | Não         | Padrão: 10                         |

**Enum `HostType`:** `HOTEL | ROOST | HOSTEL`

**Exemplo:**

```
GET /host-points?type=HOTEL&page=0&size=5
```

**Resposta (200 OK):** `Page<HostPointResponseDTO>`

```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Pousada Serra Verde",
      "description": "Pousada com vista para a serra...",
      "address": "Estrada CE-187, km 3",
      "phone": "(88) 3634-1200",
      "email": "pousada@serraverde.com",
      "webUrl": "https://serraverde.com",
      "instagramUrl": null,
      "active": true,
      "hostType": "ROOST",
      "numOfRooms": 12,
      "avgPrice": 180.00,
      "bookingUrl": "https://booking.com/serraverde",
      "photos": [ ... ]
    }
  ]
}
```

---

#### `GET /host-points/{id}` — Buscar hospedagem por ID

**Resposta (200 OK):** `HostPointResponseDTO`

---

#### `POST /host-points` — Criar hospedagem _(ADMIN)_

**Body:**

```json
{
  "name": "Pousada Serra Verde",
  "description": "Pousada aconchegante...",
  "address": "Estrada CE-187, km 3",
  "phone": "(88) 3634-1200",
  "email": "pousada@serraverde.com",
  "webUrl": "https://serraverde.com",
  "instagramUrl": null,
  "active": true,
  "hostType": "ROOST",
  "numOfRooms": 12,
  "avgPrice": 180.00,
  "bookingUrl": "https://booking.com/serraverde"
}
```

| Campo        | Obrigatório | Regras                                    |
|--------------|-------------|-------------------------------------------|
| `name`       | Sim         | máx. 100 caracteres                       |
| `description`| Sim         | —                                         |
| `address`    | Sim         | —                                         |
| `active`     | Sim         | boolean                                   |
| `hostType`   | Sim         | `HOTEL`, `ROOST`, `HOSTEL`                |
| `numOfRooms` | Não         | inteiro                                   |
| `avgPrice`   | Não         | positivo                                  |
| `bookingUrl` | Não         | URL de reserva externa                    |

**Resposta (201 Created):** `HostPointResponseDTO` + header `Location`

---

#### `POST /host-points/{id}/photos` — Upload de foto _(ADMIN)_

Padrão multipart/form-data.

---

#### `PUT /host-points/{id}` — Atualizar _(ADMIN)_

**Resposta (204 No Content)**

---

#### `DELETE /host-points/{id}` — Excluir _(ADMIN)_

**Resposta (204 No Content)**

---

### 5.6 Eventos (Events)

**Base:** `/events`

---

#### `GET /events` — Listar eventos

**Query params:**

| Parâmetro       | Tipo            | Obrigatório | Formato                  | Descrição                         |
|-----------------|-----------------|-------------|--------------------------|-----------------------------------|
| `startDateTime` | LocalDateTime   | Não         | `dd/MM/yyyy HH:mm:ss`   | Início do intervalo de busca      |
| `endDateTime`   | LocalDateTime   | Não         | `dd/MM/yyyy HH:mm:ss`   | Fim do intervalo de busca         |
| `page`          | integer         | Não         | —                        | Padrão: 0                         |
| `size`          | integer         | Não         | —                        | Padrão: 10                        |

**Exemplo:**

```
GET /events?startDateTime=01/06/2026 00:00:00&endDateTime=30/06/2026 23:59:59
```

> O valor deve ser URL-encoded: `01%2F06%2F2026+00%3A00%3A00`

**Resposta (200 OK):** `Page<EventResponseDTO>`

```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Festival de Inverno de Ubajara",
      "description": "Festival cultural com shows e gastronomia...",
      "startDateTime": "20/07/2026 18:00:00",
      "endDateTime": "22/07/2026 23:00:00",
      "location": "Praça Central de Ubajara",
      "registrationUrl": "https://eventbrite.com/festival-ubajara",
      "active": true,
      "photos": [ ... ]
    }
  ]
}
```

> **Atenção:** Datas na resposta são retornadas no formato `dd/MM/yyyy HH:mm:ss` (não ISO 8601).

---

#### `GET /events/{id}` — Buscar evento por ID

**Resposta (200 OK):** `EventResponseDTO`

---

#### `POST /events` — Criar evento _(ADMIN)_

**Body:**

```json
{
  "name": "Festival de Inverno de Ubajara",
  "description": "Festival cultural com shows e gastronomia regional",
  "startDateTime": "20/07/2026 18:00:00",
  "endDateTime": "22/07/2026 23:00:00",
  "location": "Praça Central de Ubajara",
  "registrationUrl": "https://eventbrite.com/festival-ubajara",
  "active": true
}
```

| Campo             | Obrigatório | Regras                              |
|-------------------|-------------|-------------------------------------|
| `name`            | Sim         | —                                   |
| `description`     | Sim         | —                                   |
| `startDateTime`   | Sim         | formato `dd/MM/yyyy HH:mm:ss`, futuro ou presente |
| `endDateTime`     | Sim         | formato `dd/MM/yyyy HH:mm:ss`, futuro ou presente |
| `location`        | Sim         | —                                   |
| `registrationUrl` | Não         | —                                   |
| `active`          | Sim         | boolean                             |

**Resposta (201 Created):** `EventResponseDTO` + header `Location`

---

#### `POST /events/{id}/photos` — Upload de foto _(ADMIN)_

Padrão multipart/form-data.

---

#### `PUT /events/{id}` — Atualizar _(ADMIN)_

**Resposta (204 No Content)**

---

#### `DELETE /events/{id}` — Excluir _(ADMIN)_

**Resposta (204 No Content)**

---

### 5.7 Guias Turísticos (Tour Guides)

**Base:** `/tour-guides`

---

#### `GET /tour-guides` — Listar guias

**Query params:** `page`, `size`, `sort`

**Resposta (200 OK):** `Page<TourGuideResponseDTO>`

```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Carlos Albuquerque",
      "phone": "(88) 99777-5555",
      "email": "carlos.guia@email.com",
      "languages": ["Português", "Inglês", "Espanhol"],
      "description": "Guia credenciado com 10 anos de experiência...",
      "active": true,
      "photos": [ ... ]
    }
  ]
}
```

---

#### `GET /tour-guides/{id}` — Buscar guia por ID

**Resposta (200 OK):** `TourGuideResponseDTO`

---

#### `POST /tour-guides` — Criar guia _(ADMIN)_

**Body:**

```json
{
  "name": "Carlos Albuquerque",
  "phone": "(88) 99777-5555",
  "email": "carlos.guia@email.com",
  "languages": ["Português", "Inglês"],
  "description": "Guia credenciado com 10 anos de experiência no Parque Nacional",
  "active": true
}
```

| Campo         | Obrigatório | Regras                                             |
|---------------|-------------|-----------------------------------------------------|
| `name`        | Sim         | —                                                   |
| `phone`       | Sim         | —                                                   |
| `email`       | Sim         | texto não vazio (sem validação de formato — ver nota) |
| `languages`   | Não         | lista de strings                                    |
| `description` | Não         | —                                                   |
| `active`      | Sim         | boolean                                             |

> **Nota:** O campo `email` do guia turístico usa `@NotBlank` mas **não usa `@Email`**. O backend aceita qualquer string não vazia como e-mail de guia. O frontend deve aplicar validação própria de formato de e-mail para evitar dados inconsistentes.

**Resposta (201 Created):** `TourGuideResponseDTO` + header `Location`

---

#### `POST /tour-guides/{id}/photos` — Upload de foto _(ADMIN)_

Padrão multipart/form-data.

---

#### `PUT /tour-guides/{id}` — Atualizar _(ADMIN)_

**Resposta (204 No Content)**

---

#### `DELETE /tour-guides/{id}` — Excluir _(ADMIN)_

**Resposta (204 No Content)**

---

### 5.8 Aeroportos (Airports)

**Base:** `/airports`

---

#### `GET /airports` — Listar aeroportos

**Query params:** `page`, `size`, `sort`

**Resposta (200 OK):** `Page<AirportResponseDTO>`

```json
{
  "content": [
    {
      "id": "uuid",
      "iataCode": "FOR",
      "city": "Fortaleza",
      "distanceKm": 350.5,
      "estimatedTimeMinutes": 270,
      "routeDescription": "BR-222 → CE-187, entrando pela rodovia principal...",
      "photos": [ ... ]
    }
  ]
}
```

---

#### `GET /airports/{id}` — Buscar aeroporto por ID

**Resposta (200 OK):** `AirportResponseDTO`

---

#### `POST /airports` — Criar aeroporto _(ADMIN)_

**Body:**

```json
{
  "iataCode": "FOR",
  "city": "Fortaleza",
  "distanceKm": 350.5,
  "estimatedTimeMinutes": 270,
  "routeDescription": "BR-222 → CE-187..."
}
```

| Campo                    | Obrigatório | Regras                  |
|--------------------------|-------------|-------------------------|
| `iataCode`               | Sim         | exatamente 3 caracteres, único |
| `city`                   | Sim         | máx. 50 caracteres      |
| `distanceKm`             | Não         | positivo                |
| `estimatedTimeMinutes`   | Não         | inteiro                 |
| `routeDescription`       | Não         | texto livre             |

**Resposta (201 Created):** `AirportResponseDTO` + header `Location`

---

#### `POST /airports/{id}/photos` — Upload de foto _(ADMIN)_

Padrão multipart/form-data.

---

#### `PUT /airports/{id}` — Atualizar _(ADMIN)_

**Resposta (204 No Content)**

---

#### `DELETE /airports/{id}` — Excluir _(ADMIN)_

**Resposta (204 No Content)**

---

### 5.9 Contatos (Contacts)

**Base:** `/contacts`

---

#### `GET /contacts` — Listar contatos

**Query params:** `page`, `size`, `sort`

**Resposta (200 OK):** `Page<ContactsResponseDTO>`

```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Secretaria de Turismo de Ubajara",
      "category": "Órgão público",
      "phone": "(88) 3634-1500",
      "email": "turismo@ubajara.ce.gov.br",
      "description": "Atendimento de segunda a sexta, 08h–17h"
    }
  ]
}
```

---

#### `GET /contacts/{id}` — Buscar contato por ID

**Resposta (200 OK):** `ContactsResponseDTO`

---

#### `POST /contacts` — Criar contato _(ADMIN)_

**Body:**

```json
{
  "name": "Secretaria de Turismo de Ubajara",
  "category": "Órgão público",
  "phone": "(88) 3634-1500",
  "email": "turismo@ubajara.ce.gov.br",
  "description": "Atendimento de segunda a sexta, 08h–17h"
}
```

| Campo         | Obrigatório | Regras              |
|---------------|-------------|---------------------|
| `name`        | Sim         | —                   |
| `category`    | Não         | máx. 50 caracteres  |
| `phone`       | Sim         | —                   |
| `email`       | Sim         | formato e-mail, único |
| `description` | Não         | —                   |

**Resposta (201 Created):** `ContactsResponseDTO` + header `Location`

---

#### `PUT /contacts/{id}` — Atualizar _(ADMIN)_

**Resposta (204 No Content)**

---

#### `DELETE /contacts/{id}` — Excluir _(ADMIN)_

**Resposta (204 No Content)**

---

### 5.10 Fotos (Photos)

**Base:** `/photos`

> **Contexto:** Este controller gerencia fotos de forma genérica. Para upload vinculado a uma entidade específica, prefer os endpoints `POST /{modulo}/{id}/photos` de cada módulo. O `POST /photos` genérico cria uma foto sem vínculo inicial.

---

#### `POST /photos` — Upload genérico de foto _(ADMIN)_

**Auth:** Bearer Token `ADMIN`  
**Content-Type:** `multipart/form-data`

| Part          | Tipo    | Obrigatório | Descrição                                       |
|---------------|---------|-------------|-------------------------------------------------|
| `file`        | arquivo | Sim         | JPEG, PNG, WEBP ou GIF. Máx. 5MB               |
| `description` | texto   | Não         | Legenda                                         |
| `displayOrder`| texto   | Não         | Ordem de exibição (número como string)          |

**Resposta (201 Created):** `PhotoResponseDTO`

---

#### `DELETE /photos/{id}` — Excluir foto _(ADMIN)_

Remove a foto do **AWS S3** e do banco de dados.

**Path param:** `id` (UUID)

**Resposta (204 No Content)**

**Códigos HTTP:**

| Código | Situação               |
|--------|------------------------|
| 204    | Removida com sucesso   |
| 404    | Foto não encontrada    |

---

### 5.11 Usuários (Users)

**Base:** `/users`  
**Autenticação:** Todos os endpoints requerem autenticação (qualquer usuário logado pode listar e buscar; ADMIN para modificar)

---

#### `GET /users` — Listar usuários _(autenticado)_

**Query params:**

| Parâmetro  | Tipo   | Obrigatório | Descrição                               |
|------------|--------|-------------|------------------------------------------|
| `username` | string | Não         | Filtro parcial, case-insensitive         |
| `page`     | integer| Não         | Padrão: 0                                |
| `size`     | integer| Não         | Padrão: 10                               |

**Resposta (200 OK):** `Page<UserResponseDTO>`

```json
{
  "content": [
    {
      "id": "uuid",
      "firstName": "João",
      "lastName": "Silva",
      "username": "joaosilva",
      "email": "joao@email.com",
      "role": "USER"
    }
  ]
}
```

---

#### `GET /users/{id}` — Buscar usuário por ID _(autenticado)_

**Resposta (200 OK):** `UserResponseDTO`

---

#### `PATCH /users/{id}` — Atualizar usuário _(ADMIN)_

Todos os campos são opcionais. O campo `role` permite ao ADMIN promover ou rebaixar um usuário.

**Body (parcial):**

```json
{
  "firstName": "João",
  "lastName": "Almeida",
  "username": "joaoalmeida",
  "email": "novo@email.com",
  "password": "novasenha123",
  "role": "ADMIN"
}
```

**Resposta (204 No Content)**

---

#### `DELETE /users/{id}` — Excluir usuário _(ADMIN)_

**Resposta (204 No Content)**

---

## 6. Paginação

Todos os endpoints de listagem aceitam os seguintes query params:

| Parâmetro | Tipo    | Padrão | Descrição                                         |
|-----------|---------|--------|---------------------------------------------------|
| `page`    | integer | 0      | Número da página (começa em 0)                   |
| `size`    | integer | 10     | Quantidade de itens por página                   |
| `sort`    | string  | varia  | Campo de ordenação. Ex.: `name,asc` ou `name,desc` |

**Exemplo de request paginado:**

```
GET /attractions?page=1&size=5&sort=name,desc
```

**Estrutura da resposta paginada (Page\<T\>):**

```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 1,
    "pageSize": 5,
    "sort": { "sorted": true, "orders": [{ "property": "name", "direction": "DESC" }] }
  },
  "totalElements": 42,
  "totalPages": 9,
  "number": 1,
  "size": 5,
  "first": false,
  "last": false,
  "empty": false
}
```

### Exemplo de uso no React

```typescript
interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // página atual
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// Uso com axios
const response = await api.get<Page<AttractionDTO>>('/attractions', {
  params: { page: 0, size: 10, category: 'WATERFALL' }
});
const { content, totalElements, totalPages } = response.data;
```

---

## 7. Upload de Arquivos (Multipart)

Upload de fotos usa `multipart/form-data`. **Não envie** `Content-Type: application/json`.

### Exemplo no React com axios

```typescript
const uploadPhoto = async (entityId: string, file: File, description?: string, displayOrder?: number) => {
  const formData = new FormData();
  formData.append('file', file);
  
  if (description) formData.append('description', description);
  if (displayOrder !== undefined) formData.append('displayOrder', String(displayOrder));

  const response = await api.post<PhotoResponseDTO>(
    `/attractions/${entityId}/photos`,
    formData,
    {
      headers: {
        // NÃO defina 'Content-Type' manualmente - o axios define automaticamente com boundary
        'Authorization': `Bearer ${token}`
      }
    }
  );
  return response.data;
};
```

### Restrições de arquivo

| Propriedade    | Valor                              |
|----------------|------------------------------------|
| Tipos aceitos  | `image/jpeg`, `image/png`, `image/webp`, `image/gif` |
| Tamanho máximo | 5 MB                               |
| Armazenamento  | AWS S3 (bucket `parque-ubajara-fotos`, região `sa-east-1`) |

---

## 8. Tratamento de Erros

### Mapeamento de erros para o frontend

| Código HTTP | Situação                                  | Ação recomendada no frontend          |
|-------------|-------------------------------------------|---------------------------------------|
| 400         | Requisição inválida (arquivo, e-mail dup) | Exibir mensagem de erro da resposta   |
| 401         | Token ausente, inválido ou expirado       | Redirecionar para tela de login       |
| 403         | Usuário sem permissão (role insuficiente) | Exibir "Acesso negado"                |
| 404         | Recurso não encontrado                    | Exibir "Não encontrado"               |
| 422         | Falha de validação de campos              | Exibir erros por campo (ver abaixo)   |
| 500         | Erro interno do servidor                  | Exibir mensagem genérica              |

### Tratamento de erros de validação (422)

```typescript
interface ValidationErrorResponse {
  timestamp: string;
  status: 422;
  error: string;
  fields: { field: string; message: string }[];
}

// Mapear erros por campo para exibição nos inputs
const handleValidationError = (error: AxiosError<ValidationErrorResponse>) => {
  const fieldErrors: Record<string, string> = {};
  error.response?.data.fields.forEach(({ field, message }) => {
    fieldErrors[field] = message;
  });
  setErrors(fieldErrors);
};
```

### Configuração de interceptor axios recomendada

```typescript
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

---

## 9. Observações para Consumo no React

### Configuração base do axios

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8081/api/v1',
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true, // necessário pois CORS tem allowCredentials: true
});

// Injeta token automaticamente
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### Datas — atenção especial para eventos

O backend usa o formato `dd/MM/yyyy HH:mm:ss` (não ISO 8601) para datas de eventos.

```typescript
import { format, parse } from 'date-fns';

// Enviar para o backend
const formatForApi = (date: Date) => format(date, 'dd/MM/yyyy HH:mm:ss');

// Receber do backend
const parseFromApi = (str: string) => parse(str, 'dd/MM/yyyy HH:mm:ss', new Date());
```

### OAuth2 Google — captura do token

```typescript
// Página /login-success
useEffect(() => {
  const params = new URLSearchParams(window.location.search);
  const token = params.get('token');
  if (token) {
    localStorage.setItem('token', token);
    // decodificar o token para extrair email e role
    navigate('/dashboard');
  }
}, []);
```

### Decodificação do JWT para role

```typescript
import { jwtDecode } from 'jwt-decode';

// O token contém apenas { sub: email, iat, exp }
// O role vem na resposta de login: authResponse.role
// Armazene também o role localmente junto com o token

interface AuthState {
  token: string;
  email: string;
  role: 'USER' | 'ADMIN';
}
```

> O JWT **não contém o campo `role`** no payload. A role retorna apenas no body da resposta de `/auth/login` e `/auth/register`. Armazene-a separadamente (ex.: `localStorage.setItem('role', response.data.role)`).

### Controle de acesso no frontend

```tsx
// Exemplo de proteção de rota/botão para ADMIN
const isAdmin = localStorage.getItem('role') === 'ADMIN';

{isAdmin && <button onClick={handleDelete}>Excluir</button>}
```

---

## 10. Endpoints Recomendados / Lacunas Identificadas

Os itens abaixo **não existem** no backend atual. São recomendações baseadas em gaps identificados na análise das regras de negócio.

| # | Endpoint sugerido               | Método | Motivo / Caso de uso                                                                 |
|---|----------------------------------|--------|--------------------------------------------------------------------------------------|
| 1 | `/users/me`                     | GET    | Permitir que o usuário logado acesse seu próprio perfil sem precisar saber seu UUID  |
| 2 | `/users/me`                     | PATCH  | Permitir que o usuário logado edite seu próprio perfil (sem expor admin endpoint)    |
| 3 | `/auth/me`                      | GET    | Alternativa ao `/users/me` para retornar dados do usuário logado via token           |
| 4 | `/auth/refresh`                 | POST   | Renovação de token JWT antes da expiração de 24h (sem necessidade de relogin)       |
| 5 | `/auth/logout`                  | POST   | Blacklist do token ou limpeza de sessão OAuth2 (hoje o logout é apenas client-side) |
| 6 | `/events?active=true`           | GET    | Filtrar somente eventos ativos (existe `active` no modelo, falta filtro na query)    |
| 7 | `/tourist-spots?active=true`    | GET    | Filtrar somente pontos ativos (existe `active` no modelo, falta filtro)              |
| 8 | `/tour-guides?active=true`      | GET    | Filtrar somente guias ativos                                                         |
| 9 | `/attractions?parentId=null`    | GET    | Listar apenas atrativos raiz (sem pai), excluindo sub-atrativos da listagem geral    |
| 10| `/photos/{id}`                  | GET    | Buscar metadados de uma foto específica por ID                                       |
| 11| `/photos/{id}`                  | PUT    | Atualizar `description` e `displayOrder` de uma foto existente                       |
| 12| Reordenação de fotos em lote   | PATCH  | Ex.: `PATCH /attractions/{id}/photos/order` com lista de IDs+ordem                  |

---

## 11. Dependências entre Telas e Endpoints

| Tela / Funcionalidade               | Endpoints envolvidos                                                                                       | Notas                                              |
|-------------------------------------|-------------------------------------------------------------------------------------------------------------|----------------------------------------------------|
| **Tela de Login**                   | `POST /auth/login`, `GET /oauth2/authorization/google`                                                     | Armazenar `token` e `role` no storage              |
| **Tela de Cadastro**                | `POST /auth/register`                                                                                       | Retorna token, usuário já fica logado              |
| **Home / Listagem de Atrativos**    | `GET /attractions`, `GET /attractions/{id}`                                                                | Ver nota sobre bug no CORS path                    |
| **Detalhe do Atrativo**             | `GET /attractions/{id}`                                                                                     | Inclui `subAttractions` e `photos` na resposta     |
| **Listagem de Restaurantes**        | `GET /restaurants`                                                                                          | —                                                  |
| **Listagem de Hospedagens**         | `GET /host-points?type=HOTEL` / `ROOST` / `HOSTEL`                                                        | Filtro por tipo                                    |
| **Agenda de Eventos**               | `GET /events?startDateTime=...&endDateTime=...`                                                            | Filtro por intervalo de datas                      |
| **Guias Turísticos**                | `GET /tour-guides`                                                                                          | Inclui lista de idiomas e fotos                    |
| **Aeroportos / Como Chegar**        | `GET /airports`                                                                                             | Exibe distância, tempo estimado e rota             |
| **Contatos Úteis**                  | `GET /contacts`                                                                                             | —                                                  |
| **Painel Admin — Criar/Editar**     | `POST`, `PUT`/`PATCH` de cada módulo + `POST /{id}/photos` + `DELETE /photos/{id}`                        | Requer token com role `ADMIN`                      |
| **Painel Admin — Usuários**         | `GET /users`, `PATCH /users/{id}` (inclui `role`), `DELETE /users/{id}`                                   | PATCH pode promover/rebaixar role de usuário       |
| **Perfil do Usuário Logado**        | `GET /users/{id}` (user precisa saber seu UUID)                                                            | Recomenda-se implementar `GET /users/me`           |
| **Upload de Foto (Admin)**          | `POST /{modulo}/{id}/photos`                                                                                | FormData, `displayOrder` enviado como string       |
| **Exclusão de Foto (Admin)**        | `DELETE /photos/{id}`                                                                                       | Remove do S3 e do banco                            |

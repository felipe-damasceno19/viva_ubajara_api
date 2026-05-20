# Parque Ubajara API — Revisão de Segurança Backend

> Versão do documento: 1.0 — gerado em 2026-05-18  
> Tipo: auditoria documental estática (sem execução de código)  
> Baseado na análise do código-fonte (branch `main`)

---

## Sumário

1. [Arquitetura de Segurança Atual](#1-arquitetura-de-segurança-atual)
2. [O Que Está Corretamente Implementado](#2-o-que-está-corretamente-implementado)
3. [Vulnerabilidades e Riscos Identificados](#3-vulnerabilidades-e-riscos-identificados)
4. [Pontos de Melhoria sem Vulnerabilidade Imediata](#4-pontos-de-melhoria-sem-vulnerabilidade-imediata)
5. [Boas Práticas Faltantes](#5-boas-práticas-faltantes)
6. [Checklist de Segurança](#6-checklist-de-segurança)

---

## 1. Arquitetura de Segurança Atual

### Visão geral

```
Cliente → CORS → JwtFilter → SecurityFilterChain → Controller
                    ↓
           SecurityContextHolder
           (CustomAuthentication)
                    ↓
           @PreAuthorize("hasRole('ADMIN')")
```

### Componentes principais

| Componente                      | Arquivo                                 | Função                                             |
|---------------------------------|-----------------------------------------|----------------------------------------------------|
| `SecurityConfig`                | `config/SecurityConfig.java`            | Configuração central do Spring Security            |
| `JwtFilter`                     | `security/JwtFilter.java`               | Extrai e valida o JWT a cada requisição            |
| `JwtService`                    | `security/JwtService.java`              | Geração, extração e validação de tokens JWT        |
| `CustomAuthentication`          | `security/CustomAuthentication.java`    | Objeto de autenticação com roles do usuário        |
| `CustomAuthenticationProvider`  | `security/CustomAuthenticationProvider.java` | Autenticação por e-mail e senha              |
| `CustomUserDetailsService`      | `security/CustomUserDetailsService.java`| Carrega usuário do banco por e-mail                |
| `SocialLoginSuccessHandler`     | `security/SocialLoginSuccessHandler.java` | Processa OAuth2 Google e emite JWT              |
| `SocialLoginFailureHandler`     | `handler/SocialLoginFailureHandler.java`| Retorna 401 em falha OAuth2                        |
| `GlobalExceptionHandler`        | `handler/GlobalExceptionHandler.java`   | Centraliza tratamento de exceções                  |
| `CustomAuthenticationEntryPoint`| `handler/CustomAuthenticationEntryPoint.java` | Resposta JSON para 401 (sem token)       |
| `CustomAccessDeniedHandler`     | `handler/CustomAccessDeniedHandler.java` | Resposta JSON para 403 (sem permissão)            |
| `FileValidationService`         | `service/infra/FileValidationService.java` | Valida tipo e tamanho de arquivos enviados      |

### Fluxo de autenticação JWT

```
1. POST /auth/login { email, password }
2. CustomAuthenticationProvider.authenticate()
   → BCryptPasswordEncoder.matches(raw, encoded)
3. UserDetails carregado via CustomUserDetailsService.loadUserByUsername(email)
4. JwtService.generateToken(userDetails)
   → subject = email
   → expiration = now + 86400000ms (24h)
   → assinado com HMAC-SHA (segredo via env ${JWT_SECRET})
5. AuthResponseDTO { token, email, role } → cliente
6. Próximas requisições:
   → JwtFilter extrai token do header Authorization: Bearer <token>
   → Valida assinatura + expiração
   → Busca usuário no banco pelo email extraído
   → Cria CustomAuthentication com roles do banco
   → Injeta no SecurityContextHolder
```

### Controle de acesso

| Camada        | Mecanismo                                          |
|---------------|----------------------------------------------------|
| URL           | `authorizeHttpRequests()` no `SecurityConfig`      |
| Método        | `@PreAuthorize("hasRole('ADMIN')")` nos controllers |
| Roles         | `CustomAuthentication.getAuthorities()`            |

### Endpoints públicos (sem autenticação)

Conforme `SecurityConfig.java`:

```
POST /auth/**              → registro e login
GET  /tourist-spots/**     → pontos turísticos
GET  /attractives/**       → (bug: ver seção 3.1)
GET  /host-points/**       → hospedagens
GET  /restaurants/**       → restaurantes
GET  /events/**            → eventos
GET  /tour-guides/**       → guias turísticos
GET  /airports/**          → aeroportos
GET  /contacts/**          → contatos
GET  /photos/**            → fotos
GET  /v3/api-docs/**       → Swagger
GET  /swagger-ui/**        → Swagger UI
```

---

## 2. O Que Está Corretamente Implementado

### 2.1 JWT stateless com HMAC-SHA

- Token assinado com chave HMAC-SHA via JJWT
- Segredo carregado de variável de ambiente (`${JWT_SECRET}`), não hardcoded
- Validação de assinatura + expiração em cada requisição
- Sessão stateless (`SessionCreationPolicy.STATELESS`), sem cookies de sessão

**Avaliação:** Correto e adequado para uma API REST.

---

### 2.2 Senhas com BCrypt

- `PasswordConfig.java` registra `BCryptPasswordEncoder` com strength 10
- Aplicado no registro e comparado na autenticação
- Usuários OAuth2 recebem senha aleatória (`UUID.randomUUID()`) hashada, inviabilizando login por senha para esses usuários

**Avaliação:** Correto. BCrypt com strength 10 é adequado para uso em produção.

---

### 2.3 CSRF desabilitado

- `csrf(AbstractHttpConfigurer::disable)` está presente
- Para APIs stateless com JWT (sem cookies de sessão), o CSRF não é necessário
- O CSRF é um ataque relevante apenas quando autenticação é feita via cookies

**Avaliação:** Correto para o modelo de autenticação adotado.

---

### 2.4 CORS com origens explícitas

- Origens permitidas: `localhost:3000`, `localhost:5173`, `localhost:4200`
- Métodos: `GET, POST, PUT, DELETE, OPTIONS`
- Headers: `Authorization, Content-Type`
- `allowCredentials: true` com origens explícitas (válido — não usa wildcard)

**Avaliação:** Configuração segura para desenvolvimento. A combinação de `allowCredentials: true` com origens explícitas está correta (seria inválida com wildcard `*`).

---

### 2.5 Credenciais sensíveis via variáveis de ambiente

- `${JWT_SECRET}`, `${AWS_ACCESS_KEY}`, `${AWS_SECRET_KEY}`, `${CLIENT_ID}`, `${CLIENT_SECRET}`, `${DATASOURCE_URL}`, `${DATASOURCE_USERNAME}`, `${DATASOURCE_PASSWORD}`
- Nenhuma credencial hardcoded no `application.yaml`

**Avaliação:** Correto e alinhado com boas práticas (12-factor app).

---

### 2.6 Separação de roles (ADMIN / USER)

- `CustomAuthentication` retorna `ROLE_ADMIN` + `ROLE_USER` para admins e apenas `ROLE_USER` para usuários comuns
- `@PreAuthorize("hasRole('ADMIN')")` protege endpoints de escrita
- Lógica de roles carregada do banco a cada requisição (sempre atualizada)

**Avaliação:** Design correto. Mudanças de role têm efeito imediato.

---

### 2.7 Validação de entrada (Bean Validation)

- `@Valid` nos controllers + anotações nos DTOs (`@NotBlank`, `@Email`, `@Size`, `@Positive`, `@FutureOrPresent`)
- `GlobalExceptionHandler` captura `MethodArgumentNotValidException` → 422 com detalhes por campo
- Campos opcionais bem separados de obrigatórios

**Avaliação:** Cobertura adequada. Exceções: ver item 3.7 (email do guia turístico).

---

### 2.8 Validação de arquivos

`FileValidationService.java` valida:
- Tamanho máximo: 5MB
- Tipos permitidos: `image/jpeg`, `image/png`, `image/webp`, `image/gif`
- Arquivo vazio rejeitado

**Avaliação:** Correto e suficiente para o caso de uso.

---

### 2.9 Tratamento centralizado de exceções

- `@RestControllerAdvice` com mapeamento preciso por tipo de exceção
- Sem stack traces expostos ao cliente (mensagens genéricas para erros 500)
- Respostas JSON estruturadas (não HTML de erro do Tomcat)
- Exceções de segurança (401, 403) retornam JSON via `CustomAuthenticationEntryPoint` e `CustomAccessDeniedHandler`

**Avaliação:** Implementação robusta. O tratamento de 500 com mensagem genérica evita vazamento de detalhes internos.

---

### 2.10 Auditoria JPA

- `BaseEntity` registra `createdBy`, `createdDate`, `lastModifiedBy`, `lastModifiedDate`
- `JpaConfig.AuditorAware` usa o usuário logado (via `SecurityService`)
- Operações anônimas são atribuídas ao valor `"SISTEMA"`

**Avaliação:** Boa prática de rastreabilidade implementada corretamente.

---

### 2.11 Resposta padronizada para 401 e 403

- `CustomAuthenticationEntryPoint`: retorna `{ "status": 401, "message": "Token inválido ou ausente" }` em JSON
- `CustomAccessDeniedHandler`: retorna resposta JSON para 403
- Evita que o Spring Security retorne páginas HTML inesperadas

**Avaliação:** Correto para APIs consumidas por SPAs.

---

## 3. Vulnerabilidades e Riscos Identificados

---

### 3.1 [CRÍTICO] Typo no path de segurança — `/attractives/**` vs `/attractions/**`

**Arquivo:** `config/SecurityConfig.java`, linha 68

**Problema:**

```java
.requestMatchers(HttpMethod.GET,
    "/tourist-spots/**",
    "/attractives/**",   // ← ERRO: deveria ser /attractions/**
    "/host-points/**",
    ...
).permitAll()
```

O controller está mapeado em `/attractions` (`@RequestMapping("/attractions")`). A regra de segurança declara `/attractives/**` com typo. Como resultado:

- **Todos os endpoints GET de `/attractions/**` exigem autenticação** (o path não é reconhecido como público)
- Qualquer usuário não autenticado que tente acessar `GET /attractions` recebe **401 Unauthorized**
- A intenção era que estes endpoints fossem públicos (como os demais)

**Impacto:** Funcionalidade quebrada para usuários não autenticados ao tentar listar atrativos. Bloqueio inadvertido do público-alvo da aplicação.

**Recomendação:** Corrigir o path para `/attractions/**` no `SecurityConfig.java`.

---

### 3.2 [CRÍTICO] Ausência de `@EnableMethodSecurity` — `@PreAuthorize` pode ser ignorado

**Arquivo:** `config/SecurityConfig.java`

**Problema:**

A classe `SecurityConfig` possui apenas `@EnableWebSecurity`, sem `@EnableMethodSecurity`:

```java
@Configuration
@EnableWebSecurity
// @EnableMethodSecurity ← ausente
public class SecurityConfig { ... }
```

No Spring Security 6.x (usado no Spring Boot 3.x), a segurança em nível de método **não é habilitada automaticamente**. Sem `@EnableMethodSecurity`, as anotações `@PreAuthorize("hasRole('ADMIN')")` presentes nos controllers são **silenciosamente ignoradas**.

**Consequência:** Se confirmado em execução, qualquer usuário autenticado (independente de role) pode executar operações de criação, edição, exclusão e upload em todos os módulos, efetivamente tornando a distinção `ADMIN/USER` inoperante.

**Escopo do impacto:** Todos os endpoints protegidos por `@PreAuthorize`:
- `POST`, `PUT`, `DELETE` em todos os controllers de conteúdo
- `POST /{id}/photos` em todos os controllers
- `PATCH /users/{id}`, `DELETE /users/{id}`
- `DELETE /photos/{id}`

> **Observação:** Existe a possibilidade de que alguma dependência do projeto injete `@EnableMethodSecurity` indiretamente. Verificar este comportamento em execução é prioritário. Para garantia, adicionar `@EnableMethodSecurity` explicitamente à `SecurityConfig`.

**Recomendação:** Adicionar `@EnableMethodSecurity` (prePostEnabled habilitado por padrão):

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig { ... }
```

---

### 3.3 [ALTO] URL de redirect OAuth2 hardcoded no código

**Arquivo:** `security/SocialLoginSuccessHandler.java`, linha 56

**Problema:**

```java
response.sendRedirect("http://localhost:5173/login-success?token=" + token);
```

A URL de redirect após autenticação Google está **hardcoded** diretamente no código-fonte. Isso significa:

1. Em produção, o redirect irá para `localhost:5173` do servidor, que não existe
2. Não há como configurar via `application.yaml` sem modificar o código
3. O token JWT é **passado diretamente na URL** como query parameter, expondo-o em logs de servidor, histórico do navegador e cabeçalhos `Referer` se o usuário navegar para outra página

**Impacto:** OAuth2 não funciona em produção. Risco de exposição do token via URL.

**Recomendações:**
1. Mover a URL base para `application.yaml`: `oauth2.redirect-url: ${FRONTEND_URL}/login-success`
2. Considerar transmitir o token via cookie `HttpOnly` com `SameSite=Strict` ou via `POST` ao invés de query parameter

---

### 3.4 [ALTO] `ddl-auto: update` no perfil de produção

**Arquivo:** `src/main/resources/application.yaml`, linha 67

**Problema:**

```yaml
# PERFIL DE PRODUÇÃO
spring:
  jpa:
    hibernate:
      ddl-auto: update  # ← perigoso em produção
```

`ddl-auto: update` instrui o Hibernate a modificar o schema do banco de dados automaticamente ao inicializar a aplicação. Em produção isso representa:

- Alterações de schema sem controle ou revisão
- Potencial de corrupção de dados em caso de refatoração de entidades
- Impossibilidade de auditoria das migrações executadas
- Nenhuma estratégia de rollback

**Recomendação:** Adotar uma ferramenta de migração controlada (Flyway ou Liquibase) e setar `ddl-auto: validate` ou `ddl-auto: none` em produção.

---

### 3.5 [ALTO] `GET /users` e `GET /users/{id}` sem restrição de role

**Arquivo:** `controller/UserController.java`

**Problema:**

Os endpoints de listagem e busca de usuários não possuem `@PreAuthorize`:

```java
@GetMapping
public ResponseEntity<Page<UserResponseDTO>> findAll(...) { ... } // sem anotação

@GetMapping("/{id}")
public ResponseEntity<UserResponseDTO> getById(@PathVariable UUID id) { ... } // sem anotação
```

Esses endpoints não estão na lista de paths públicos do `SecurityConfig`. Portanto, qualquer usuário **autenticado com role USER** pode:
- Listar todos os usuários do sistema (incluindo admins)
- Ver nome, sobrenome, username, e-mail e role de qualquer usuário
- Buscar usuários por username (com filtro parcial)

**Impacto:** Exposição desnecessária de dados pessoais de todos os usuários. Violação do princípio do menor privilégio. Possível implicação com LGPD (exposição de dados de terceiros sem necessidade).

**Recomendação:** Adicionar `@PreAuthorize("hasRole('ADMIN')")` em `findAll` e `getById`, e criar um endpoint `GET /users/me` para o usuário logado acessar apenas seus próprios dados.

---

### 3.6 [MÉDIO] `UserUpdateDTO` expõe campo `role` sem restrição adicional

**Arquivo:** `dto/update/UserUpdateDTO.java`

**Problema:**

```java
public record UserUpdateDTO(
    String firstName,
    String lastName,
    String username,
    String email,
    String password,
    Role role    // ← campo sensível
)
```

O campo `role` permite que um ADMIN altere a role de qualquer usuário via `PATCH /users/{id}`. Isso é presumivelmente intencional (gestão de usuários). No entanto:

1. Não há validação de negócio impedindo que um ADMIN rebaixe a si mesmo para USER
2. Não há log de auditoria específico para mudanças de role
3. Se `@EnableMethodSecurity` estiver ausente (ver 3.2), qualquer usuário autenticado pode promover a si mesmo para ADMIN

**Recomendação:** Separar o update de role em um endpoint dedicado (`PATCH /users/{id}/role`) com `@PreAuthorize("hasRole('ADMIN')")` explícito, e adicionar log de auditoria para esse evento.

---

### 3.7 [MÉDIO] `TourGuideRequestDTO` aceita qualquer string como e-mail

**Arquivo:** `dto/request/TourGuideRequestDTO.java`

**Problema:**

```java
public record TourGuideRequestDTO(
    ...
    @NotBlank(message = "Email obrigatório!") String email, // ← sem @Email
    ...
)
```

Todos os outros DTOs que possuem campo `email` usam `@Email`. O `TourGuideRequestDTO` usa apenas `@NotBlank`. Isso permite que qualquer string não vazia seja armazenada como e-mail de guia turístico.

**Impacto:** Dados inconsistentes no banco, possíveis falhas ao tentar enviar e-mails para guias turísticos por sistemas externos.

---

### 3.8 [MÉDIO] Token JWT exposto como query parameter no fluxo OAuth2

**Contexto:** Já mencionado em 3.3, mas merece destaque próprio.

O token JWT é transmitido via URL (`?token=eyJ...`). Além dos riscos citados, query parameters são:
- Armazenados no histórico do navegador
- Incluídos em `Referer` headers em navegações subsequentes
- Registrados em logs de acesso de servidores/proxies/CDNs
- Visíveis na barra de endereços (phishing facilitado por shoulder surfing)

**Recomendação:** Transmitir o token via `Authorization` header após um POST inicial, ou via cookie `HttpOnly; Secure; SameSite=Strict`.

---

### 3.9 [MÉDIO] `POST /photos` genérico sem vinculação obrigatória

**Arquivo:** `controller/PhotoController.java`, `service/PhotoService.java`

**Problema:** O endpoint `POST /photos` cria fotos no S3 sem vinculá-las a nenhuma entidade. O arquivo é enviado ao S3, a URL e `storageKey` são salvas no banco, mas os campos `touristSpot`, `event`, `tourGuide` e `airport` ficam `null`.

Isso pode resultar em:
- Arquivos órfãos no S3 que nunca serão exibidos
- Consumo de storage desnecessário sem limpeza automática

**Recomendação:** Deprecar ou remover o `POST /photos` genérico, incentivando o uso dos endpoints específicos `POST /{modulo}/{id}/photos`. Ou implementar um mecanismo de limpeza de fotos órfãs.

---

### 3.10 [BAIXO] `parseInt` sem tratamento de exceção no upload de fotos

**Arquivo:** Todos os controllers com upload de foto (ex.: `AttractionController.java`, linha 85)

**Problema:**

```java
displayOrder != null ? Integer.parseInt(displayOrder) : null
```

O campo `displayOrder` chega como `String` no FormData e é convertido via `Integer.parseInt`. Se o frontend enviar um valor não numérico (ex.: `"abc"`), ocorre `NumberFormatException`. Esta exceção não é mapeada no `GlobalExceptionHandler`, portanto cai no handler genérico de `Exception.class` que retorna 500 Internal Server Error, quando o correto seria 400 Bad Request.

**Recomendação:** Capturar `NumberFormatException` no `GlobalExceptionHandler` mapeando para 400, ou usar `Integer.valueOf()` com try-catch no próprio controller.

---

## 4. Pontos de Melhoria sem Vulnerabilidade Imediata

### 4.1 `formLogin` habilitado desnecessariamente

**Arquivo:** `config/SecurityConfig.java`, linha 46

```java
.formLogin(Customizer.withDefaults())
```

Esta linha habilita endpoints `GET /login` e `POST /login` do Spring Security com formulário HTML. Como a API é inteiramente baseada em JWT e nunca usa sessions/cookies de autenticação, esses endpoints são desnecessários e representam superfície de ataque adicional.

**Recomendação:** Desabilitar: `.formLogin(AbstractHttpConfigurer::disable)`

---

### 4.2 Ausência de mecanismo de refresh de token

O JWT expira em 24 horas sem possibilidade de renovação. O usuário é forçado a fazer login completo após expiração. Para aplicações com sessões longas de usuário (admin usando o painel), isso é um inconveniente.

**Recomendação:** Implementar refresh token com tempo de vida maior (ex.: 7 dias), rotacionado a cada uso, armazenado de forma segura (HttpOnly cookie ou banco de dados).

---

### 4.3 CORS hardcoded para `localhost`

**Arquivo:** `config/SecurityConfig.java`

As origens permitidas no CORS estão hardcoded para `localhost`. Em produção, o domínio real do frontend precisará ser adicionado. Sem isso, o frontend em produção não conseguirá fazer requisições à API.

**Recomendação:** Carregar as origens permitidas de variável de ambiente:

```yaml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173}
```

---

### 4.4 Ausência de logging de eventos de segurança

Não há registro de log para:
- Tentativas de login com falha
- Tentativas de acesso não autorizado (403)
- Promoções/rebaixamentos de role de usuário
- Uploads de arquivos (quem enviou, quando)

**Recomendação:** Adicionar logging em `CustomAuthenticationProvider` (falhas de autenticação) e `GlobalExceptionHandler` (eventos 401/403), usando um logger com nível `WARN` e incluindo IP do cliente.

---

### 4.5 `allow-credentials: true` sem configuração explícita em produção

`config.setAllowCredentials(true)` em conjunto com origens dinâmicas de produção exige atenção. Se em produção o CORS for alterado para aceitar todos os subdomínios (`*.parqueubajara.com`), a combinação com `allowCredentials` pode ser problemática.

**Recomendação:** Documentar e revisar o CORS antes de ir para produção com múltiplos subdomínios.

---

### 4.6 Ausência de rate limiting nos endpoints de autenticação

`POST /auth/login` e `POST /auth/register` não têm proteção contra força bruta ou criação massiva de contas.

**Recomendação:** Implementar rate limiting por IP (ex.: Bucket4j, Spring Boot Actuator + filtro customizado) ou configurar no nível de proxy/CDN (Nginx, Cloudflare).

---

### 4.7 Token JWT sem claims de role

O JWT contém apenas o e-mail como `subject`. Roles são consultadas no banco a cada requisição. Embora garantido em frescor, gera uma query adicional por request.

**Trade-off:** Incluir roles no token aumenta a performance mas atrasa a propagação de revogação de roles (até expiração do token). A escolha atual é conservadora e correta para o contexto, mas deve ser documentada.

---

### 4.8 `TouristSpotController` não retorna header `Location` no POST

**Arquivo:** `controller/TouristSpotController.java`

```java
return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
// Sem generateHeaderLocation(spot.getId())
```

Diferente de todos os outros controllers (que implementam `GenericController`), o `TouristSpotController` não retorna o header `Location` com a URL do recurso criado. Isso quebra a consistência da API REST.

---

## 5. Boas Práticas Faltantes

| # | Prática                                     | Status       | Impacto                           |
|---|---------------------------------------------|--------------|-----------------------------------|
| 1 | `@EnableMethodSecurity` explícito           | Ausente      | Pode invalidar toda autorização por método |
| 2 | Flyway / Liquibase para migrações           | Ausente      | Risco de schema drift em produção |
| 3 | Rate limiting em endpoints de auth          | Ausente      | Vulnerável a força bruta          |
| 4 | Refresh token                               | Ausente      | UX degradada e logout forçado     |
| 5 | Log de eventos de segurança                 | Ausente      | Sem auditoria de incidentes       |
| 6 | `@Email` em `TourGuideRequestDTO`           | Ausente      | Dados inconsistentes              |
| 7 | `acceptsReservation` em `RestaurantRequestDTO` | Ausente   | Campo não configurável na criação |
| 8 | CORS configurável por ambiente              | Ausente      | Bloqueio em produção              |
| 9 | URL de redirect OAuth2 em config            | Ausente      | OAuth2 inoperante em produção     |
| 10| `@PreAuthorize` em GET `/users`             | Ausente      | Exposição de dados de usuários    |
| 11| `GET /users/me`                             | Ausente      | Usuário não consegue ver seu perfil facilmente |
| 12| `formLogin` desabilitado                    | Não feito    | Superfície de ataque desnecessária |
| 13| Header `Location` em POST `/tourist-spots`  | Ausente      | Inconsistência na API             |
| 14| Tratamento de `NumberFormatException` (displayOrder) | Ausente | 500 em vez de 400 |

---

## 6. Checklist de Segurança

| # | Item                                                                             | Status               | Prioridade |
|---|----------------------------------------------------------------------------------|----------------------|------------|
| 1 | **Spring Security ativo com configuração explícita**                            | ✅ Sim                | —          |
| 2 | **Senhas hashadas com BCrypt (force ≥ 10)**                                     | ✅ Sim (10)           | —          |
| 3 | **JWT assinado com chave segura (env var)**                                     | ✅ Sim                | —          |
| 4 | **CSRF desabilitado (correto para API stateless)**                              | ✅ Sim                | —          |
| 5 | **CORS configurado com origens explícitas**                                     | ✅ Sim (dev)          | —          |
| 6 | **Credenciais em variáveis de ambiente (sem hardcode)**                         | ✅ Sim                | —          |
| 7 | **Exceções tratadas sem expor stack trace**                                     | ✅ Sim                | —          |
| 8 | **Validação de entrada nos DTOs (Bean Validation)**                             | ✅ Parcial            | Médio      |
| 9 | **Validação de arquivos (tipo + tamanho)**                                      | ✅ Sim                | —          |
| 10| **Respostas 401/403 em JSON (não HTML)**                                        | ✅ Sim                | —          |
| 11| **Senha não exposta nas respostas da API**                                      | ✅ Sim                | —          |
| 12| **Auditoria JPA (createdBy/modifiedBy)**                                        | ✅ Sim                | —          |
| 13| **`@EnableMethodSecurity` declarado**                                           | ⚠️ Não encontrado    | **Crítico** |
| 14| **Path correto em `SecurityConfig` para `/attractions/**`**                     | ❌ Typo (`/attractives/**`) | **Crítico** |
| 15| **URL de redirect OAuth2 configurável por ambiente**                            | ❌ Hardcoded          | **Alto**   |
| 16| **`ddl-auto: validate` ou `none` em produção**                                 | ❌ `update` em prod  | **Alto**   |
| 17| **`GET /users` restrito a ADMIN**                                               | ❌ Apenas autenticado | **Alto**   |
| 18| **`formLogin` desabilitado**                                                    | ❌ Habilitado         | Médio      |
| 19| **Rate limiting em `/auth/login` e `/auth/register`**                          | ❌ Ausente            | Médio      |
| 20| **CORS configurável por ambiente (produção)**                                   | ❌ Hardcoded          | Médio      |
| 21| **Token JWT não exposto em URL (OAuth2)**                                       | ❌ Exposto em ?token= | Médio      |
| 22| **Refresh token implementado**                                                  | ❌ Ausente            | Médio      |
| 23| **Logging de eventos de segurança (401, 403, login falho)**                     | ❌ Ausente            | Médio      |
| 24| **`@Email` em `TourGuideRequestDTO`**                                           | ❌ Ausente            | Baixo      |
| 25| **`acceptsReservation` em `RestaurantRequestDTO`**                              | ❌ Ausente            | Baixo      |
| 26| **Tratamento de `NumberFormatException` em uploads**                            | ❌ Ausente            | Baixo      |
| 27| **Migração de schema via Flyway/Liquibase**                                     | ❌ Ausente            | Alto       |
| 28| **`@PreAuthorize` em `GET /users/{id}` e `GET /users`**                        | ❌ Ausente            | Alto       |
| 29| **Header `Location` em `POST /tourist-spots`**                                 | ❌ Ausente            | Baixo      |
| 30| **`formLogin` desabilitado explicitamente**                                    | ❌ Ativo              | Médio      |

---

### Resumo por prioridade

| Prioridade | Quantidade | Itens                                              |
|------------|------------|----------------------------------------------------|
| **Crítico** | 2         | #13 `@EnableMethodSecurity`, #14 typo no path      |
| **Alto**    | 5         | #15, #16, #17, #27, #28                            |
| **Médio**   | 6         | #18, #19, #20, #21, #22, #23                      |
| **Baixo**   | 4         | #24, #25, #26, #29                                 |

---

### Ordem de correção recomendada

1. **Verificar e ativar `@EnableMethodSecurity`** — risco de autorização completamente inoperante
2. **Corrigir typo `/attractives/**` → `/attractions/**`** no `SecurityConfig`
3. **Restringir `GET /users` e `GET /users/{id}` a ADMIN** — exposição de dados pessoais
4. **Substituir `ddl-auto: update` por Flyway/Liquibase** em produção
5. **Mover URL de redirect OAuth2 para variável de ambiente**
6. **Mover CORS para configuração por ambiente**
7. **Implementar rate limiting** nos endpoints de autenticação
8. **Desabilitar `formLogin`**
9. **Adicionar `@Email` em `TourGuideRequestDTO`** e `acceptsReservation` em `RestaurantRequestDTO`
10. **Tratar `NumberFormatException`** no `GlobalExceptionHandler`

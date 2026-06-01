# Viva Ubajara API

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.0-6DB33F?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?logo=postgresql&logoColor=white)
![AWS](https://img.shields.io/badge/AWS_S3_%2B_CloudFront-232F3E?logo=amazons3&logoColor=white)
![JWT](https://img.shields.io/badge/JWT_%2B_OAuth2-black?logo=jsonwebtokens)
![CI/CD](https://img.shields.io/badge/GitHub_Actions-CI%2FCD-2088FF?logo=githubactions&logoColor=white)
![Status](https://img.shields.io/badge/Status-Em%20desenvolvimento-yellow)

API REST desenvolvida sob demanda da **Prefeitura Municipal de Ubajara — Secretaria de Turismo** para um catálogo turístico da cidade. O sistema centraliza pontos turísticos, atrações, hospedagens, restaurantes, eventos e guias de turismo, servindo como infraestrutura para o site oficial e com potencial de evolução para um marketplace turístico regional.

🖥️ [Ver repositório do frontend](https://github.com/gabsiq73/viva_ubajara_frontend) · 📖 [Swagger UI](https://parque-ubajara-api.onrender.com/api/v1/swagger-ui/index.html)

---

## Tecnologias

| Camada        | Tecnologia                                              |
|---------------|---------------------------------------------------------|
| Linguagem     | Java 21                                                 |
| Framework     | Spring Boot 3.5.0                                       |
| Segurança     | Spring Security + JWT (jjwt 0.12.5) + OAuth2 Google    |
| Persistência  | Spring Data JPA + Hibernate + PostgreSQL 16             |
| Mapeamento    | MapStruct 1.6.3                                         |
| Storage       | AWS S3 (bucket privado) + CloudFront CDN                |
| Documentação  | Springdoc OpenAPI 2.8.5 (Swagger)                       |
| Testes        | JUnit 5 + Mockito + SpringBootTest + MockMvc            |
| CI/CD         | GitHub Actions                                          |
| Containers    | Docker (multi-stage build)                              |
| Deploy        | Render (API) + Vercel (frontend)                        |

---

## Arquitetura e decisões técnicas

**Herança com `InheritanceType.JOINED`** — as entidades `Attraction`, `HostPoint` e `Restaurant` estendem `TouristSpot`, que centraliza os campos comuns. A estratégia JOINED foi escolhida para evitar colunas nulas e manter integridade relacional sem desnormalização.

**Storage desacoplado via interface** — `StorageService` abstrai o acesso ao S3. O upload retorna uma `storageKey` que é persistida na entidade `Photo`; a URL pública é gerada sob demanda via CloudFront. Isso permite trocar o provedor de storage sem alterar as camadas de negócio.

**Autenticação dual** — o sistema suporta login tradicional (email + senha com BCrypt) e login social via OAuth2 Google. Ambos os fluxos geram um JWT próprio, mantendo a autenticação stateless em toda a API.

**Segurança por método HTTP** — rotas `GET` são públicas para consumo pelo frontend sem autenticação. Operações `POST`, `PUT`, `PATCH` e `DELETE` exigem token JWT com role `ADMIN`.

**DTOs com separação por intenção** — os pacotes `request/`, `response/` e `update/` organizam os contratos de entrada e saída por caso de uso, evitando expor entidades JPA diretamente e mantendo flexibilidade para evoluir cada contrato de forma independente.

---

## Estrutura do projeto

```
io.github.parqueubajara.api
├── config/           # S3, JPA, Swagger, Password encoder
├── controller/       # Endpoints REST
├── dto/
│   ├── request/      # Contratos de entrada (criação)
│   ├── response/     # Contratos de saída
│   └── update/       # Contratos de entrada (atualização parcial)
├── exception/        # Exceções de domínio
├── handler/          # GlobalExceptionHandler + StandardError
├── mapper/           # MapStruct — entidade ↔ DTO
├── model/
│   └── enums/        # AttractionType, HostType, Role, PaymentMethod
├── repository/       # Interfaces Spring Data JPA
├── security/         # JWT, OAuth2, filtros e providers
└── service/
    └── infra/        # S3StorageService, FileValidationService
```

---

## Como rodar

**Pré-requisitos:** Java 21, Maven, Docker

**1. Clone o repositório**
```bash
git clone https://github.com/felipe-damasceno19/viva-ubajara-api.git
cd viva-ubajara-api
```

**2. Configure as variáveis de ambiente**
```bash
cp .env.example .env
```

```env
DATASOURCE_URL=jdbc:postgresql://localhost:5432/ubajara_db
DATASOURCE_USERNAME=postgres
DATASOURCE_PASSWORD=postgres

JWT_SECRET=sua_chave_base64_minimo_32_chars

AWS_ACCESS_KEY=sua_access_key
AWS_SECRET_KEY=sua_secret_key
CLOUDFRONT_URL=https://xxxxxxxxxx.cloudfront.net

CLIENT_ID=google_oauth2_client_id
CLIENT_SECRET=google_oauth2_client_secret
```

**3. Suba o banco de dados**
```bash
docker-compose up -d
```

**4. Execute a aplicação**
```bash
./mvnw spring-boot:run
```

Swagger disponível em `http://localhost:8080/api/v1/swagger-ui.html`.

---

## Como rodar com Docker

```bash
docker build -t parque-ubajara-api .

docker run -p 8080:8080 \
  -e DATASOURCE_URL=jdbc:postgresql://host/ubajara_db \
  -e DATASOURCE_USERNAME=postgres \
  -e DATASOURCE_PASSWORD=postgres \
  -e JWT_SECRET=sua_secret \
  -e AWS_ACCESS_KEY=sua_key \
  -e AWS_SECRET_KEY=sua_secret_key \
  -e CLIENT_ID=seu_client_id \
  -e CLIENT_SECRET=seu_client_secret \
  -e CLOUDFRONT_URL=https://xxx.cloudfront.net \
  -e FRONTEND_URL=https://seu-frontend
  -e FRONTEND_REDIRECT_URL=https://seu-frontend/login-success
  parque-ubajara-api
```

---

## CI/CD

O pipeline do GitHub Actions executa a cada push e PR em `main` e `develop`:

1. Sobe um container PostgreSQL 16 temporário
2. Instala Java 21 e concede permissão ao `mvnw`
3. Executa `./mvnw clean verify` (build + testes)
4. Publica relatório de testes como artefato

Secrets necessários no repositório: `JWT_SECRET`, `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`, `CLIENT_ID`, `CLIENT_SECRET`, `CLOUDFRONT_URL`.

---

## Padrões adotados

- **Conventional Commits** em inglês (`feat:`, `fix:`, `refactor:`, `ci:`, `chore:`, `docs:`)
- **GitHub Projects com Scrum** — 6 sprints de 1 semana
- Nenhuma credencial hardcoded — todas via variáveis de ambiente
- `application.yml` commitado sem valores sensíveis

---

## Autor

Desenvolvido por [Felipe Damasceno](https://github.com/felipe-damasceno19) | [Gabriel Siqueira](https://github.com/gabsiq63).  
Frontend desenvolvido em colaboração com [Gabriel Siqueira](https://github.com/).

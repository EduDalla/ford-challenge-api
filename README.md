# Pulso Retencao API

Backend Java/Spring Boot usado como BFF da solucao Pulso Retencao para apoiar consultores na identificacao, priorizacao e recuperacao de clientes com risco de evasao no pos-venda Ford.

A API integra o app/mobile ou Swagger com servicos internos, Supabase Auth/PostgreSQL e a FastAPI de Machine Learning. O README esta organizado conforme os criterios de avaliacao da disciplina e tambem funciona como contrato tecnico dos endpoints REST.

## Sumario

- [1. Integracao por Web Services (50%)](#1-integracao-por-web-services-50)
- [2. Arquitetura Orientada a Servicos - SOA (20%)](#2-arquitetura-orientada-a-servicos---soa-20)
- [3. Padroes e Boas Praticas (15%)](#3-padroes-e-boas-praticas-15)
- [4. Conexao com Banco de Dados (15%)](#4-conexao-com-banco-de-dados-15)
- [5. Como Executar](#5-como-executar)
- [6. Funcionalidades](#6-funcionalidades)
- [7. Contrato dos Endpoints](#7-contrato-dos-endpoints)
- [8. Deploy, CI/CD e Demonstracao](#8-deploy-cicd-e-demonstracao)

## 1. Integracao por Web Services (50%)

### 1.1 Desenho de arquitetura contendo os componentes usados (10%)

```text
Consultor / Gestor
      |
      v
Mobile React Native/Expo ou Swagger UI
      |
      | HTTPS + JSON + Bearer JWT
      v
Java Spring Boot BFF - Pulso Retencao API
      |
      |-- Spring Security: JWT, RBAC, CORS e filtros stateless
      |-- Controllers REST: contrato HTTP consumido pelo mobile e por integracoes
      |-- Services: regras de negocio de clientes, missoes, interacoes e ML
      |-- Repositories JPA/JDBC: persistencia e consultas no PostgreSQL
      |
      | JDBC + Flyway
      v
Supabase PostgreSQL
      |
      |-- auth.users: autenticacao humana via Supabase Auth
      |-- public.profiles: perfil funcional usado pelo backend
      |-- public.clientes, veiculos, missoes, interacoes, informacoes
      |-- public.vin_share_feature_snapshots, ml_feature_refresh_queue, predicao_resultados
      |-- private.api_clients, permissions, api_client_permissions
      |
      | HTTPS + X-ML-Service-Token
      v
FastAPI ML no Render
      |
      |-- /predict
      |-- /predict-batch
      v
Predicao de perfil, risco, score, motivo, canal e acao recomendada

CI/CD GitHub Actions
      |
      |-- ./mvnw test
      |-- docker build
      |-- Trivy scan
      |-- push para GHCR
      v
Render / Container Docker
```

Responsabilidades dos componentes:

| Componente | Responsabilidade |
| --- | --- |
| Mobile/Swagger | Cliente consumidor do contrato REST. Envia JSON e autentica por Bearer token. |
| Spring Boot BFF | Camada de integracao, seguranca, regras de negocio e orquestracao entre sistemas. |
| Supabase Auth | Autenticacao de usuarios humanos e emissao do JWT Supabase. |
| Supabase PostgreSQL | Banco transacional da aplicacao, incluindo dominio de retencao, missoes, fila ML e integracoes privadas. |
| FastAPI ML | Servico externo de IA/ML para predicao de churn, risco e acao recomendada. |
| Flyway | Versionamento e aplicacao automatica das migrations do banco. |
| Render/GHCR | Publicacao e execucao em container Docker. |

### 1.2 Implementacao de APIs RESTful para comunicacao entre sistemas (20%)

O projeto adota Web Services RESTful com JSON como formato de troca de dados. Os contratos sao expostos por controllers Spring MVC e documentados pelo README e pelo Swagger/OpenAPI em ambiente fora de producao.

Principais integracoes REST:

| Integracao | Protocolo | Formato | Autenticacao | Objetivo |
| --- | --- | --- | --- | --- |
| Mobile/Swagger -> Java BFF | HTTPS REST | JSON | Bearer JWT Supabase ou service token | Consumir funcionalidades de retencao, missoes, indicadores e ML. |
| Java BFF -> FastAPI ML | HTTPS REST | JSON | `X-ML-Service-Token` | Enviar features e receber predicoes de risco/churn. |
| Java BFF -> Supabase/PostgreSQL | JDBC | SQL | Usuario e senha do banco via env vars | Persistir clientes, missoes, interacoes, fila ML e resultados. |
| Java BFF -> Supabase Auth/JWKS | HTTPS | JSON/JWKS | URL publica JWKS ou secret legacy | Validar tokens JWT de usuarios humanos. |

O projeto nao usa SOAP, XML ou WSDL porque a arquitetura escolhida e REST + JSON + OpenAPI, padrao adequado para integracao com mobile, Swagger e servicos web modernos.

### 1.3 Uso adequado de metodos HTTP (10%)

| Metodo | Uso no projeto | Exemplos |
| --- | --- | --- |
| `GET` | Consulta de recursos sem alterar estado. | `/health`, `/api/v1/me`, `/api/v1/missoes`, `/api/v1/radar/prioridades`, `/api/v1/clientes/{id}` |
| `POST` | Criacao de recursos ou disparo de processamento. | `/api/v1/auth/service-token`, `/api/v1/missoes`, `/api/v1/missoes/{id}/acoes`, `/api/ml/predict` |
| `PUT` | Atualizacao completa de cadastros. | `/api/v1/clientes/{id}`, `/api/v1/informacoes/{id}`, `/api/v1/informacoes-user/{id}` |
| `PATCH` | Atualizacao parcial de estado. | `/api/v1/missoes/{id}/status` |
| `DELETE` | Exclusao logica ou remocao de vinculos. | `/api/v1/clientes/{id}`, `/api/v1/interacoes/{id}`, `/api/v1/informacoes-user/{id}` |
| `OPTIONS` | Preflight CORS. | Permitido globalmente para integracao com front-end/mobile. |

### 1.4 Documentacao das APIs com README e Swagger como contrato (10%)

O contrato dos endpoints esta descrito na secao [7. Contrato dos Endpoints](#7-contrato-dos-endpoints). Alem disso, o projeto possui Swagger/OpenAPI via `springdoc-openapi-starter-webmvc-ui`.

URLs de documentacao quando o profile nao e `prod`:

```text
GET /swagger-ui.html
GET /swagger-ui/**
GET /v3/api-docs
GET /v3/api-docs/**
```

No profile `prod`, Swagger e OpenAPI ficam desabilitados por seguranca:

```properties
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

## 2. Arquitetura Orientada a Servicos - SOA (20%)

### 2.1 Organizacao modular baseada em servicos independentes e reutilizaveis (10%)

A aplicacao e dividida por dominios de negocio e servicos reutilizaveis. Cada modulo possui responsabilidade clara e pode ser evoluido sem misturar regras de outros dominios.

| Modulo | Responsabilidade |
| --- | --- |
| `autenticacao` | Emissao de token tecnico para clientes de integracao. |
| `cliente` | CRUD, filtros e soft delete de clientes. |
| `interacao` | Registro e consulta de contatos/acoes de retencao por cliente. |
| `informacao` | Catalogo de informacoes relevantes para consultores. |
| `informacaouser` | Vinculo de informacoes/alertas a usuarios consultores. |
| `missao` | Radar de prioridades, missoes, cartoes de recuperacao, acoes, resultados e indicadores. |
| `ml` | BFF REST para chamada da FastAPI ML e processamento batch. |
| `mlpipeline` | Feature snapshots, fila de predicao, resultados ML, jobs e logs de execucao. |
| `integracao` | Clientes tecnicos, permissoes e credenciais privadas. |
| `profile` | Perfil funcional do usuario autenticado no Supabase. |
| `infra.security` | JWT, RBAC, CORS, filtros e configuracao stateless. |
| `shared.exception` | Tratamento centralizado de erros da API. |

### 2.2 Separacao clara entre apresentacao, servico e dados (10%)

O projeto segue separacao por camadas:

| Camada | Pacotes | Papel |
| --- | --- | --- |
| Apresentacao/API | `*.api`, `controller` | Controllers REST, DTOs de request/response, validacoes de entrada e contrato HTTP. |
| Servico/Negocio | `*.service` | Regras de negocio, orquestracao de processos, autorizacao funcional e chamadas externas. |
| Dominio | `*.domain`, `*.enums` | Entidades, enums e conceitos centrais do negocio. |
| Dados | `*.repository`, `*.persistence` | Spring Data JPA, JDBC customizado e acesso ao PostgreSQL. |
| Infraestrutura | `infra.config`, `infra.security` | Configuracoes de OpenAPI, RestClient, Jackson, CORS e seguranca. |
| Compartilhado | `shared.api`, `shared.exception` | Padrao de erro e excecoes reutilizaveis. |

Essa separacao permite que controllers apenas recebam e respondam requisicoes, services concentrem regras de negocio e repositories isolem detalhes de persistencia.

## 3. Padroes e Boas Praticas (15%)

### 3.1 Adoção de padroes REST, JSON e OpenAPI (8%)

Padroes aplicados no projeto:

| Padrao | Aplicacao no projeto |
| --- | --- |
| REST | Recursos versionados em `/api/v1`, metodos HTTP semanticos e respostas HTTP padronizadas. |
| JSON | Entrada e saida dos endpoints REST, payloads ML e respostas de erro. |
| OpenAPI/Swagger | Contrato navegavel da API em ambiente local/dev. |
| DTOs | Requests e responses separados das entidades de dominio. |
| Bean Validation | Validacao declarativa com `@Valid`, `@Min`, `@Max` e constraints nos DTOs. |
| JWT | Autenticacao stateless com Bearer token Supabase e JWT tecnico interno. |
| RBAC | Controle de acesso por roles `ADMIN`, `GESTOR`, `ANALISTA` e scopes tecnicos. |
| CORS | Origens configuraveis por `CORS_ALLOWED_ORIGINS`, com bloqueio de wildcard em `prod`. |
| Soft delete | Exclusao logica em entidades como clientes e interacoes. |
| Externalized config | Secrets, URLs e parametros de jobs configurados por variaveis de ambiente. |

SOAP, XML e WSDL sao padroes validos para Web Services em outros contextos, mas este projeto usa REST + JSON + OpenAPI por ser mais direto para mobile, BFF e integracoes HTTP modernas.

### 3.2 Tratamento adequado de erros e excecoes nos servicos (7%)

O tratamento de erros e centralizado em `GlobalExceptionHandler`, evitando respostas inconsistentes entre controllers.

Excecoes tratadas:

| Excecao | HTTP | Uso |
| --- | --- | --- |
| `ResourceNotFoundException` | `404 Not Found` | Recurso inexistente. |
| `BadRequestException` | `400 Bad Request` | Requisicao invalida ou regra de negocio nao atendida. |
| `DatabaseException` | `400 Bad Request` | Falha controlada relacionada a banco. |
| `AccessDeniedException` | `403 Forbidden` | Usuario autenticado sem permissao para o recurso. |
| `DataIntegrityViolationException` | `400 Bad Request` | Violacao de restricao do banco. |
| `MethodArgumentNotValidException` | `400 Bad Request` | Campos invalidos em DTOs com Bean Validation. |
| `ConstraintViolationException` | `400 Bad Request` | Parametros invalidos em request/query. |
| `HttpMessageNotReadableException` | `400 Bad Request` | JSON invalido ou enum/campo nao suportado. |
| `Exception` | `500 Internal Server Error` | Erro inesperado tratado de forma padronizada. |

Formato padrao de erro:

```json
{
  "timestamp": "2026-05-24T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados invalidos na requisicao.",
  "path": "/api/v1/clientes",
  "fields": {
    "nome": "nao deve estar em branco"
  }
}
```

## 4. Conexao com Banco de Dados (15%)

### 4.1 Dependencias e configuracoes para conexao (8%)

Dependencias relacionadas a banco e persistencia:

| Dependencia | Papel |
| --- | --- |
| `spring-boot-starter-data-jpa` | Mapeamento ORM, repositories e entidades. |
| `postgresql` | Driver JDBC para PostgreSQL/Supabase. |
| `spring-boot-starter-flyway` | Execucao das migrations no startup. |
| `flyway-database-postgresql` | Suporte Flyway especifico para PostgreSQL. |
| `h2` | Banco em memoria para testes automatizados. |
| `spring-boot-starter-data-jpa-test` | Suporte de testes para JPA. |
| `spring-boot-starter-flyway-test` | Suporte de testes para migrations. |

Profiles do projeto:

| Profile | Banco | Migrations | Uso |
| --- | --- | --- | --- |
| default/dev | PostgreSQL local, porta padrao `5433` | `classpath:db/migration-postgres` | Desenvolvimento local e Docker Compose. |
| `prod` | Supabase PostgreSQL | `classpath:db/migration-postgres` com baseline | Deploy Render/producao. |
| `test` | H2 em memoria | `classpath:db/migration` | Testes automatizados. |

Variaveis principais de conexao:

```env
DB_URL=jdbc:postgresql://localhost:5433/pulso_retencao
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/pulso_retencao
DB_USERNAME=postgres
SPRING_DATASOURCE_USERNAME=postgres
DB_PASSWORD=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

Outras variaveis importantes:

```env
JWT_SECRET=segredo_interno_para_service_tokens
SUPABASE_JWKS_URL=https://PROJECT_REF.supabase.co/auth/v1/.well-known/jwks.json
SUPABASE_JWT_ISSUER=https://PROJECT_REF.supabase.co/auth/v1
SUPABASE_JWT_AUDIENCE=authenticated
SUPABASE_JWT_SECRET=legacy_jwt_secret_opcional
FORD_ML_BASE_URL=https://ford-vinguard-api.onrender.com
ML_API_BASE_URL=https://ford-vinguard-api.onrender.com
FORD_ML_SERVICE_TOKEN=segredo_server_to_server_ml
CORS_ALLOWED_ORIGINS=http://localhost:8081
DEMO_MODE=false
JAVA_ML_DEMO_TOKEN=token_de_demo_opcional
```

### 4.2 Controle de migracoes (7%)

O projeto usa Flyway para versionar e aplicar alteracoes do banco de dados automaticamente.

Diretorios de migrations:

| Diretorio | Uso |
| --- | --- |
| `src/main/resources/db/migration-postgres` | Migrations PostgreSQL/Supabase usadas em `dev`, default e `prod`. |
| `src/main/resources/db/migration` | Migrations adaptadas para H2 no profile `test`. |

Migrations PostgreSQL principais:

| Migration | Conteudo |
| --- | --- |
| `V1__create_app_foundation.sql` | Schema privado, `profiles` e base da aplicacao. |
| `V2__create_retention_domain.sql` | Clientes, interacoes, informacoes e informacoes por usuario. |
| `V3__create_private_integrations.sql` | Clientes tecnicos, permissoes e vinculos de permissao. |
| `V4__seed_demo_data.sql` | Dados iniciais de demonstracao e permissoes. |
| `V5__harden_database_access.sql` | Reforco de RLS e acesso ao banco. |
| `V6__create_mission_domain.sql` | Veiculos, missoes, acoes e resultados. |
| `V7__create_feature_store_mock.sql` | Feature store, snapshots, fila ML e resultados de predicao. |
| `V8__enhance_ml_pipeline.sql` | Melhorias no pipeline ML, logs de jobs, indices e status. |

Schemas/tabelas relevantes:

```text
auth.users
public.profiles
public.clientes
public.veiculos
public.missoes
public.missao_acoes
public.missao_resultados
public.interacoes
public.informacoes
public.informacoes_user
public.vin_share_servicos
public.vin_share_feature_snapshots
public.ml_feature_refresh_queue
public.predicao_resultados
public.job_execution_logs
private.api_clients
private.permissions
private.api_client_permissions
```

## 5. Como Executar

### 5.1 Requisitos

- Java 17 instalado e `JAVA_HOME` configurado.
- Docker e Docker Compose para subir PostgreSQL e aplicacao em container.
- Maven Wrapper do projeto (`./mvnw`).
- Porta `8080` livre para a API.
- Porta `5433` livre para PostgreSQL local via Docker Compose.

### 5.2 Configuracao local com Docker Compose

Crie o arquivo `.env` a partir do exemplo:

```bash
cp .env.example .env
```

Edite os valores sensiveis:

```env
POSTGRES_DB=pulso_retencao
POSTGRES_USER=postgres
POSTGRES_PASSWORD=troque-esta-senha
JWT_SECRET=troque-este-segredo
POSTGRES_HOST_PORT=5433
APP_PORT=8080
TZ=America/Sao_Paulo
```

Suba banco e aplicacao:

```bash
docker compose up --build
```

Teste o health check:

```bash
curl http://localhost:8080/health
```

Resposta esperada:

```json
{"status":"ok"}
```

### 5.3 Rodar a aplicacao localmente com Maven

Suba apenas o PostgreSQL:

```bash
docker compose up -d postgres
```

Execute a aplicacao:

```bash
./mvnw spring-boot:run
```

Se quiser informar profile explicitamente:

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

### 5.4 Testes automatizados

Os testes usam H2 e migrations em `src/main/resources/db/migration`.

```bash
./mvnw test
```

Se o comando falhar com erro de `JAVA_HOME`, instale Java 17 e configure o ambiente antes de rodar novamente.

## 6. Funcionalidades

### 6.1 Autenticacao e autorizacao

- Validacao de usuario humano por JWT Supabase.
- Validacao por JWKS (`SUPABASE_JWKS_URL`) para chaves novas ECC/RSA.
- Fallback por `SUPABASE_JWT_SECRET` para tokens legacy HS256.
- Busca do usuario em `public.profiles` para transformar `perfil` em role Spring.
- Roles funcionais: `ADMIN`, `GESTOR`, `ANALISTA`.
- Emissao de service token tecnico por `/api/v1/auth/service-token`.
- Scopes tecnicos como `ml:predict`, convertidos para `SCOPE_ml:predict`.

### 6.2 Cadastros e operacao de retencao

- CRUD de clientes com filtros por termo, risco e status ativo.
- Registro e consulta de interacoes por cliente.
- Catalogo de informacoes relevantes para consultores.
- Vinculo de informacoes e alertas por usuario.
- Soft delete para preservar historico operacional.

### 6.3 Radar, missoes e indicadores

- Radar de prioridades para listar missoes ordenadas por risco, prioridade e score.
- Criacao de missao a partir de dados de cliente, veiculo e predicao ML.
- Consulta de missao por ID.
- Abertura de missao por cartao de recuperacao (`codigoCartao`).
- Atualizacao de status por `PATCH`.
- Registro de acoes de contato e acompanhamento.
- Registro de memoria/resultado da missao.
- Indicadores por consultor e indicadores agregados de retencao.

### 6.4 Integracao ML e pipeline batch

- BFF Java chama FastAPI ML em `/predict` usando `X-ML-Service-Token`.
- Endpoint `/api/ml/predict` retorna a resposta ML e uma sugestao de missao para o negocio.
- Endpoint `/api/v1/ml/predicoes/processar-lote` reserva snapshots da fila e chama `/predict-batch`.
- Jobs agendados renovam feature snapshots e processam predicoes pendentes.
- Resultados ficam gravados em `public.predicao_resultados`.
- Logs de execucao ficam em `public.job_execution_logs`.

Configuracoes dos jobs:

```env
APP_JOBS_REFRESH_FEATURE_SNAPSHOTS_ENABLED=true
APP_JOBS_REFRESH_FEATURE_SNAPSHOTS_CRON=0 */30 * * * *
APP_JOBS_REFRESH_FEATURE_SNAPSHOTS_BATCH_SIZE=100
APP_JOBS_PROCESSAR_PREDICOES_ML_ENABLED=true
APP_JOBS_PROCESSAR_PREDICOES_ML_CRON=0 0 23 * * SUN
APP_JOBS_PROCESSAR_PREDICOES_ML_BATCH_SIZE=100
APP_JOBS_PROCESSAR_PREDICOES_ML_MAX_ATTEMPTS=3
APP_JOBS_PROCESSAR_PREDICOES_ML_RETRY_DELAY_MINUTES=60
```

## 7. Contrato dos Endpoints

### 7.1 Endpoints publicos

| Metodo | Rota | Responsabilidade | Autenticacao | Modulo |
| --- | --- | --- | --- | --- |
| `GET` | `/` | Liveness simples da API. | Publica | `controller` |
| `GET` | `/health` | Health check da aplicacao. | Publica | `controller` |
| `GET` | `/healthCheck` | Alias de health check. | Publica | `controller` |
| `GET` | `/actuator/health` | Health check do Spring Actuator. | Publica | Actuator |
| `POST` | `/api/v1/auth/service-token` | Emite JWT tecnico para client de integracao. | Publica, valida credenciais no banco | `autenticacao` |
| `GET` | `/swagger-ui.html` | Interface Swagger em ambiente nao-prod. | Publica fora de `prod` | OpenAPI |
| `GET` | `/v3/api-docs` | Contrato OpenAPI JSON em ambiente nao-prod. | Publica fora de `prod` | OpenAPI |

### 7.2 Endpoints de identidade e seguranca

| Metodo | Rota | Responsabilidade | Autenticacao | Modulo |
| --- | --- | --- | --- | --- |
| `GET` | `/api/v1/me` | Retorna dados do usuario Supabase autenticado. | Bearer Supabase JWT | `me` |

### 7.3 Endpoints de clientes

| Metodo | Rota | Responsabilidade | Autenticacao | Modulo |
| --- | --- | --- | --- | --- |
| `POST` | `/api/v1/clientes` | Cadastra cliente. | `ADMIN` ou `GESTOR` | `cliente` |
| `GET` | `/api/v1/clientes` | Lista clientes com filtros e paginacao. | `ADMIN` ou `GESTOR` | `cliente` |
| `GET` | `/api/v1/clientes/{id}` | Busca cliente por ID. | `ADMIN` ou `GESTOR` | `cliente` |
| `PUT` | `/api/v1/clientes/{id}` | Atualiza cliente. | `ADMIN` ou `GESTOR` | `cliente` |
| `DELETE` | `/api/v1/clientes/{id}` | Exclui cliente logicamente. | `ADMIN` ou `GESTOR` | `cliente` |

Filtros aceitos em `GET /api/v1/clientes`:

```text
q
nivelRisco
ativo
page
size
sort
```

### 7.4 Endpoints de informacoes

| Metodo | Rota | Responsabilidade | Autenticacao | Modulo |
| --- | --- | --- | --- | --- |
| `POST` | `/api/v1/informacoes` | Cadastra informacao no catalogo. | `ADMIN` ou `GESTOR` | `informacao` |
| `GET` | `/api/v1/informacoes` | Lista informacoes. | `ADMIN`, `GESTOR` ou `ANALISTA` | `informacao` |
| `GET` | `/api/v1/informacoes/{id}` | Busca informacao por ID. | `ADMIN`, `GESTOR` ou `ANALISTA` | `informacao` |
| `PUT` | `/api/v1/informacoes/{id}` | Atualiza informacao. | `ADMIN` ou `GESTOR` | `informacao` |
| `DELETE` | `/api/v1/informacoes/{id}` | Desativa informacao. | `ADMIN` ou `GESTOR` | `informacao` |

### 7.5 Endpoints de informacoes por usuario

| Metodo | Rota | Responsabilidade | Autenticacao | Modulo |
| --- | --- | --- | --- | --- |
| `POST` | `/api/v1/informacoes-user` | Cria vinculo de informacao para usuario. | `ADMIN` ou `GESTOR` | `informacaouser` |
| `GET` | `/api/v1/informacoes-user` | Lista vinculos com filtros. | `ADMIN` ou `GESTOR` | `informacaouser` |
| `GET` | `/api/v1/informacoes-user/{id}` | Busca vinculo por ID. | `ADMIN` ou `GESTOR` | `informacaouser` |
| `PUT` | `/api/v1/informacoes-user/{id}` | Atualiza vinculo. | `ADMIN` ou `GESTOR` | `informacaouser` |
| `DELETE` | `/api/v1/informacoes-user/{id}` | Exclui vinculo. | `ADMIN` ou `GESTOR` | `informacaouser` |

Filtros aceitos em `GET /api/v1/informacoes-user`:

```text
userId
informacaoId
dataAlerta
page
size
sort
```

### 7.6 Endpoints de interacoes

| Metodo | Rota | Responsabilidade | Autenticacao | Modulo |
| --- | --- | --- | --- | --- |
| `POST` | `/api/v1/clientes/{clienteId}/interacoes` | Registra interacao de retencao para cliente. | `ADMIN` ou `GESTOR` | `interacao` |
| `GET` | `/api/v1/clientes/{clienteId}/interacoes` | Lista interacoes de um cliente. | `ADMIN` ou `GESTOR` | `interacao` |
| `GET` | `/api/v1/interacoes/{id}` | Busca interacao por ID. | `ADMIN` ou `GESTOR` | `interacao` |
| `DELETE` | `/api/v1/interacoes/{id}` | Exclui interacao logicamente. | `ADMIN` ou `GESTOR` | `interacao` |

Filtros aceitos em `GET /api/v1/clientes/{clienteId}/interacoes`:

```text
q
tipo
dataInicio
dataFim
page
size
sort
```

### 7.7 Endpoints de radar, missoes e indicadores

| Metodo | Rota | Responsabilidade | Autenticacao | Modulo |
| --- | --- | --- | --- | --- |
| `GET` | `/api/v1/radar/prioridades` | Lista fila priorizada do Radar. | Bearer Supabase JWT | `missao` |
| `GET` | `/api/v1/missoes` | Lista missoes visiveis para o usuario. | Bearer Supabase JWT | `missao` |
| `POST` | `/api/v1/missoes` | Cria missao a partir de cliente, veiculo e predicao. | Bearer Supabase JWT | `missao` |
| `GET` | `/api/v1/missoes/{id}` | Busca ficha da missao. | Bearer Supabase JWT | `missao` |
| `GET` | `/api/v1/cartoes/{codigoCartao}` | Abre missao pelo cartao de recuperacao. | Bearer Supabase JWT | `missao` |
| `PATCH` | `/api/v1/missoes/{id}/status` | Atualiza estado da missao. | Bearer Supabase JWT | `missao` |
| `POST` | `/api/v1/missoes/{id}/acoes` | Registra contato/acao da missao. | Bearer Supabase JWT | `missao` |
| `POST` | `/api/v1/missoes/{id}/resultado` | Registra memoria de resultado. | Bearer Supabase JWT | `missao` |
| `GET` | `/api/v1/indicadores/consultor` | Retorna indicadores do consultor logado. | Bearer Supabase JWT | `missao` |
| `GET` | `/api/v1/indicadores/retencao` | Retorna indicadores agregados. | Bearer Supabase JWT | `missao` |

Regras funcionais de acesso:

- `ANALISTA` visualiza missoes livres ou atribuidas a ele.
- `GESTOR` e `ADMIN` visualizam fila e indicadores agregados.
- Quando um analista assume uma missao, o BFF registra `responsavel_id` com o `public.profiles.id` extraido do JWT Supabase.

### 7.8 Endpoints ML

| Metodo | Rota | Responsabilidade | Autenticacao | Modulo |
| --- | --- | --- | --- | --- |
| `POST` | `/api/ml/predict` | Chama FastAPI ML `/predict` e devolve predicao + sugestao de missao. | `SCOPE_ml:predict`, `ADMIN`, `GESTOR`, `ANALISTA` ou demo token habilitado | `ml` |
| `POST` | `/api/v1/ml/predicoes/processar-lote` | Processa fila ML em lote e salva resultados. | `SCOPE_ml:predict`, `ADMIN`, `GESTOR`, `ANALISTA` ou demo token habilitado | `ml` |

`/api/ml/**` e `/api/v1/ml/**` aceitam:

- service token Java com authority `SCOPE_ml:predict`;
- Supabase user token com role `ADMIN`, `GESTOR` ou `ANALISTA`;
- `X-ML-Demo-Token` apenas quando `DEMO_MODE=true` e o valor bate com `JAVA_ML_DEMO_TOKEN`.

Com `DEMO_MODE=false`, o header `X-ML-Demo-Token` nao libera acesso.

### 7.9 Exemplos de uso

Gerar service token tecnico:

```bash
curl -X POST http://localhost:8080/api/v1/auth/service-token \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "java-bff-demo",
    "clientSecret": "<segredo_do_cliente>"
  }'
```

Consultar usuario autenticado:

```bash
curl http://localhost:8080/api/v1/me \
  -H "Authorization: Bearer <supabase_access_token>"
```

Chamar predicao ML pelo BFF:

```bash
curl -X POST http://localhost:8080/api/ml/predict \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <supabase_access_token_ou_service_token_java>" \
  -d '{
    "features": {
      "ano_modelo": 2020,
      "qtde_revisoes_ate_corte": 2,
      "meses_desde_ultimo_servico_ate_corte": 14.2,
      "meses_relacionamento_ate_corte": 48.0,
      "n_dealers_usados_ate_corte": 1,
      "km_max_ate_corte": 48200,
      "pct_agenda_ate_corte": 0.65,
      "intervalo_medio_revisoes_dias_ate_corte": 220.0,
      "dias_ate_primeira_revisao": 180,
      "idade_veiculo_meses_ate_corte": 54.0,
      "modelo": "KA"
    },
    "modelo_veiculo": "Ka"
  }'
```

Processar lote ML:

```bash
curl -X POST "http://localhost:8080/api/v1/ml/predicoes/processar-lote?limit=100" \
  -H "Authorization: Bearer <supabase_access_token_ou_service_token_java>"
```

Criar missao:

```bash
curl -X POST http://localhost:8080/api/v1/missoes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <supabase_access_token_admin_ou_gestor>" \
  -d '{
    "clienteId": 1,
    "veiculoId": 1,
    "prazo": "Hoje",
    "valorPotencial": 980,
    "impactoVinShareEstimado": 1.4,
    "predicao": {
      "features": {
        "ano_modelo": 2020,
        "qtde_revisoes_ate_corte": 2,
        "meses_desde_ultimo_servico_ate_corte": 14.2,
        "meses_relacionamento_ate_corte": 48.0,
        "n_dealers_usados_ate_corte": 1,
        "km_max_ate_corte": 48200,
        "pct_agenda_ate_corte": 0.65,
        "intervalo_medio_revisoes_dias_ate_corte": 220.0,
        "dias_ate_primeira_revisao": 180,
        "idade_veiculo_meses_ate_corte": 54.0,
        "modelo": "KA"
      },
      "modelo_veiculo": "Ka"
    }
  }'
```

## 8. Deploy, CI/CD e Demonstracao

### 8.1 Deploy no Render

Servico recomendado:

```text
Name: ford-challenge-api
Language: Docker
Branch: main
Dockerfile Path: Dockerfile
Health Check Path: /health
```

Variaveis recomendadas para `prod`:

```env
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST_POOLER:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres.xxxxx
SPRING_DATASOURCE_PASSWORD=SENHA_SUPABASE
SUPABASE_JWKS_URL=https://PROJECT_REF.supabase.co/auth/v1/.well-known/jwks.json
SUPABASE_JWT_ISSUER=https://PROJECT_REF.supabase.co/auth/v1
SUPABASE_JWT_AUDIENCE=authenticated
JWT_SECRET=segredo_interno_para_service_tokens
ML_API_BASE_URL=https://ford-vinguard-api.onrender.com
FORD_ML_SERVICE_TOKEN=<mesmo-service-token-configurado-na-fastapi>
CORS_ALLOWED_ORIGINS=https://<origem-do-app>
DEMO_MODE=false
```

No profile `prod`:

- Hibernate roda com `ddl-auto=validate`.
- Flyway usa `classpath:db/migration-postgres`.
- `baseline-on-migrate=true` permite trabalhar com schema Supabase existente.
- Swagger/OpenAPI fica desabilitado.
- `CORS_ALLOWED_ORIGINS` nao pode conter wildcard (`*`).

### 8.2 CI/CD com GitHub Actions e GHCR

Workflow: `.github/workflows/docker-ghcr.yml`.

Pipeline executado em push para `main` ou manualmente:

```text
Checkout do codigo
-> setup Java 17
-> ./mvnw -B test
-> login no GHCR
-> docker build
-> Trivy scan para vulnerabilidades HIGH/CRITICAL
-> docker push com tags SHA e latest
```

A imagem e publicada em:

```text
ghcr.io/<owner>/<repository>:<sha>
ghcr.io/<owner>/<repository>:latest
```

### 8.3 Roteiro rapido de demonstracao alinhado a rubrica

1. Apresentar o diagrama textual da arquitetura na secao 1.1.
2. Abrir `/health` para provar disponibilidade do Web Service.
3. Abrir `/swagger-ui.html` em ambiente nao-prod para mostrar o contrato OpenAPI.
4. Mostrar tabelas de endpoints no README e destacar metodos HTTP corretos.
5. Autenticar com Supabase JWT ou gerar service token tecnico.
6. Chamar `GET /api/v1/me` para demonstrar JWT, profile e role.
7. Chamar `GET /api/v1/radar/prioridades` para demonstrar servico de negocio.
8. Abrir uma missao por `/api/v1/cartoes/{codigoCartao}` ou `/api/v1/missoes/{id}`.
9. Registrar acao em `/api/v1/missoes/{id}/acoes`.
10. Registrar resultado em `/api/v1/missoes/{id}/resultado`.
11. Consultar `/api/v1/indicadores/consultor` e `/api/v1/indicadores/retencao`.
12. Demonstrar `/api/ml/predict` para evidenciar integracao Java BFF -> FastAPI ML.
13. Explicar Flyway e migrations na secao 4.2.
14. Explicar CI/CD com testes, Docker, Trivy e GHCR na secao 8.2.

### 8.4 Checklist de avaliacao

| Criterio | Evidencia no projeto |
| --- | --- |
| Web Services - arquitetura | Diagrama textual com Mobile/Swagger, BFF, Supabase, FastAPI ML, Flyway, Render e GHCR. |
| Web Services - API REST | Controllers REST versionados, JSON, Swagger/OpenAPI e endpoints documentados. |
| Web Services - metodos HTTP | Uso explicito de `GET`, `POST`, `PUT`, `PATCH`, `DELETE` e `OPTIONS`. |
| Web Services - documentacao | README como contrato e Swagger/OpenAPI em ambiente nao-prod. |
| SOA - modularizacao | Pacotes por dominio e services reutilizaveis. |
| SOA - camadas | Separacao entre API/controllers, services, repositories, domain, infra e shared. |
| Padroes - REST/JSON/OpenAPI | RESTful API, JSON, DTOs, validacao, JWT, RBAC e CORS. |
| Padroes - erros | `GlobalExceptionHandler` e `ErrorResponse` padronizado. |
| Banco - conexao | PostgreSQL/Supabase, H2 em testes, JPA e env vars por profile. |
| Banco - migrations | Flyway com migrations PostgreSQL e H2 versionadas. |

### 8.5 Observacoes de seguranca operacional

- Nao commitar `.env`, senhas, connection strings reais, JWT secrets ou tokens.
- `private.api_clients.client_secret_hash` deve armazenar apenas BCrypt hash.
- `FORD_ML_SERVICE_TOKEN` deve ser igual ao segredo configurado na FastAPI ML.
- `X-ML-Demo-Token` deve ser usado somente em demo local/controlada.
- Em producao, manter `DEMO_MODE=false` e `CORS_ALLOWED_ORIGINS` sem wildcard.

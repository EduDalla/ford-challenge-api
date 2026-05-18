# Pulso Retencao API

Backend Java/Spring Boot usado como BFF da demo Ford Dock360 / Ford VinGuard.
Ele expoe CRUDs de apoio, valida usuarios autenticados pelo Supabase, emite
tokens tecnicos internos e chama a FastAPI ML.

## Arquitetura Da Demo

```text
Mobile React Native/Expo ou Swagger Java
-> Java/Spring Boot BFF
-> FastAPI ML no Render
-> modelos joblib externos
-> missao, risco e acao recomendada
```

Responsabilidades:

- Mobile: experiencia do consultor e fallback mock.
- Java BFF: autenticacao, contrato REST para o mobile, persistencia e chamada ao ML.
- FastAPI ML: predicao, perfil, risco, score e acao recomendada.
- Supabase Postgres: dados da aplicacao e perfis de usuarios.

## Stack

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Flyway
- PostgreSQL em producao
- H2 em testes
- Springdoc OpenAPI/Swagger
- Auth0 Java JWT

## Banco De Dados

Em producao, o banco esperado e Supabase Postgres com estes schemas/tabelas:

```text
auth.users
public.profiles
public.clientes
public.interacoes
public.informacoes
public.informacoes_user
private.api_clients
private.permissions
private.api_client_permissions
```

O Java nao cria nem autentica usuarios humanos localmente. Usuarios humanos
nascem no Supabase Auth, e o backend usa `public.profiles` como perfil funcional
da aplicacao.

As credenciais tecnicas ficam em `private.api_clients`; nao representam usuario
humano.

## Profiles

### Local

O arquivo `application.properties` aponta para PostgreSQL local por padrao:

```properties
spring.datasource.url=${DB_URL:${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5433/pulso_retencao}}
spring.datasource.username=${DB_USERNAME:${SPRING_DATASOURCE_USERNAME:postgres}}
spring.datasource.password=${DB_PASSWORD:${SPRING_DATASOURCE_PASSWORD:postgres}}
```

### Testes

Os testes usam H2 com migrations em `src/main/resources/db/migration`.

```bash
./mvnw test
```

### Producao

Use o profile `prod` no Render:

```env
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST_POOLER:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres.xxxxx
SPRING_DATASOURCE_PASSWORD=SENHA_SUPABASE
```

O profile `prod` usa:

```properties
spring.flyway.locations=classpath:db/migration-postgres
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.jpa.hibernate.ddl-auto=validate
```

## Autenticacao

### Usuario Humano

O endpoint `/login` foi removido do fluxo operacional. Para usuario humano:

1. O app autentica no Supabase.
2. O app envia `Authorization: Bearer <supabase_access_token>` ao Java.
3. O Java valida o JWT com `SUPABASE_JWT_SECRET`.
4. O Java extrai `sub` e busca `public.profiles.id`.
5. O campo `profiles.perfil` vira role Spring: `ROLE_ADMIN`, `ROLE_ANALISTA` ou `ROLE_GESTOR`.

Variaveis:

```env
SUPABASE_JWT_SECRET=jwt_secret_do_supabase
SUPABASE_JWT_ISSUER=https://PROJECT_REF.supabase.co/auth/v1
SUPABASE_JWT_AUDIENCE=authenticated
```

`SUPABASE_JWT_SECRET` e necessario para aceitar tokens de usuario Supabase. Se
ele estiver vazio, o backend ainda sobe e tokens tecnicos continuam funcionando,
mas Bearer tokens de usuario Supabase recebem `401`. `SUPABASE_JWT_ISSUER` pode
ficar vazio em ambiente local. Em producao, configure com o issuer do projeto
Supabase.

### Cliente Tecnico

O endpoint tecnico continua existindo:

```text
POST /api/v1/auth/service-token
```

Ele valida `clientId` e `clientSecret` contra `private.api_clients` e emite um
JWT interno assinado com `JWT_SECRET`.

Exemplo:

```bash
curl -X POST https://ford-challenge-api.onrender.com/api/v1/auth/service-token \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "python-ml-service",
    "clientSecret": "<segredo_do_cliente>"
  }'
```

O banco deve guardar apenas BCrypt hash em
`private.api_clients.client_secret_hash`, nunca o segredo em texto puro.

SQL base para cadastrar um client tecnico, usando um hash BCrypt gerado fora do
repositorio:

```sql
insert into private.api_clients (
    nome,
    client_id,
    client_secret_hash,
    ativo,
    criado_em,
    atualizado_em
)
values (
    'Java BFF Demo',
    'java-bff-demo',
    '<bcrypt_hash_do_segredo>',
    true,
    current_timestamp,
    current_timestamp
)
on conflict (client_id) do nothing;

insert into private.api_client_permissions (api_client_id, permission_id)
select ac.id, p.id
from private.api_clients ac
join private.permissions p on p.codigo = 'ml:predict'
where ac.client_id = 'java-bff-demo'
on conflict do nothing;
```

## Endpoints Principais

```text
GET  /                         health leve
GET  /health                   health check do Render
GET  /v3/api-docs              OpenAPI JSON
GET  /swagger-ui.html          Swagger UI
POST /api/v1/auth/service-token token tecnico
POST /api/ml/predict           BFF para FastAPI ML
```

`POST /api/ml/predict` aceita:

- service token Java com authority `SCOPE_ml:predict`; ou
- Supabase user token cujo `public.profiles.perfil` seja `ADMIN`, `GESTOR` ou `ANALISTA`.

## Integracao FastAPI ML

Configure o Java com:

```env
ML_API_BASE_URL=https://ford-vinguard-api.onrender.com
FORD_ML_SERVICE_TOKEN=TOKEN_GERADO_NA_FASTAPI
FORD_ML_TIMEOUT_MS=8000
```

`FORD_ML_BASE_URL` tambem e aceito como nome principal; `ML_API_BASE_URL` fica
como fallback.

Para demo via Swagger sem gravar o token da FastAPI no ambiente, o endpoint
`/api/ml/predict` aceita `X-ML-Demo-Token` quando `FORD_ML_SERVICE_TOKEN` nao
estiver configurado.

Exemplo:

```bash
curl -X POST https://ford-challenge-api.onrender.com/api/ml/predict \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <supabase_access_token_ou_service_token_java>" \
  -H "X-ML-Demo-Token: <token_fastapi_opcional>" \
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

## Deploy Render

Crie o backend como Web Service Docker:

```text
Name: ford-challenge-api
Language: Docker
Branch: main
Region: Oregon (US West)
Root Directory: ford-challenge-api
Dockerfile Path: Dockerfile
Health Check Path: /health
```

Variaveis recomendadas:

```env
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST_POOLER:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres.xxxxx
SPRING_DATASOURCE_PASSWORD=SENHA_SUPABASE
SUPABASE_JWT_SECRET=jwt_secret_do_supabase
SUPABASE_JWT_ISSUER=https://PROJECT_REF.supabase.co/auth/v1
SUPABASE_JWT_AUDIENCE=authenticated
JWT_SECRET=segredo_interno_para_service_tokens
ML_API_BASE_URL=https://ford-vinguard-api.onrender.com
FORD_ML_SERVICE_TOKEN=TOKEN_GERADO_NA_FASTAPI
CORS_ALLOWED_ORIGINS=*
```

Nao commite `.env`, connection string real, senha Supabase, JWT secret ou tokens.

## Roteiro Rapido De Demo

1. Abrir `https://ford-challenge-api.onrender.com/health`.
2. Abrir `https://ford-challenge-api.onrender.com/swagger-ui.html`.
3. Autenticar usuario no Supabase ou gerar service token Java.
4. Chamar `POST /api/ml/predict`.
5. Confirmar resposta com `ml` e `missao`.
6. No mobile, manter mock como fallback e apontar API real quando a demo for integrada.

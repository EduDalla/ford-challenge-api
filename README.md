# Pulso Retenção API

API REST em Java/Spring Boot para acompanhar clientes em risco de evasão e registrar interações de retenção. O projeto foi montado de forma didática para demonstrar Web Services, arquitetura em camadas, persistência com banco de dados e migrations.

## 1. Nome e descrição do projeto

**Pulso Retenção API** gerencia clientes, classifica o nível de risco de perda e registra contatos feitos pela equipe de retenção, como telefone, email, WhatsApp, reunião ou visita.

## 2. Justificativa da arquitetura

A aplicação usa uma arquitetura monolítica modular com separação em camadas. Essa escolha é simples para estudo, mas ainda demonstra os conceitos de SOA: serviços com responsabilidades específicas, baixo acoplamento entre controller e banco, e comunicação externa por API REST.

## 3. Desenho textual da arquitetura

```text
Cliente/Postman/Swagger
→ Controller REST
→ Service
→ Repository
→ Banco de Dados MySQL
```

- **Cliente/Postman/Swagger**: envia requisições HTTP e recebe JSON.
- **Controller REST**: expõe endpoints e define status HTTP.
- **Service**: concentra regras de negócio, como validar duplicidade e impedir data futura.
- **Repository**: acessa o banco via Spring Data JPA.
- **Banco de Dados MySQL**: armazena clientes e interações.

## 4. Estrutura de pastas

```text
src/main/java/br/com/fiap/ford/pulsoretencao
├── autenticacao
│   ├── api
│   ├── domain
│   ├── repository
│   └── service
├── cliente
│   ├── api
│   ├── domain
│   ├── repository
│   └── service
├── integracao
│   ├── domain
│   ├── repository
│   └── service
├── interacao
│   ├── api
│   ├── domain
│   ├── repository
│   └── service
├── ml
│   ├── api
│   └── service
├── usuario
│   ├── domain
│   └── repository
├── infra
│   ├── config
│   └── security
└── shared
    ├── api
    └── exception

src/main/resources
├── application.properties
├── application-prod.properties
└── db
    ├── migration
    └── migration-postgres
```

Os módulos seguem o padrão `api`, `domain`, `repository` e `service` quando
essas camadas existem. `usuario` guarda a identidade humana, `autenticacao`
guarda login e estado de autenticação, e `integracao` guarda clientes técnicos e
permissões de sistema-sistema. Configurações e segurança ficam em `infra`,
enquanto erros e contratos compartilhados ficam em `shared`.

## 5. Dependências do projeto

- Java 17
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Bean Validation
- MySQL Connector/J
- PostgreSQL JDBC Driver para profile `prod` com Supabase
- Flyway
- Springdoc OpenAPI/Swagger UI
- Auth0 Java JWT
- Maven Wrapper (`./mvnw`)
- H2 apenas para testes automatizados

## 6. Configuração do banco de dados

O projeto usa MySQL por padrão. As configurações estão em `src/main/resources/application.properties`.

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/pulso_retencao
spring.datasource.username=pulso
spring.datasource.password=pulso123
spring.jpa.hibernate.ddl-auto=validate
```

Também é possível sobrescrever por variáveis de ambiente:

```bash
DB_URL=jdbc:mysql://localhost:3307/pulso_retencao
DB_USERNAME=pulso
DB_PASSWORD=pulso123
```

Para subir MySQL com Docker:

```bash
docker compose up -d
```

## 7. Modelos/entities

### Cliente

Representa a entidade principal do sistema:

- `id`
- `nome`
- `email`
- `telefone`
- `documento`
- `segmento`
- `nivelRisco`: `BAIXO`, `MEDIO`, `ALTO`, `CRITICO`
- `ativo`
- `criadoEm`
- `atualizadoEm`

### Interacao

Representa uma ação de retenção vinculada a um cliente:

- `id`
- `cliente`
- `tipo`: `TELEFONE`, `EMAIL`, `WHATSAPP`, `REUNIAO`, `VISITA`
- `descricao`
- `resultado`
- `dataInteracao`

### Usuario

Representa o usuário autenticável da API:

- `id`
- `nome`
- `email`
- `login`
- `senhaHash`
- `perfil`: `ADMIN`, `ANALISTA`, `GESTOR`
- `ativo`
- `criadoEm`
- `atualizadoEm`

### UsuarioAutenticacao

Representa o estado mutável da autenticação de um usuário:

- `id`
- `usuario`
- `tentativasLogin`
- `bloqueadoAte`
- `senhaAlteradaEm`
- `ultimoLoginEm`

### ApiClient

Representa uma credencial técnica para integrações sistema-sistema:

- `id`
- `nome`
- `clientId`
- `clientSecretHash`
- `ativo`
- `criadoEm`
- `atualizadoEm`
- `ultimoUsoEm`
- permissões vinculadas por `api_client_permissions`

## 8. Repositories

- `ClienteRepository`: CRUD de clientes e consultas de duplicidade por email/documento.
- `InteracaoRepository`: CRUD de interações, listagem por cliente e contagem por cliente.
- `UsuarioRepository`: consulta usuários por login para autenticação.
- `UsuarioAutenticacaoRepository`: controla tentativas, bloqueio e último login.
- `ApiClientRepository`, `PermissionRepository` e `ApiClientPermissionRepository`: controlam clientes técnicos e permissões.

## 9. Services

- `ClienteService`: cadastro, listagem, busca, atualização, exclusão e validação de duplicidade.
- `InteracaoService`: registro, listagem, busca, exclusão e validação de data futura.
- `MlPredictionService`: integração com a FastAPI ML.
- `AutenticacaoService`: integração do usuário com Spring Security.
- `UsuarioAutenticacaoService`: registra sucesso/falha de login e bloqueio.
- `ApiClientService`: valida `clientId/clientSecret` de integrações técnicas.

As regras de negócio ficam nos services, não nos controllers.

## 10. Controllers

- `ClienteController`: endpoints de `/api/v1/clientes`.
- `InteracaoController`: endpoints de `/api/v1/clientes/{clienteId}/interacoes` e `/api/v1/interacoes/{id}`.
- `MlPredictionController`: endpoint de `/api/ml/predict`.
- `AutenticacaoController`: endpoint de `/login`.
- `ServicoAutenticacaoController`: endpoint de `/api/v1/auth/service-token`.

## 11. DTOs

- `ClienteRequest`: dados recebidos para criar/atualizar cliente.
- `ClienteResponse`: dados devolvidos ao cliente da API.
- `InteracaoRequest`: dados recebidos para registrar interação.
- `InteracaoResponse`: dados devolvidos sobre uma interação.
- `MlPredictRequest`, `MlFeaturesRequest`, `MlPredictResponse`, `MlBffPredictResponse` e `MlMissaoResponse`: contratos da integração ML.
- `DadosAutenticacao`, `DadosAutenticacaoServico` e `DadosTokenJWT`: contratos de autenticação.
- `ErrorResponse`: formato padronizado de erro.

## 12. Tratamento de exceções

O projeto usa `@RestControllerAdvice` em `GlobalExceptionHandler`.

Exemplo de erro:

```json
{
  "timestamp": "2026-05-04T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Cliente não encontrado com id 10",
  "path": "/api/v1/clientes/10"
}
```

Exceções tratadas:

- `ResourceNotFoundException`: 404 Not Found
- `BadRequestException`: 400 Bad Request
- `DatabaseException`: 400 Bad Request
- erros de validação: 400 Bad Request
- erros inesperados: 500 Internal Server Error

## 13. Migrations

As migrations ficam em `src/main/resources/db/migration`.

- `V1__create_tables.sql`: cria `clientes` e `interacoes`.
- `V2__insert_initial_data.sql`: insere dados iniciais.
- `V3__create_usuarios_table.sql`: cria `usuarios`, `usuario_autenticacao`, `api_clients`, `permissions` e `api_client_permissions`.

Para PostgreSQL, o profile de produção usa scripts equivalentes em
`src/main/resources/db/migration-postgres`. O Hibernate está em modo `validate`,
então quem cria a estrutura é o Flyway.

## 14. Documentação dos endpoints

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

### Autenticação

| Método | Endpoint | Descrição | Status |
| --- | --- | --- | --- |
| POST | `/login` | Gera JWT para usuário humano | 200 ou 401 |
| POST | `/api/v1/auth/service-token` | Gera JWT para cliente técnico | 200 ou 401 |

Tokens de usuário carregam `type=user` e `role`. Tokens de serviço carregam
`type=service` e `scopes`. O endpoint `/api/ml/predict` exige token de serviço
com o scope `ml:predict`.

Exemplo de token técnico:

```bash
curl -X POST http://localhost:8080/api/v1/auth/service-token \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "python-ml-service",
    "clientSecret": "<segredo_do_cliente>"
  }'
```

O valor salvo em `api_clients.client_secret_hash` deve ser um hash BCrypt, não o
segredo em texto puro.

### Clientes

| Método | Endpoint | Descrição | Status |
| --- | --- | --- | --- |
| POST | `/api/v1/clientes` | Cadastra cliente | 201 |
| GET | `/api/v1/clientes` | Lista clientes | 200 |
| GET | `/api/v1/clientes/{id}` | Busca cliente por ID | 200 ou 404 |
| PUT | `/api/v1/clientes/{id}` | Atualiza cliente | 200 ou 404 |
| DELETE | `/api/v1/clientes/{id}` | Exclui cliente | 204 ou 404 |

### Interações

| Método | Endpoint | Descrição | Status |
| --- | --- | --- | --- |
| POST | `/api/v1/clientes/{clienteId}/interacoes` | Registra interação | 201 |
| GET | `/api/v1/clientes/{clienteId}/interacoes` | Lista interações do cliente | 200 |
| GET | `/api/v1/interacoes/{id}` | Busca interação por ID | 200 ou 404 |
| DELETE | `/api/v1/interacoes/{id}` | Exclui interação | 204 ou 404 |

## 15. Exemplos JSON

### Criar cliente

Requisição:

```json
{
  "nome": "Mariana Costa",
  "email": "mariana.costa@example.com",
  "telefone": "11988887777",
  "documento": "44455566677",
  "segmento": "Educação",
  "nivelRisco": "ALTO",
  "ativo": true
}
```

Resposta `201 Created`:

```json
{
  "id": 4,
  "nome": "Mariana Costa",
  "email": "mariana.costa@example.com",
  "telefone": "11988887777",
  "documento": "44455566677",
  "segmento": "Educação",
  "nivelRisco": "ALTO",
  "ativo": true,
  "criadoEm": "2026-05-04T10:00:00",
  "atualizadoEm": "2026-05-04T10:00:00",
  "totalInteracoes": 0
}
```

### Registrar interação

Requisição:

```json
{
  "tipo": "TELEFONE",
  "descricao": "Contato para entender motivo de insatisfação.",
  "resultado": "Cliente aceitou reunião de acompanhamento.",
  "dataInteracao": "2026-05-04T09:30:00"
}
```

Resposta `201 Created`:

```json
{
  "id": 4,
  "clienteId": 1,
  "clienteNome": "Ana Martins",
  "tipo": "TELEFONE",
  "descricao": "Contato para entender motivo de insatisfação.",
  "resultado": "Cliente aceitou reunião de acompanhamento.",
  "dataInteracao": "2026-05-04T09:30:00"
}
```

## 16. Como executar o projeto

Pré-requisitos:

- JDK 17 ou superior
- Docker, se quiser usar o MySQL do `docker-compose.yml`

Se o JDK não estiver no ambiente, configure `JAVA_HOME` antes de usar o Maven
Wrapper:

```bash
export JAVA_HOME=/caminho/para/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"
```

Passos:

```bash
docker compose up -d
./mvnw spring-boot:run
```

Depois acesse:

```text
http://localhost:8080/swagger-ui.html
```

## 17. Como testar a API

Testes automatizados:

```bash
./mvnw -DskipTests compile
./mvnw test
```

Testes manuais:

```bash
curl -X GET http://localhost:8080/api/v1/clientes
```

```bash
curl -X POST http://localhost:8080/api/v1/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Mariana Costa",
    "email": "mariana.costa@example.com",
    "telefone": "11988887777",
    "documento": "44455566677",
    "segmento": "Educação",
    "nivelRisco": "ALTO",
    "ativo": true
  }'
```

## 18. Como os requisitos foram atendidos

- **Integração por Web Services**: API RESTful com Spring Web MVC.
- **Desenho de arquitetura**: documentado no README com fluxo cliente → controller → service → repository → banco.
- **APIs RESTful**: CRUD de clientes e endpoints de interações.
- **Métodos HTTP**: uso de GET, POST, PUT e DELETE com status 200, 201, 204, 400, 404 e 500.
- **Documentação da API**: README e Swagger/OpenAPI.
- **SOA**: services independentes para clientes e interações.
- **Separação de responsabilidades**: módulos por domínio com subpacotes `api`, `domain`, `repository` e `service` quando aplicável.
- **Boas práticas REST**: JSON, endpoints no plural e versionamento `/api/v1`.
- **Tratamento de erros**: exceções customizadas e handler global.
- **Banco de dados**: MySQL com Spring Data JPA e Hibernate.
- **Migrations**: Flyway cria tabelas, usuários e dados iniciais.

## 19. Integracao ML BFF para a demo

Fluxo integrado:

```text
Mobile React Native/Expo ou Swagger Java
-> Java/Spring Boot BFF
-> FastAPI ML no Render
-> modelos joblib externos validados por SHA256
-> resposta ML convertida em missao/risco/acao recomendada
```

Endpoint:

```text
POST /api/ml/predict
```

Configuracao:

```properties
FORD_ML_BASE_URL=https://ford-vinguard-api.onrender.com
FORD_ML_SERVICE_TOKEN=<token JWT analyst gerado pela FastAPI>
FORD_ML_TIMEOUT_MS=8000
JWT_SECRET=<segredo usado para assinar tokens do Java>
```

Para testar via Swagger Java sem gravar o token da FastAPI em `.env`, informe o
JWT da FastAPI no header `X-ML-Demo-Token`. Esse header so e usado quando
`FORD_ML_SERVICE_TOKEN` nao estiver configurado.

Além disso, para chamar `/api/ml/predict`, informe no header `Authorization` um
Bearer token emitido por `/api/v1/auth/service-token` para um `api_client` com a
permissao `ml:predict`.

Antes, gere um token na FastAPI:

```bash
curl -X POST "https://ford-vinguard-api.onrender.com/auth/demo-token?role=analyst" \
  -H "X-Demo-Token-Secret: <DEMO_TOKEN_SECRET>"
```

Depois chame o BFF Java:

```bash
curl -X POST http://localhost:8080/api/ml/predict \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <service_token_java>" \
  -H "X-ML-Demo-Token: <access_token>" \
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

Resposta esperada em forma:

```json
{
  "ml": {
    "prediction": "churn",
    "churn_probability": 0.73,
    "risk_level": "high",
    "perfil_previsto": "inativo",
    "acao_recomendada": "Ativar recuperacao imediata..."
  },
  "missao": {
    "codigoCartao": "CARD-ML-DEMO",
    "risco": "alto",
    "score": 73,
    "prioridadeRadar": "P1",
    "perfil": "Cliente inativo",
    "acaoRecomendada": "Ativar recuperacao imediata..."
  }
}
```

## 20. Supabase Postgres em producao

O profile local continua usando a configuracao padrao do projeto. Os testes
continuam usando H2 por `src/test/resources/application-test.properties`.

PostgreSQL deve ser usado somente com o profile `prod`:

```bash
SPRING_PROFILES_ACTIVE=prod
```

No Render, configure as variaveis de ambiente abaixo com os dados do projeto
Supabase. Nao grave esses valores no repositorio:

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<database>?sslmode=require
SPRING_DATASOURCE_USERNAME=<usuario>
SPRING_DATASOURCE_PASSWORD=<senha>
FORD_ML_BASE_URL=https://ford-vinguard-api.onrender.com
FORD_ML_SERVICE_TOKEN=<token JWT analyst gerado pela FastAPI>
CORS_ALLOWED_ORIGINS=*
```

Notas para Supabase:

- Use a connection string JDBC do Postgres/Supabase com `sslmode=require`.
- O schema esperado e `public`.
- As migrations PostgreSQL ficam em `src/main/resources/db/migration-postgres`.
- O profile `prod` usa `spring.flyway.locations=classpath:db/migration-postgres`.
- O Hibernate fica em `validate`; quem cria as tabelas em producao e o Flyway.
- O driver PostgreSQL e o modulo Flyway PostgreSQL ficam no `pom.xml`.

Checklist de deploy no Render:

1. Criar o banco no Supabase e copiar host, database, usuario e senha.
2. Configurar `SPRING_PROFILES_ACTIVE=prod`.
3. Configurar `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`.
4. Manter `FORD_ML_BASE_URL` e `FORD_ML_SERVICE_TOKEN` se o BFF Java for chamar a FastAPI ML.
5. Configurar `CORS_ALLOWED_ORIGINS` com `*` para demo ou com a origem real do mobile/web.
6. Subir o backend; o Flyway aplicara `V1__create_tables.sql` e `V2__insert_initial_data.sql` no Postgres.

## 21. Deploy Docker no Render

Este backend Java deve ser criado no Render como Web Service com Docker, nao
como Node. Como o projeto esta em monorepo, configure o Root Directory para que
o Render rode o build dentro da pasta do backend.

Campos principais:

```text
Name: ford-challenge-api
Language: Docker
Branch: main
Region: Oregon (US West)
Root Directory: ford-challenge-api
Dockerfile Path: Dockerfile
```

Com Docker, nao preencha `Build Command` nem `Start Command` se o Render usar o
`CMD` do `Dockerfile`. O container executa:

```text
java -jar /app/app.jar
```

O `server.port` respeita a variavel `PORT` enviada pela plataforma:

```properties
server.port=${PORT:8080}
```

Variaveis de ambiente recomendadas para a demo integrada:

```env
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST_POOLER:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres.xxxxx
SPRING_DATASOURCE_PASSWORD=SENHA_SUPABASE
FORD_ML_BASE_URL=https://ford-vinguard-api.onrender.com
FORD_ML_SERVICE_TOKEN=TOKEN_GERADO_NA_FASTAPI
CORS_ALLOWED_ORIGINS=*
```

`FORD_ML_BASE_URL` e o nome principal usado pelo codigo. `ML_API_BASE_URL`
tambem e aceito como fallback para evitar falha caso a variavel antiga tenha
sido configurada no painel.

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
├── config
├── controller
├── dto
├── exception
├── model
├── repository
└── service

src/main/resources
├── application.properties
└── db/migration
    ├── V1__create_tables.sql
    └── V2__insert_initial_data.sql
```

## 5. Dependências do projeto

- Java 17
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- MySQL Connector/J
- PostgreSQL JDBC Driver para profile `prod` com Supabase
- Flyway
- Springdoc OpenAPI/Swagger UI
- Maven
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

## 8. Repositories

- `ClienteRepository`: CRUD de clientes e consultas de duplicidade por email/documento.
- `InteracaoRepository`: CRUD de interações, listagem por cliente e contagem por cliente.

## 9. Services

- `ClienteService`: cadastro, listagem, busca, atualização, exclusão e validação de duplicidade.
- `InteracaoService`: registro, listagem, busca, exclusão e validação de data futura.

As regras de negócio ficam nos services, não nos controllers.

## 10. Controllers

- `ClienteController`: endpoints de `/api/v1/clientes`.
- `InteracaoController`: endpoints de `/api/v1/clientes/{clienteId}/interacoes` e `/api/v1/interacoes/{id}`.

## 11. DTOs

- `ClienteRequest`: dados recebidos para criar/atualizar cliente.
- `ClienteResponse`: dados devolvidos ao cliente da API.
- `InteracaoRequest`: dados recebidos para registrar interação.
- `InteracaoResponse`: dados devolvidos sobre uma interação.
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

O Hibernate está em modo `validate`, então quem cria a estrutura é o Flyway.

## 14. Documentação dos endpoints

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

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
- **Separação de responsabilidades**: controller, service, repository, model, dto e exception.
- **Boas práticas REST**: JSON, endpoints no plural e versionamento `/api/v1`.
- **Tratamento de erros**: exceções customizadas e handler global.
- **Banco de dados**: MySQL com Spring Data JPA e Hibernate.
- **Migrations**: Flyway cria tabelas e insere dados iniciais.

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
```

Para testar via Swagger Java sem gravar token em `.env`, informe o JWT no
header `X-ML-Demo-Token`. Esse header so e usado quando
`FORD_ML_SERVICE_TOKEN` nao estiver configurado.

Antes, gere um token na FastAPI:

```bash
curl -X POST "https://ford-vinguard-api.onrender.com/auth/demo-token?role=analyst" \
  -H "X-Demo-Token-Secret: <DEMO_TOKEN_SECRET>"
```

Depois chame o BFF Java:

```bash
curl -X POST http://localhost:8080/api/ml/predict \
  -H "Content-Type: application/json" \
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
5. Subir o backend; o Flyway aplicara `V1__create_tables.sql` e `V2__insert_initial_data.sql` no Postgres.

# Pulso Retenção API

Backend Java/Spring Boot usado como BFF da solução Pulso Retenção. A aplicação integra clientes web/mobile, autenticação, PostgreSQL e o serviço externo de Machine Learning.

## Equipe

| Nome | RM |
| --- | --- |
| Abner Barbosa | 558468 |
| Eduardo Dallabella | 556803 |
| Fernando Luiz | 555201 |
| Heloísa Real | 554535 |
| Thomas de Almeida | 554812 |

## Executar localmente

Requisitos: Docker com Docker Compose e Git.

Suba o ambiente local independente do Supabase remoto:

```bash
docker compose -f docker-compose.local.yml up -d --build
```

Serviços publicados:

| Serviço | Endereço |
| --- | --- |
| API | `http://localhost:8083` |
| Health | `http://localhost:8083/health` |
| Swagger | `http://localhost:8083/swagger-ui/index.html` |
| PostgreSQL | `localhost:5434` |

Verifique os containers:

```bash
docker compose -f docker-compose.local.yml ps
curl http://localhost:8083/health
```

O ambiente local usa o banco `pulso_local`, usuário `pulso_local` e senha `pulso-local-only`. Esses dados são exclusivos para desenvolvimento.

Para encerrar os containers:

```bash
docker compose -f docker-compose.local.yml down
```

O volume do PostgreSQL é preservado. Para executar a aplicação diretamente com Maven, configure Java 17 e use:

```bash
./mvnw spring-boot:run
```

## Conexão com a solução

Esta API é o backend do Ford Service Pulse: o app e outros clientes consultam os clientes em risco, acompanham o radar e as missões e registram ações e resultados de retenção. O backend lê e grava esses dados no PostgreSQL e, quando uma predição é solicitada, consulta o serviço FastAPI ML. O painel de campanha e a estação física fazem parte da solução proposta no projeto; este repositório implementa a API que pode atendê-los, mas não contém esses componentes.

## Arquitetura contemplada

O diagrama editável está em [Diagrams/arquitetura-sprint-3.md](Diagrams/arquitetura-sprint-3.md).

- Mobile/Swagger consome a API REST Java/Spring Boot.
- O BFF concentra controllers, regras de negócio, autenticação e integração.
- PostgreSQL armazena os dados da aplicação.
- Supabase Auth fornece JWTs de usuários humanos quando configurado.
- FastAPI ML é consumida pelo BFF através de HTTP e token técnico.
- Flyway aplica as migrations do banco.

## Autenticação e autorização

Fora do profile `prod`, health e documentação são públicos. Os demais recursos exigem autenticação.

| Recurso | Acesso contemplado |
| --- | --- |
| `/health` e `/actuator/health` | Público |
| Swagger/OpenAPI fora de `prod` | Público |
| `/api/v1/auth/service-token` | Público para credenciais técnicas válidas |
| Clientes e interações | `ADMIN` e `GESTOR`; `ANALISTA` recebe 403 |
| Machine Learning | Usuários autorizados ou cliente técnico com scope `ml:predict` |

Usuários humanos usam JWT Supabase. O `sub` do token é associado a um profile ativo e o backend transforma o perfil em role (`ADMIN`, `GESTOR` ou `ANALISTA`).

Clientes técnicos usam credenciais armazenadas em `private.api_clients` e recebem tokens com:

- `type=service`;
- issuer `API Ford`;
- audience `pulso-retencao-api`;
- scopes de permissão, como `ml:predict`;
- expiração de duas horas.

`DEMO_MODE=true` é rejeitado no profile `prod`. Token inválido, profile inativo e assinatura adulterada resultam em 401.

## Funcionalidades principais

- Health check da API e do Actuator.
- Emissão de token técnico.
- CRUD de clientes com filtros e soft delete.
- Registro e consulta de interações por cliente.
- Cadastro e consulta de informações para consultores.
- Radar de prioridades e gerenciamento de missões.
- Registro de ações e resultados de missões.
- Indicadores de retenção e de consultor.
- Predição individual em `/api/ml/predict`.
- Processamento de lote de predições em `/api/v1/ml/predicoes/processar-lote`.
- Persistência de fila, snapshots e resultados do pipeline ML.
- Documentação OpenAPI/Swagger fora de produção.

## Contrato REST e erros

Os endpoints usam JSON e recursos versionados em `/api/v1`. Os métodos utilizados incluem `GET`, `POST`, `PUT`, `PATCH`, `DELETE` e `OPTIONS`.

As respostas de erro usam o formato `ErrorResponse`, com timestamp, status, erro, mensagem e caminho:

- `400`: JSON, enum ou parâmetro inválido;
- `401`: Bearer ausente ou inválido, com `WWW-Authenticate: Bearer`;
- `403`: role ou scope insuficiente;
- `404`: recurso inexistente;
- `405`: método não suportado;
- `415`: mídia não suportada;
- `429`: limite de requisições, com `Retry-After`;
- `500`: falha inesperada sem detalhes internos.

Falhas na integração ML são separadas entre entrada inválida, timeout, indisponibilidade e resposta inválida do serviço externo, sem expor o corpo bruto da FastAPI.

## Testes e CI

Execute os testes rápidos com H2:

```bash
./mvnw test
```

A suíte atual executa 25 testes, incluindo contexto da aplicação, endpoints públicos, JWT, autorização, CRUD de clientes e soft delete, fluxos de informações/interações/missões, contrato OpenAPI e o pipeline ML. Os relatórios ficam em:

- `target/surefire-reports`;
- `target/site/jacoco`.

O CI em `.github/workflows/ci.yml` executa os testes em pull requests e publica os relatórios. A publicação da imagem no GHCR permanece separada no workflow de Docker.

O teste PostgreSQL com Testcontainers pode ser executado com:

```bash
./mvnw -Ppostgres-integration verify
```

## Documentação

No ambiente local, acesse:

- Swagger UI: `http://localhost:8083/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8083/v3/api-docs`

Essas rotas não exigem token fora do profile `prod`. Em produção, Swagger e OpenAPI ficam desabilitados.

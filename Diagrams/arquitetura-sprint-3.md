# Arquitetura Sprint 3

```mermaid
flowchart LR
    U[Consultor / Mobile / Swagger] -->|login HTTPS| A[Supabase Auth]
    A -->|JWT assinado| BFF[Java Spring Boot BFF]
    BFF -->|subject| P[(profiles / role)]
    BFF -->|JDBC + Flyway| DB[(PostgreSQL)]
    BFF -->|POST /predict\nX-ML-Service-Token| ML[FastAPI ML]
    BFF -->|POST /api/v1/auth/service-token| C[Cliente técnico]
    C -->|JWT service + scope| BFF

    subgraph BFF[Java Spring Boot BFF — um deploy]
      SEC[SecurityFilter / RBAC]
      API[Controllers REST]
      DOM[Services de domínio]
      PIPE[ML pipeline / schedulers]
      SEC --> API --> DOM
      DOM --> PIPE
    end

    subgraph EXT[Serviços implantados separadamente]
      A
      DB
      ML
    end
```

Fluxo humano: login no Supabase Auth → JWT → validação de assinatura, issuer/audience e subject pelo BFF → busca de `profiles` → role `ADMIN`, `GESTOR` ou `ANALISTA`.

Fluxo técnico: cliente envia credenciais para `/api/v1/auth/service-token` → BFF valida `private.api_clients` → emite JWT HS256 separado, com issuer `API Ford`, audience `pulso-retencao-api`, tipo `service` e scopes → BFF autoriza o escopo necessário.

O BFF chama a FastAPI somente por `/predict` e `/predict-batch`; a rota pública do BFF para predição é `/api/ml/predict`.

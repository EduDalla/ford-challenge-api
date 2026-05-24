# Política de Segurança — Pulso de Retenção API

## Controles de Segurança Implementados

### 1. Criptografia em Repouso

Os dados persistidos estão protegidos por criptografia em repouso em duas camadas:

- **Banco de Dados (Supabase / PostgreSQL):** criptografia AES-256 gerenciada pelo provedor de infraestrutura. Chaves rotacionadas automaticamente. Backup também cifrado.
- **Segredos e credenciais de API Client:** armazenados exclusivamente como hash BCrypt (`BCryptPasswordEncoder`, custo padrão 10). O valor original nunca é persistido.
- **Tokens JWT:** não são armazenados no servidor; o estado de sessão é stateless. O `JWT_SECRET` e o `SUPABASE_JWT_SECRET` são injetados via variáveis de ambiente e nunca commitados no repositório (protegidos por `.gitignore`).

---

### 2. Validação de Entradas

Toda entrada de dados nas fronteiras do sistema é validada via **Jakarta Bean Validation** (integrada ao Spring Boot) antes de chegar à camada de serviço.

**Mecanismo:**
- Controllers anotados com `@Valid` em todos os `@RequestBody`.
- Records de request anotados com `@NotNull`, `@NotBlank`, `@Email`, `@Size`, `@DecimalMin`, `@PositiveOrZero`, `@PastOrPresent`, `@DecimalMax` conforme o tipo de cada campo.
- Violações resultam em HTTP 400 com resposta estruturada (`ErrorResponse`) listando os campos inválidos — sem exposição de stack trace.

**Exemplos de regras aplicadas:**

| Campo | Validação |
|---|---|
| `email` | `@NotBlank`, `@Email`, `@Size(max=150)` |
| `documento` | `@NotBlank`, `@Size(max=30)` |
| `nivelRisco` | `@NotNull` (enum) |
| `ativo` | `@NotNull` |
| `kmAtual` | `@NotNull`, `@PositiveOrZero` |
| `ultimaRevisao` | `@NotNull`, `@PastOrPresent` |
| `pctAgendaAteCorte` | `@PositiveOrZero`, `@DecimalMax("1.0")` |
| `clientId` / `clientSecret` | `@NotBlank` |

**Proteção contra injeção:**
- Todas as queries ao banco de dados são executadas via JPA/Hibernate com parâmetros vinculados — sem concatenação de SQL.
- Sem uso de `nativeQuery` com interpolação de strings.

---

### 3. Logs Estruturados sem Dados Sensíveis

**Formato:**
- Produção: formato **ECS (Elastic Common Schema)** via `logging.structured.format.console=ecs`. Saída em JSON estruturado, compatível com Elastic Stack e qualquer agregador de logs.
- Desenvolvimento: formato texto legível (sem ECS).

**Dados que nunca aparecem nos logs:**
- Senhas, `clientSecret`, `JWT_SECRET`, `SUPABASE_JWT_SECRET`
- Tokens de acesso completos (apenas prefixo nos logs de debug, se necessário)
- CPF/documento completo
- Dados bancários

**Garantias implementadas:**
- `show-sql=false` em produção (queries JPA não são logadas).
- `format_sql=false` em produção.
- O `GlobalExceptionHandler` captura todas as exceções e retorna `ErrorResponse` sem stack trace na resposta HTTP.
- Campos sensíveis não são incluídos em `toString()` de entidades ou DTOs.

---

### 4. Política de Retenção de Dados

**Exclusão lógica (soft delete):**
- Nenhum registro de cliente, missão ou interação é removido fisicamente do banco. Registros são marcados como inativo (`ativo = false`) e excluídos das consultas padrão.
- Isso preserva rastreabilidade e auditoria sem expor dados ativos.

**Retenção de dados de predição ML:**
- Snapshots de features (`vin_share_feature_snapshots`) são sobrescritos a cada ciclo de processamento — somente o estado mais recente é mantido como ativo.
- Resultados de predição (`predicao_resultado`) são mantidos com timestamp para histórico de churn score.
- Job execution logs são mantidos para rastreabilidade de pipelines batch.

**Dados de integração (API Clients):**
- API Clients revogados podem ser desativados sem exclusão, preservando histórico de acesso.

**Conformidade:**
- A política de exclusão definitiva de dados de um titular (LGPD, Art. 18) deve ser executada manualmente por administrador via exclusão física direta no banco ou procedure dedicada — não disponível como endpoint público.

---

### 5. Autenticação e Autorização

- **JWT (HS256):** tokens assinados com `JWT_SECRET`. Validação stateless a cada request.
- **Supabase JWT (RS256):** validação via JWKS endpoint público, suportando rotação de chaves sem redeploy.
- **RBAC:** perfis `ROLE_ADMIN`, `ROLE_GESTOR`, `ROLE_ANALISTA`, scope `SCOPE_ml:predict`.
- **Service Tokens:** API Clients com permissões granulares (tabela `permissions`), segredo armazenado como BCrypt hash.

---

### 6. Rate Limiting

| Escopo | Limite |
|---|---|
| Endpoints gerais (por IP) | 60 req / 60 segundos |
| Endpoints de autenticação (`/api/v1/auth/`) | 10 req / 60 segundos |
| Falhas de autenticação (`AuthenticationRateLimiter`) | 5 falhas / 10 minutos por client+IP |

Respostas bloqueadas: HTTP `429 Too Many Requests` com `ErrorResponse` estruturado.

---

### 7. CORS

- Origens permitidas configuradas via `CORS_ALLOWED_ORIGINS` (lista separada por vírgula).
- Wildcard (`*`) é rejeitado com `IllegalStateException` quando o perfil `prod` está ativo.
- Preflight OPTIONS é respondido sem autenticação.

---

### 8. Segurança de Container e CI/CD

- **Imagem Docker:** build multi-stage (Maven + JRE mínimo), usuário não-root `app:app`.
- **Trivy:** varredura de vulnerabilidades HIGH/CRITICAL no pipeline GitHub Actions com `exit-code=1` (bloqueia o push se encontrada).
- **Secrets:** nenhum segredo commitado. `.env` listado no `.gitignore`. Secrets de produção injetados via variáveis de ambiente do Render.

---

## Divulgação de Vulnerabilidades

Para reportar uma vulnerabilidade de segurança neste projeto, entre em contato com os mantenedores via **issue privada** no repositório ou pelo e-mail institucional FIAP do time.

Não abra issues públicas para vulnerabilidades que ainda não foram corrigidas.

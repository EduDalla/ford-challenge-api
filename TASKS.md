# Pendências da entrega — Sprint 3

Checklist único baseado na auditoria de 26/09/2026 e nos critérios da entrega.
Marcar uma tarefa somente depois de cumprir o aceite e registrar a evidência.
P0 = bloqueia execução/demonstração; P1 = completar a rubrica; P2 = melhoria adicional.
Os pesos pertencem aos critérios, não às tarefas; esta lista não estima nota.

## P0 — Ambiente reproduzível

- [x] **ENV-01 — Docker local independente do Supabase remoto.** Criar Compose separado, banco/volume isolados, bootstrap de `auth.users`, roles e `pgcrypto`, fixtures locais e instruções. **Aceite:** banco vazio recebe as migrations PostgreSQL; `/health`, `/api/v1/me` e consulta de clientes funcionam; ANALISTA recebe 403 em clientes; reinício preserva dados e não reaplica seeds. **Concluída em 26/09/2026:** `docker-compose.local.yml` e `docker/local/`; 9 migrations aplicadas (V1–V8 e V8.1 local), três perfis consultados com 200, clientes com 200 para ADMIN/GESTOR e 403 para ANALISTA, emissão de service token e OpenAPI com 200. Reinício validou schema atualizado sem reaplicar migrations e preservou três profiles e cinco clientes dos seeds. Build Docker executou os 6 testes existentes sem falhas. Não equivale a implementar o serviço Supabase Auth nem o modelo ML.
- [x] **ENV-02 — Corrigir permissões dos artefatos locais.** Há arquivos de `target/` pertencentes a root. Recriar apenas os artefatos afetados com o usuário de desenvolvimento e evitar builds que gerem arquivos root no workspace. **Aceite:** `./mvnw test` funciona diretamente no checkout sem sudo. **Concluída em 26/09/2026:** ownership de `target/` e cache Maven foi normalizado, os artefatos antigos foram removidos e `./mvnw test` executou diretamente no checkout sem sudo, com 11 testes aprovados e relatório JaCoCo gerado.

## P1 — Arquitetura da solução (20%)

- [x] **ARQ-01 — Sincronizar os diagramas com o código.** Corrigir a rota de predição para `/api/ml/predict`, representar Java → FastAPI e distinguir módulos internos de serviços implantados separadamente. Incluir login humano → Supabase Auth → JWT → BFF → profile/role e emissão do token técnico. **Concluída em 26/09/2026:** README e `Diagrams/arquitetura-sprint-3.md` apresentam os componentes, responsabilidades, protocolos, rota `/api/ml/predict`, integração FastAPI, fluxo humano e fluxo de token técnico.

## P1 — Autenticação e autorização (20%)

- [ ] **AUTH-01 — Consolidar a matriz de acesso.** Documentar recursos públicos, roles e scopes realmente aceitos; distinguir usuário humano de cliente técnico. **Aceite:** ADMIN/GESTOR/ANALISTA, cliente técnico sem scope, profile inativo e analista acessando missão de outro responsável possuem testes de permissão/negação; as regras do README coincidem com o código.
- [x] **AUTH-02 — Impedir acesso de demonstração em produção.** Rejeitar `DEMO_MODE=true` no profile prod e garantir que o header de demo nunca autorize nesse ambiente. **Aceite:** teste de configuração prod e teste de acesso com header de demo; configuração local usa JWT real assinado para fixtures. **Concluída em 26/09/2026:** `SecurityConfigurations` falha na inicialização quando o profile `prod` recebe `DEMO_MODE=true`; o header continua condicionado a `demoMode`, e o teste local existente valida token aceito/rejeitado.

## P1 — JWT (15%)

- [x] **JWT-01 — Corrigir o relógio de expiração.** Substituir `LocalDateTime` + offset fixo por `Instant`/`Clock` e duração explícita de duas horas para service tokens. **Aceite:** validade idêntica em UTC e America/Sao_Paulo; token expirado rejeitado; testes com relógio controlado. **Concluída em 26/09/2026:** `TokenService` usa `Clock.systemUTC()` injetado e `Duration.ofHours(2)`; teste unitário fixa o relógio em UTC e confirma o instante exato de expiração.
- [x] **JWT-02 — Reforçar claims e validação.** Exigir subject, expiração e tipo apropriado; definir audiência de service tokens e exigir issuer/audience configurados para Supabase em produção. Manter emissores/chaves separados e algoritmos permitidos explícitos. **Concluída em 26/09/2026:** service tokens agora exigem issuer `API Ford`, audience `pulso-retencao-api`, subject, expiração e `type=service`; produção exige issuer/audience Supabase configurados; testes cobrem audience, claims ausentes, tipo indevido e assinatura adulterada. Tokens técnicos emitidos antes desta alteração precisam ser renovados.

## P1 — Maturidade REST nível 2 (20%)

- [ ] **REST-01 — Corrigir status de falhas ML.** Separar erro de entrada do consumidor, timeout (504), resposta inválida/falha do upstream (502) e indisponibilidade do serviço (503). Não expor corpo bruto ou detalhes internos da FastAPI. **Aceite:** chamadas HTTP simuladas exercitam cada classe de falha e validam status e `ErrorResponse`; configuração de token ML ausente não é atribuída ao consumidor como 400.
- [ ] **REST-02 — Auditar erros HTTP gerais.** Garantir 400 para parâmetros/JSON inválidos, 401 para ausência/falha de autenticação, 403 para falta de permissão, 404 para recurso inexistente, 405 para método não suportado e 415 para mídia inválida. Evitar que o handler genérico converta erros de cliente em 500. **Aceite:** testes com status exatos, incluindo método, ID e enum inválidos; CRUD mantém 201/200/204. Documentar qualquer alteração de contrato.

## P1 — Testes automatizados (15%)

- [ ] **TEST-01 — Cobrir os fluxos principais.** Adicionar testes de clientes, interações, informações e missões: criação/consulta/alteração/exclusão quando disponíveis, validações e recursos inexistentes. Cobrir JWT e matriz de acesso das tarefas anteriores. **Aceite:** cenários de sucesso, erro e acesso não autorizado; substituir `is4xxClientError()` por status específicos; confirmar persistência e soft delete.
- [ ] **TEST-02 — Testar PostgreSQL e migrations reais.** Introduzir Testcontainers com PostgreSQL, bootstrap local explícito e migrations `migration-postgres`. **Aceite:** banco vazio inicializa, entidades validam, restrições e fila ML funcionam com registros reais; nenhuma dependência de Supabase/ML remoto na suíte. H2 pode permanecer para testes rápidos.
- [ ] **TEST-03 — Testar integração ML e pipeline.** Simular FastAPI e validar processamento de lote não vazio, resultados persistidos, referências ausentes/duplicadas, falhas e retries. **Aceite:** não considerar apenas fila vazia como sucesso do pipeline; testes determinísticos, sem chamadas externas.
- [x] **TEST-04 — Publicar evidências no CI.** Executar testes em pull requests e publicar relatórios Surefire mesmo em falha; adicionar relatório de cobertura para orientar lacunas. Separar validação de PR de publicação no GHCR. **Concluída em 26/09/2026:** `.github/workflows/ci.yml` executa `./mvnw -B test` em pull requests e publica Surefire/JaCoCo mesmo em falha; publicação GHCR permanece no workflow separado de main/manual; README aponta os artefatos.

## P1 — Documentação e tratamento de erros (10%)

- [x] **DOC-01 — Reorganizar README pela rubrica correta.** Usar arquitetura 20%, autenticação/autorização 20%, JWT 15%, REST 20%, testes 15%, documentação/erros 10%. Vincular código, diagramas e evidências; manter instruções separadas para local e Supabase. **Concluída em 26/09/2026:** README ganhou a tabela de rubrica da Sprint 3, links para o diagrama, matriz de acesso, contrato de erros, instruções local/Supabase e localização dos artefatos do CI.
- [ ] **ERR-01 — Unificar respostas de erro.** Reutilizar `ErrorResponse` em filtros, `AuthenticationEntryPoint`, `AccessDeniedHandler`, autenticação técnica e limites de requisições; restringir captura de exceções no filtro JWT para não mascarar falhas de banco como 401. **Aceite:** 400/401/403/404/429/500 possuem contrato consistente, `WWW-Authenticate` em falha Bearer e mensagens sem dados internos; testes verificam conteúdo e status.
- [ ] **DOC-02 — Completar contrato OpenAPI.** Documentar respostas de erro, permissões, recursos públicos sem Bearer e modos de autenticação ML. **Aceite:** `/v3/api-docs` representa o comportamento real, incluindo respostas 401/403/429 e schemas de erro; Swagger permite demonstrar os fluxos locais.

## P2 — Boas práticas adicionais

- [ ] **BP-01 — Rate limiting confiável.** Não confiar em `X-Forwarded-For` de clientes diretos; configurar proxies confiáveis, expirar/limitar chaves em memória e retornar `Retry-After`. **Aceite:** cabeçalho forjado não evita limite; entradas antigas são removidas; documentar limite por instância ou adotar armazenamento compartilhado se houver múltiplas réplicas.
- [ ] **BP-02 — Concorrência e integridade das missões.** Adicionar controle otimista/atômico ao assumir missão e validar que veículo informado pertence ao cliente. **Aceite:** duas requisições concorrentes não assumem a mesma missão com sucesso; vínculo cliente/veículo incompatível é rejeitado; conflito tem resposta documentada.
- [ ] **BP-03 — Paginação e transações.** Paginar radar/missões sem carregar todo o histórico, calcular indicadores no banco e reduzir duração da transação que chama ML. **Aceite:** limites de consulta definidos, contrato do consumidor preservado ou migrado explicitamente e falha externa não deixa registros parciais.
- [ ] **BP-04 — Diagnóstico de erros.** Registrar exceções inesperadas com identificador de correlação, sem JWT, senhas ou dados pessoais. **Aceite:** uma falha pode ser rastreada no log pelo identificador retornado; resposta pública permanece genérica.

## Sequência sugerida e referências

ENV-01/02 → JWT-01 e ERR-01/REST-01/02 → AUTH/JWT restantes → TEST-01/02/03/04 → ARQ/DOC → melhorias P2. Implementar testes junto das correções, não apenas ao final.

- [Spring Security: tratamento Bearer e ponto de entrada de autenticação](https://docs.spring.io/spring-security/reference/7.0/servlet/oauth2/resource-server/index.html)
- [OWASP: segurança REST, JWT, erros e status HTTP](https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html)
- [Spring Boot: Testcontainers](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html)
- [Docker: inicialização do PostgreSQL e persistência](https://docs.docker.com/guides/postgresql/immediate-setup-and-data-persistence/)

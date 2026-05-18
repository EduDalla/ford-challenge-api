-- V9__cleanup_domain_constraints_and_indexes.sql
-- Consolida ajustes estruturais do domínio para o modelo:
-- Cliente/App -> Backend/BFF -> Banco.
-- Objetivos:
-- 1. Compatibilizar unique constraints com soft delete.
-- 2. Proteger domínio contra valores inválidos.
-- 3. Melhorar índices de consulta.
-- 4. Reduzir risco de perda de histórico por hard delete acidental.

-- =========================================================
-- 1) CLIENTES: unique compatível com exclusão lógica
-- =========================================================

alter table public.clientes
drop constraint if exists uk_clientes_email;

alter table public.clientes
drop constraint if exists uk_clientes_documento;

create unique index if not exists uk_clientes_email_ativo
on public.clientes (lower(email))
where excluido_em is null;

create unique index if not exists uk_clientes_documento_ativo
on public.clientes (documento)
where excluido_em is null;

-- =========================================================
-- 2) CLIENTES: checks de domínio
-- =========================================================

alter table public.clientes
drop constraint if exists ck_clientes_nivel_risco;

alter table public.clientes
add constraint ck_clientes_nivel_risco
check (nivel_risco in ('BAIXO', 'MEDIO', 'ALTO', 'CRITICO'));

-- =========================================================
-- 3) INTERACOES: checks de domínio
-- =========================================================

alter table public.interacoes
drop constraint if exists ck_interacoes_tipo;

alter table public.interacoes
add constraint ck_interacoes_tipo
check (tipo in ('TELEFONE', 'EMAIL', 'WHATSAPP', 'REUNIAO', 'VISITA'));

alter table public.interacoes
drop constraint if exists ck_interacoes_data_nao_futura;

alter table public.interacoes
add constraint ck_interacoes_data_nao_futura
check (data_interacao <= now());

-- =========================================================
-- 4) ÍNDICES para consultas comuns
-- =========================================================

create index if not exists idx_clientes_excluido_em
on public.clientes (excluido_em);

create index if not exists idx_clientes_nivel_risco_ativo
on public.clientes (nivel_risco, ativo)
where excluido_em is null;

create index if not exists idx_clientes_nome_ativo
on public.clientes (nome)
where excluido_em is null;

create index if not exists idx_interacoes_cliente_excluido
on public.interacoes (cliente_id, excluido_em);

create index if not exists idx_interacoes_data
on public.interacoes (data_interacao);

create index if not exists idx_informacoes_user_user_informacao
on public.informacoes_user (user_id, informacao_id);

create index if not exists idx_informacoes_user_data_alerta
on public.informacoes_user (data_alerta);

-- =========================================================
-- 5) Proteger histórico contra hard delete acidental
-- =========================================================
-- A migration original usa ON DELETE CASCADE entre clientes e interacoes.
-- Como agora o domínio usa soft delete, é mais seguro impedir delete físico
-- de cliente que possui interações, em vez de apagar histórico em cascata.

alter table public.interacoes
drop constraint if exists fk_interacoes_clientes;

alter table public.interacoes
add constraint fk_interacoes_clientes
foreign key (cliente_id)
references public.clientes (id)
on delete restrict;

-- =========================================================
-- 6) Comentários de arquitetura
-- =========================================================

comment on table public.clientes is
'Tabela de clientes de retenção. Acesso direto pela Data API bloqueado; acesso esperado via backend/BFF. Usa exclusão lógica por excluido_em.';

comment on table public.interacoes is
'Tabela de interações de retenção vinculadas a clientes. Acesso esperado via backend/BFF. Usa exclusão lógica por excluido_em.';

comment on column public.clientes.excluido_em is
'Quando preenchido, indica exclusão lógica do cliente. Registros excluídos não participam das constraints únicas parciais.';

comment on column public.interacoes.excluido_em is
'Quando preenchido, indica exclusão lógica da interação.';

comment on constraint fk_interacoes_clientes on public.interacoes is
'FK protegida contra hard delete acidental. Como o domínio usa soft delete, não apagamos histórico por cascade.';
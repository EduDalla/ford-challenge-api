-- V2__create_retention_domain.sql
-- Domínio principal do Pulso de Retenção Ford.

create table public.clientes (
    id bigserial primary key,
    nome varchar(120) not null,
    email varchar(150) not null,
    telefone varchar(20),
    documento varchar(30) not null,
    segmento varchar(80),
    nivel_risco varchar(20) not null,
    ativo boolean not null default true,
    criado_em timestamp(6) not null default current_timestamp,
    atualizado_em timestamp(6) not null default current_timestamp,
    excluido_em timestamp(6),

    constraint ck_clientes_nivel_risco
        check (nivel_risco in ('BAIXO', 'MEDIO', 'ALTO', 'CRITICO'))
);

create unique index uk_clientes_email_ativo
on public.clientes (lower(email))
where excluido_em is null;

create unique index uk_clientes_documento_ativo
on public.clientes (documento)
where excluido_em is null;

create index idx_clientes_excluido_em
on public.clientes (excluido_em);

create index idx_clientes_nivel_risco_ativo
on public.clientes (nivel_risco, ativo)
where excluido_em is null;

create index idx_clientes_nome_ativo
on public.clientes (nome)
where excluido_em is null;

create trigger trg_clientes_updated_at
before update on public.clientes
for each row
execute function private.set_updated_at();

alter table public.clientes enable row level security;

revoke all privileges on table public.clientes from anon, authenticated, public;

comment on table public.clientes is
'Clientes do Pulso de Retenção. Acesso direto bloqueado; acesso esperado via backend/BFF/Edge Function. Usa exclusão lógica por excluido_em.';

comment on column public.clientes.excluido_em is
'Quando preenchido, indica exclusão lógica. Registros excluídos não participam das constraints únicas parciais.';


create table public.interacoes (
    id bigserial primary key,
    cliente_id bigint not null,
    tipo varchar(20) not null,
    descricao varchar(500) not null,
    resultado varchar(120) not null,
    data_interacao timestamp(6) not null,
    criado_em timestamp(6) not null default current_timestamp,
    atualizado_em timestamp(6) not null default current_timestamp,
    excluido_em timestamp(6),

    constraint fk_interacoes_clientes
        foreign key (cliente_id)
        references public.clientes(id)
        on delete restrict,

    constraint ck_interacoes_tipo
        check (tipo in ('TELEFONE', 'EMAIL', 'WHATSAPP', 'REUNIAO', 'VISITA'))
);

create index idx_interacoes_cliente_id
on public.interacoes (cliente_id);

create index idx_interacoes_cliente_excluido
on public.interacoes (cliente_id, excluido_em);

create index idx_interacoes_data
on public.interacoes (data_interacao);

create trigger trg_interacoes_updated_at
before update on public.interacoes
for each row
execute function private.set_updated_at();

alter table public.interacoes enable row level security;

revoke all privileges on table public.interacoes from anon, authenticated, public;

comment on table public.interacoes is
'Interações de retenção vinculadas a clientes. Acesso direto bloqueado; acesso esperado via backend/BFF/Edge Function. Usa exclusão lógica por excluido_em.';

comment on column public.interacoes.excluido_em is
'Quando preenchido, indica exclusão lógica da interação.';


create table public.informacoes (
    id bigserial primary key,
    nome varchar(120) not null,
    descricao varchar(500) not null,
    ativo boolean not null default true,
    criado_em timestamp(6) not null default current_timestamp,
    atualizado_em timestamp(6) not null default current_timestamp,

    constraint uk_informacoes_nome unique (nome)
);

create index idx_informacoes_ativo
on public.informacoes (ativo);

create trigger trg_informacoes_updated_at
before update on public.informacoes
for each row
execute function private.set_updated_at();

alter table public.informacoes enable row level security;

revoke all privileges on table public.informacoes from anon, authenticated, public;

comment on table public.informacoes is
'Tabela de informações/alertas funcionais da aplicação. Acesso direto bloqueado; acesso esperado via backend/BFF/Edge Function.';


create table public.informacoes_user (
    id bigserial primary key,
    user_id uuid not null,
    informacao_id bigint not null,
    data_alerta date not null,
    criado_em timestamp(6) not null default current_timestamp,

    constraint fk_informacoes_user_profile
        foreign key (user_id)
        references public.profiles(id)
        on delete cascade,

    constraint fk_informacoes_user_informacao
        foreign key (informacao_id)
        references public.informacoes(id)
        on delete cascade,

    constraint uk_informacoes_user_alerta
        unique (user_id, informacao_id, data_alerta)
);

create index idx_informacoes_user_user_id
on public.informacoes_user (user_id);

create index idx_informacoes_user_informacao_id
on public.informacoes_user (informacao_id);

create index idx_informacoes_user_data_alerta
on public.informacoes_user (data_alerta);

alter table public.informacoes_user enable row level security;

revoke all privileges on table public.informacoes_user from anon, authenticated, public;

comment on table public.informacoes_user is
'Relação entre usuário Supabase Auth/profile e informações/alertas da aplicação. Acesso direto bloqueado; acesso esperado via backend/BFF/Edge Function.';
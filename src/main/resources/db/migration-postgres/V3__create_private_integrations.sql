-- V3__create_private_integrations.sql
-- Credenciais técnicas internas.
-- Não representa usuário humano.

create table private.api_clients (
    id bigserial primary key,
    nome varchar(120) not null,
    client_id varchar(100) not null,
    client_secret_hash varchar(255) not null,
    ativo boolean not null default true,
    criado_em timestamp(6) not null default current_timestamp,
    atualizado_em timestamp(6) not null default current_timestamp,
    ultimo_uso_em timestamp(6),

    constraint uk_api_clients_client_id unique (client_id)
);

create table private.permissions (
    id bigserial primary key,
    codigo varchar(80) not null,
    descricao varchar(255) not null,
    ativo boolean not null default true,
    criado_em timestamp(6) not null default current_timestamp,

    constraint uk_permissions_codigo unique (codigo)
);

create table private.api_client_permissions (
    api_client_id bigint not null,
    permission_id bigint not null,

    primary key (api_client_id, permission_id),

    constraint fk_api_client_permissions_client
        foreign key (api_client_id)
        references private.api_clients(id)
        on delete cascade,

    constraint fk_api_client_permissions_permission
        foreign key (permission_id)
        references private.permissions(id)
        on delete restrict
);

create index idx_api_client_permissions_permission_id
on private.api_client_permissions (permission_id);

create trigger trg_api_clients_updated_at
before update on private.api_clients
for each row
execute function private.set_updated_at();

alter table private.api_clients enable row level security;
alter table private.permissions enable row level security;
alter table private.api_client_permissions enable row level security;

revoke all privileges on table private.api_clients from anon, authenticated, public;
revoke all privileges on table private.permissions from anon, authenticated, public;
revoke all privileges on table private.api_client_permissions from anon, authenticated, public;

comment on table private.api_clients is
'Clientes técnicos internos para autenticação sistema-sistema. Não representa usuário humano.';

comment on table private.permissions is
'Permissões/scopes internos de clientes técnicos.';

comment on table private.api_client_permissions is
'Relacionamento entre clientes técnicos e permissões internas.';
-- V7__harden_supabase_public_schema.sql
-- Garante que o schema public do Supabase não fique exposto via Data API.

alter table public.flyway_schema_history enable row level security;
alter table public.clientes enable row level security;
alter table public.interacoes enable row level security;
alter table public.usuarios enable row level security;
alter table public.usuario_autenticacao enable row level security;
alter table public.api_clients enable row level security;
alter table public.permissions enable row level security;
alter table public.api_client_permissions enable row level security;
alter table public.informacoes enable row level security;
alter table public.informacoes_user enable row level security;

revoke all privileges on table public.flyway_schema_history from anon, authenticated, public;
revoke all privileges on table public.clientes from anon, authenticated, public;
revoke all privileges on table public.interacoes from anon, authenticated, public;
revoke all privileges on table public.usuarios from anon, authenticated, public;
revoke all privileges on table public.usuario_autenticacao from anon, authenticated, public;
revoke all privileges on table public.api_clients from anon, authenticated, public;
revoke all privileges on table public.permissions from anon, authenticated, public;
revoke all privileges on table public.api_client_permissions from anon, authenticated, public;
revoke all privileges on table public.informacoes from anon, authenticated, public;
revoke all privileges on table public.informacoes_user from anon, authenticated, public;

create index if not exists idx_api_client_permissions_permission_id
on public.api_client_permissions(permission_id);

create index if not exists idx_informacoes_user_informacao_id
on public.informacoes_user(informacao_id);
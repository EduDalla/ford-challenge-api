-- V5__harden_database_access.sql
-- Reforço final para modelo backend-controlled.
-- Nenhuma tabela da aplicação deve ser acessada direto por anon/authenticated.

alter table public.profiles enable row level security;
alter table public.clientes enable row level security;
alter table public.interacoes enable row level security;
alter table public.informacoes enable row level security;
alter table public.informacoes_user enable row level security;

alter table private.api_clients enable row level security;
alter table private.permissions enable row level security;
alter table private.api_client_permissions enable row level security;

revoke all privileges on all tables in schema public from anon;
revoke all privileges on all tables in schema public from authenticated;
revoke all privileges on all tables in schema public from public;

revoke all privileges on all tables in schema private from anon;
revoke all privileges on all tables in schema private from authenticated;
revoke all privileges on all tables in schema private from public;

revoke all privileges on all functions in schema private from anon;
revoke all privileges on all functions in schema private from authenticated;
revoke all privileges on all functions in schema private from public;

revoke all on schema private from anon;
revoke all on schema private from authenticated;
revoke all on schema private from public;

alter table public.flyway_schema_history enable row level security;

revoke all privileges on table public.flyway_schema_history from anon;
revoke all privileges on table public.flyway_schema_history from authenticated;
revoke all privileges on table public.flyway_schema_history from public;
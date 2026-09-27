-- Compatibilidade minima para as migrations da aplicacao, apenas em banco local vazio.
-- Nao implementa Supabase Auth, login, refresh token ou a Data API.
create role anon nologin;
create role authenticated nologin;
create schema auth;
create schema extensions;
create extension pgcrypto with schema extensions;

create table auth.users (
    id uuid primary key,
    email text,
    raw_user_meta_data jsonb not null default '{}'::jsonb,
    raw_app_meta_data jsonb not null default '{}'::jsonb
);

-- V1__create_app_foundation.sql
-- Fundação da aplicação.
-- Modelo:
-- Supabase Auth para usuários humanos.
-- Backend/BFF/Edge Function controla acesso ao banco.
-- Sem auth própria em public.usuarios.

create schema if not exists private;

revoke all on schema private from public;
revoke all on schema private from anon;
revoke all on schema private from authenticated;

comment on schema private is
'Schema interno da aplicação. Não deve ser exposto diretamente pela Data API do Supabase.';

-- Função utilitária de updated_at
create or replace function private.set_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
    new.atualizado_em = current_timestamp;
    return new;
end;
$$;

revoke all on function private.set_updated_at() from public;
revoke all on function private.set_updated_at() from anon;
revoke all on function private.set_updated_at() from authenticated;

-- Perfil funcional do usuário Supabase Auth
create table public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    nome varchar(120) not null,
    email varchar(150),
    perfil varchar(30) not null default 'ANALISTA',
    ativo boolean not null default true,
    criado_em timestamp(6) not null default current_timestamp,
    atualizado_em timestamp(6) not null default current_timestamp,

    constraint ck_profiles_perfil
        check (perfil in ('ADMIN', 'ANALISTA', 'GESTOR'))
);

alter table public.profiles enable row level security;

revoke all privileges on table public.profiles from anon, authenticated, public;

create unique index uk_profiles_email_ativo
on public.profiles (lower(email))
where email is not null and ativo = true;

create index idx_profiles_perfil
on public.profiles (perfil);

create index idx_profiles_ativo
on public.profiles (ativo);

create trigger trg_profiles_updated_at
before update on public.profiles
for each row
execute function private.set_updated_at();

-- Cria profile automaticamente ao criar usuário no Supabase Auth
create or replace function private.handle_new_auth_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.profiles (
        id,
        nome,
        email,
        perfil,
        ativo
    )
    values (
        new.id,
        coalesce(
            new.raw_user_meta_data ->> 'nome',
            new.raw_user_meta_data ->> 'name',
            split_part(coalesce(new.email, ''), '@', 1),
            'Usuário'
        ),
        new.email,
        coalesce(
            new.raw_app_meta_data ->> 'perfil',
            'ANALISTA'
        ),
        true
    )
    on conflict (id) do nothing;

    return new;
end;
$$;

revoke all on function private.handle_new_auth_user() from public;
revoke all on function private.handle_new_auth_user() from anon;
revoke all on function private.handle_new_auth_user() from authenticated;

drop trigger if exists on_auth_user_created on auth.users;

create trigger on_auth_user_created
after insert on auth.users
for each row
execute function private.handle_new_auth_user();

comment on table public.profiles is
'Perfil funcional de usuários autenticados via Supabase Auth. Acesso direto bloqueado; acesso esperado via backend/BFF/Edge Function.';

comment on column public.profiles.id is
'UUID do usuário em auth.users(id).';

comment on column public.profiles.perfil is
'Perfil funcional da aplicação: ADMIN, ANALISTA ou GESTOR.';
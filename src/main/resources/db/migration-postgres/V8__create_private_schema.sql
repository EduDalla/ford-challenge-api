-- V8__create_private_schema.sql
-- Schema para futuras tabelas/funções internas não expostas pela API do Supabase.

create schema if not exists private;

revoke all on schema private from public;
revoke all on schema private from anon;
revoke all on schema private from authenticated;

comment on schema private is
'Schema interno para dados/funções sensíveis. Não deve ser exposto diretamente pela Data API do Supabase.';
-- V6__create_mission_domain.sql
-- Ciclo operacional do Pulso de Retencao Ford: Radar -> Missao -> Acao -> Resultado.

create table public.veiculos (
    id bigserial primary key,
    cliente_id bigint not null,
    modelo varchar(80) not null,
    ano integer not null,
    vin_simulado varchar(40) not null,
    km_atual integer not null,
    ultima_revisao date not null,
    dias_sem_servico integer not null,
    status_garantia varchar(40) not null,
    criado_em timestamp(6) not null default current_timestamp,
    atualizado_em timestamp(6) not null default current_timestamp,

    constraint fk_veiculos_clientes
        foreign key (cliente_id)
        references public.clientes(id)
        on delete restrict,
    constraint uk_veiculos_vin_simulado unique (vin_simulado)
);

create index idx_veiculos_cliente_id on public.veiculos (cliente_id);

create trigger trg_veiculos_updated_at
before update on public.veiculos
for each row
execute function private.set_updated_at();

alter table public.veiculos enable row level security;
revoke all privileges on table public.veiculos from anon, authenticated, public;


create table public.missoes (
    id bigserial primary key,
    codigo_cartao varchar(40) not null,
    cliente_id bigint not null,
    veiculo_id bigint not null,
    responsavel_id uuid,
    perfil varchar(80) not null,
    risco varchar(20) not null,
    score integer not null,
    prioridade_radar varchar(2) not null,
    sinais_radar varchar(1200) not null,
    motivo_principal varchar(500) not null,
    acao_recomendada varchar(500) not null,
    mensagem_sugerida varchar(500) not null,
    valor_potencial numeric(12,2) not null,
    impacto_vin_share_estimado numeric(5,2) not null,
    prazo varchar(80) not null,
    status varchar(30) not null,
    criado_em timestamp(6) not null default current_timestamp,
    atualizado_em timestamp(6) not null default current_timestamp,

    constraint uk_missoes_codigo_cartao unique (codigo_cartao),
    constraint fk_missoes_clientes
        foreign key (cliente_id)
        references public.clientes(id)
        on delete restrict,
    constraint fk_missoes_veiculos
        foreign key (veiculo_id)
        references public.veiculos(id)
        on delete restrict,
    constraint fk_missoes_responsavel
        foreign key (responsavel_id)
        references public.profiles(id)
        on delete set null,
    constraint ck_missoes_risco
        check (risco in ('BAIXO', 'MEDIO', 'ALTO')),
    constraint ck_missoes_prioridade
        check (prioridade_radar in ('P1', 'P2', 'P3')),
    constraint ck_missoes_status
        check (status in ('EM_RISCO', 'ASSUMIDO', 'CONTATO_FEITO', 'RESPOSTA_RECEBIDA', 'AGENDADO', 'RECUPERADO', 'REPROGRAMAR', 'PERDIDO')),
    constraint ck_missoes_score
        check (score between 0 and 100)
);

create index idx_missoes_responsavel_id on public.missoes (responsavel_id);
create index idx_missoes_status_prioridade on public.missoes (status, prioridade_radar, score);
create index idx_missoes_cliente_id on public.missoes (cliente_id);

create trigger trg_missoes_updated_at
before update on public.missoes
for each row
execute function private.set_updated_at();

alter table public.missoes enable row level security;
revoke all privileges on table public.missoes from anon, authenticated, public;


create table public.missao_acoes (
    id bigserial primary key,
    missao_id bigint not null,
    tipo varchar(30) not null,
    canal varchar(20) not null,
    observacao varchar(500),
    criado_em timestamp(6) not null default current_timestamp,

    constraint fk_missao_acoes_missoes
        foreign key (missao_id)
        references public.missoes(id)
        on delete cascade,
    constraint ck_missao_acoes_tipo
        check (tipo in ('EM_RISCO', 'ASSUMIDO', 'CONTATO_FEITO', 'RESPOSTA_RECEBIDA', 'AGENDADO', 'RECUPERADO', 'REPROGRAMAR', 'PERDIDO')),
    constraint ck_missao_acoes_canal
        check (canal in ('WHATSAPP', 'TELEFONE', 'EMAIL'))
);

create index idx_missao_acoes_missao_id on public.missao_acoes (missao_id);
create index idx_missao_acoes_criado_em on public.missao_acoes (criado_em);

alter table public.missao_acoes enable row level security;
revoke all privileges on table public.missao_acoes from anon, authenticated, public;


create table public.missao_resultados (
    id bigserial primary key,
    missao_id bigint not null,
    compareceu boolean,
    servico_pago boolean,
    receita_estimada numeric(12,2),
    impacto_vin_share numeric(5,2),
    proximo_passo varchar(255),
    atualizado_em timestamp(6) not null default current_timestamp,

    constraint uk_missao_resultados_missao unique (missao_id),
    constraint fk_missao_resultados_missoes
        foreign key (missao_id)
        references public.missoes(id)
        on delete cascade
);

alter table public.missao_resultados enable row level security;
revoke all privileges on table public.missao_resultados from anon, authenticated, public;


insert into public.clientes (nome, email, telefone, documento, segmento, nivel_risco, ativo)
select 'Mariana Oliveira', 'mariana.oliveira.demo@example.com', '11987654321', '90000000001', 'Sao Paulo', 'ALTO', true
where not exists (select 1 from public.clientes where documento = '90000000001');

insert into public.clientes (nome, email, telefone, documento, segmento, nivel_risco, ativo)
select 'Carlos Mendes', 'carlos.mendes.demo@example.com', '11912345678', '90000000002', 'Campinas', 'MEDIO', true
where not exists (select 1 from public.clientes where documento = '90000000002');

insert into public.veiculos (cliente_id, modelo, ano, vin_simulado, km_atual, ultima_revisao, dias_sem_servico, status_garantia)
select c.id, 'Ford Ka', 2020, 'VIN-DEMO-0001', 48200, current_date - 210, 210, 'Garantia expirada'
from public.clientes c
where c.documento = '90000000001'
  and not exists (select 1 from public.veiculos where vin_simulado = 'VIN-DEMO-0001');

insert into public.veiculos (cliente_id, modelo, ano, vin_simulado, km_atual, ultima_revisao, dias_sem_servico, status_garantia)
select c.id, 'Ford EcoSport', 2019, 'VIN-DEMO-0002', 67200, current_date - 145, 145, 'Garantia expirada'
from public.clientes c
where c.documento = '90000000002'
  and not exists (select 1 from public.veiculos where vin_simulado = 'VIN-DEMO-0002');

insert into public.missoes (
    codigo_cartao, cliente_id, veiculo_id, perfil, risco, score, prioridade_radar, sinais_radar,
    motivo_principal, acao_recomendada, mensagem_sugerida, valor_potencial,
    impacto_vin_share_estimado, prazo, status
)
select
    'CARD-001', c.id, v.id, 'Cliente esquecido', 'ALTO', 87, 'P1',
    concat(
        'Score ML de abandono: 87/100', chr(10),
        '142 dias sem revisao recomendada', chr(10),
        'Alta chance de recuperar com contato rapido'
    ),
    'Cliente esta ha muitos meses sem retornar para revisao.',
    'Lembrete personalizado e agendamento facilitado.',
    'Ola, percebemos que seu Ford Ka pode estar perto de uma revisao. Posso te ajudar com um horario rapido na rede Ford?',
    980.00, 1.4, 'Hoje', 'EM_RISCO'
from public.clientes c
join public.veiculos v on v.cliente_id = c.id
where c.documento = '90000000001'
  and v.vin_simulado = 'VIN-DEMO-0001'
  and not exists (select 1 from public.missoes where codigo_cartao = 'CARD-001');

insert into public.missoes (
    codigo_cartao, cliente_id, veiculo_id, perfil, risco, score, prioridade_radar, sinais_radar,
    motivo_principal, acao_recomendada, mensagem_sugerida, valor_potencial,
    impacto_vin_share_estimado, prazo, status
)
select
    'CARD-002', c.id, v.id, 'Cliente economico', 'MEDIO', 64, 'P2',
    concat(
        'Score ML de abandono: 64/100', chr(10),
        'Sensibilidade a preco', chr(10),
        'Historico de comparacao antes de agendar'
    ),
    'Cliente costuma comparar preco antes de retornar.',
    'Oferecer pacote essencial com preco fechado.',
    'Ola, tenho uma opcao de pacote essencial para seu EcoSport com preco fechado. Posso te mostrar?',
    720.00, 0.8, 'Amanha', 'EM_RISCO'
from public.clientes c
join public.veiculos v on v.cliente_id = c.id
where c.documento = '90000000002'
  and v.vin_simulado = 'VIN-DEMO-0002'
  and not exists (select 1 from public.missoes where codigo_cartao = 'CARD-002');

comment on table public.missoes is
'Missoes operacionais do Pulso de Retencao Ford. Acesso esperado via Java BFF com Supabase JWT.';

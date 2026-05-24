-- V8__enhance_ml_pipeline.sql
-- Evolucao nao-destrutiva do pipeline ML orientado a snapshots.

create extension if not exists pgcrypto;
create schema if not exists ml;

revoke all on schema ml from public;
revoke all on schema ml from anon;
revoke all on schema ml from authenticated;

alter table if exists public.clientes
    add column if not exists nome_simulado varchar(120),
    add column if not exists canal_preferido varchar(40),
    add column if not exists regiao varchar(80);

alter table if exists public.veiculos
    add column if not exists sales_date date,
    add column if not exists delivery_date date;

create index if not exists idx_vin_share_servicos_vin on public.vin_share_servicos (vin_hash);
create index if not exists idx_vin_share_servicos_data on public.vin_share_servicos (service_date);
create index if not exists idx_vin_share_servicos_model_name on public.vin_share_servicos (model_name);
create index if not exists idx_vin_share_servicos_dealer on public.vin_share_servicos (dealer_code);

alter table if exists public.ml_feature_refresh_queue
    add column if not exists motivo varchar(80) not null default 'servico_alterado',
    add column if not exists processado_em timestamp(6),
    add column if not exists erro text,
    add column if not exists atualizado_em timestamp(6) not null default current_timestamp;

alter table if exists public.ml_feature_refresh_queue
    drop constraint if exists ck_ml_feature_refresh_queue_status;

update public.ml_feature_refresh_queue
set status = case status
    when 'pending_features' then 'pending'
    when 'features_processing' then 'processing'
    when 'ready_to_predict' then 'done'
    when 'prediction_processing' then 'processing'
    when 'completed' then 'done'
    else 'failed'
end
where status not in ('pending', 'processing', 'done', 'failed');

alter table if exists public.ml_feature_refresh_queue
    add constraint ck_ml_feature_refresh_queue_status
        check (status in ('pending', 'processing', 'done', 'failed'));

create unique index if not exists uk_ml_queue_vin_corte_status_open
on public.ml_feature_refresh_queue (vin_hash, data_corte)
where status in ('pending', 'processing');

alter table if exists public.vin_share_feature_snapshots
    add column if not exists status_predicao varchar(30) not null default 'pending',
    add column if not exists tentativas_predicao integer not null default 0,
    add column if not exists erro_predicao text,
    add column if not exists predito_em timestamp(6),
    add column if not exists atualizado_em timestamp(6) not null default current_timestamp;

alter table if exists public.vin_share_feature_snapshots
    drop constraint if exists ck_vin_share_feature_snapshots_status_predicao;

alter table if exists public.vin_share_feature_snapshots
    add constraint ck_vin_share_feature_snapshots_status_predicao
        check (status_predicao in ('pending', 'processing', 'completed', 'failed', 'retry'));

create index if not exists idx_feature_snapshots_status_predicao
on public.vin_share_feature_snapshots (status_predicao, criado_em);

create index if not exists idx_feature_snapshots_vin_data
on public.vin_share_feature_snapshots (vin_hash, data_corte);

create table if not exists public.job_execution_logs (
    id uuid primary key default gen_random_uuid(),
    job_name varchar(120) not null,
    status varchar(30) not null,
    started_at timestamptz not null,
    finished_at timestamptz,
    duration_ms bigint,
    total_processed integer not null default 0,
    total_success integer not null default 0,
    total_failed integer not null default 0,
    error_message text,
    created_at timestamptz not null default current_timestamp
);

create index if not exists idx_job_execution_logs_job_started
on public.job_execution_logs (job_name, started_at desc);

create index if not exists idx_job_execution_logs_status
on public.job_execution_logs (status, started_at desc);

alter table public.job_execution_logs enable row level security;
revoke all privileges on table public.job_execution_logs from anon, authenticated, public;

create or replace function ml.build_predict_payload(
    p_vin_hash varchar,
    p_data_corte date
)
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_payload jsonb;
begin
    with servicos as (
        select s.*
        from public.vin_share_servicos s
        where s.vin_hash = p_vin_hash
          and s.service_date <= p_data_corte
    ),
    intervalos as (
        select avg(delta)::numeric as intervalo_medio
        from (
            select (s.service_date - lag(s.service_date) over (order by s.service_date))::numeric as delta
            from servicos s
        ) t
        where t.delta is not null
    ),
    base as (
        select
            coalesce(max(v.modelo), max(s.model_name), 'DESCONHECIDO') as modelo,
            coalesce(max(v.ano), max(s.model_year), extract(year from p_data_corte)::integer) as ano_modelo,
            count(*)::integer as qtde_revisoes,
            round(((p_data_corte - max(s.service_date)) / 30.44)::numeric, 2) as meses_desde_ultimo,
            round(((p_data_corte - min(coalesce(s.sales_date, s.delivery_date, s.service_date))) / 30.44)::numeric, 2) as meses_relacionamento,
            count(distinct s.dealer_code)::integer as n_dealers,
            max(s.km)::numeric(12,2) as km_max,
            round(avg(case when s.agenda_flag then 1.0 else 0.0 end)::numeric, 4) as pct_agenda,
            round(max(i.intervalo_medio)::numeric, 2) as intervalo_medio_revisoes,
            case
                when min(s.service_date) is not null and min(coalesce(s.sales_date, s.delivery_date, s.service_date)) is not null
                then greatest(0, min(s.service_date) - min(coalesce(s.sales_date, s.delivery_date, s.service_date)))
                else null
            end as dias_ate_primeira_revisao
        from servicos s
        left join public.veiculos v
            on v.vin_hash = s.vin_hash or v.vin_simulado = s.vin_hash
        cross join intervalos i
    )
    select jsonb_build_object(
        'ano_modelo', b.ano_modelo,
        'qtde_revisoes_ate_corte', coalesce(b.qtde_revisoes, 0),
        'meses_desde_ultimo_servico_ate_corte', coalesce(b.meses_desde_ultimo, 999),
        'meses_relacionamento_ate_corte', coalesce(b.meses_relacionamento, 0),
        'n_dealers_usados_ate_corte', coalesce(b.n_dealers, 0),
        'km_max_ate_corte', coalesce(b.km_max, 0),
        'pct_agenda_ate_corte', coalesce(b.pct_agenda, 0),
        'intervalo_medio_revisoes_dias_ate_corte', coalesce(b.intervalo_medio_revisoes, 365),
        'dias_ate_primeira_revisao', coalesce(b.dias_ate_primeira_revisao, 365),
        'idade_veiculo_meses_ate_corte', round(((p_data_corte - make_date(b.ano_modelo, 1, 1)) / 30.44)::numeric, 2),
        'modelo', b.modelo
    )
    into v_payload
    from base b;

    return v_payload;
end;
$$;

revoke all on function ml.build_predict_payload(varchar, date) from public;
revoke all on function ml.build_predict_payload(varchar, date) from anon;
revoke all on function ml.build_predict_payload(varchar, date) from authenticated;

create or replace function ml.refresh_feature_snapshot(
    p_vin_hash varchar,
    p_data_corte date
)
returns bigint
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_snapshot_id bigint;
    v_veiculo_id bigint;
    v_cliente_id bigint;
    v_payload jsonb;
    v_tem_servico boolean;
begin
    select exists(
        select 1
        from public.vin_share_servicos s
        where s.vin_hash = p_vin_hash
          and s.service_date <= p_data_corte
    )
    into v_tem_servico;

    if not v_tem_servico then
        raise exception 'Nao ha historico de servico para o VIN % na data %', p_vin_hash, p_data_corte;
    end if;

    select v.id, v.cliente_id
    into v_veiculo_id, v_cliente_id
    from public.veiculos v
    where v.vin_hash = p_vin_hash or v.vin_simulado = p_vin_hash
    order by v.atualizado_em desc nulls last, v.id desc
    limit 1;

    if v_veiculo_id is null then
        select max(s.cliente_id)
        into v_cliente_id
        from public.vin_share_servicos s
        where s.vin_hash = p_vin_hash
          and s.service_date <= p_data_corte;
    end if;

    select ml.build_predict_payload(p_vin_hash, p_data_corte)
    into v_payload;

    if v_payload is null then
        raise exception 'Nao ha dados para snapshot do VIN % na data %', p_vin_hash, p_data_corte;
    end if;

    insert into public.vin_share_feature_snapshots (
        veiculo_id,
        cliente_id,
        vin_hash,
        data_corte,
        modelo,
        ano_modelo,
        qtde_revisoes_ate_corte,
        meses_desde_ultimo_servico_ate_corte,
        meses_relacionamento_ate_corte,
        n_dealers_usados_ate_corte,
        km_max_ate_corte,
        pct_agenda_ate_corte,
        intervalo_medio_revisoes_dias_ate_corte,
        dias_ate_primeira_revisao,
        idade_veiculo_meses_ate_corte,
        payload_predict,
        payload_hash,
        status_predicao,
        tentativas_predicao,
        erro_predicao,
        atualizado_em
    )
    values (
        v_veiculo_id,
        v_cliente_id,
        p_vin_hash,
        p_data_corte,
        coalesce(v_payload ->> 'modelo', 'DESCONHECIDO'),
        coalesce((v_payload ->> 'ano_modelo')::integer, extract(year from p_data_corte)::integer),
        coalesce((v_payload ->> 'qtde_revisoes_ate_corte')::integer, 0),
        coalesce((v_payload ->> 'meses_desde_ultimo_servico_ate_corte')::numeric, 0),
        coalesce((v_payload ->> 'meses_relacionamento_ate_corte')::numeric, 0),
        coalesce((v_payload ->> 'n_dealers_usados_ate_corte')::integer, 0),
        coalesce((v_payload ->> 'km_max_ate_corte')::numeric, 0),
        coalesce((v_payload ->> 'pct_agenda_ate_corte')::numeric, 0),
        coalesce((v_payload ->> 'intervalo_medio_revisoes_dias_ate_corte')::numeric, 365),
        coalesce((v_payload ->> 'dias_ate_primeira_revisao')::integer, 365),
        coalesce((v_payload ->> 'idade_veiculo_meses_ate_corte')::numeric, 0),
        v_payload,
        md5(v_payload::text),
        'pending',
        0,
        null,
        current_timestamp
    )
    on conflict (vin_hash, data_corte)
    do update set
        veiculo_id = excluded.veiculo_id,
        cliente_id = excluded.cliente_id,
        modelo = excluded.modelo,
        ano_modelo = excluded.ano_modelo,
        qtde_revisoes_ate_corte = excluded.qtde_revisoes_ate_corte,
        meses_desde_ultimo_servico_ate_corte = excluded.meses_desde_ultimo_servico_ate_corte,
        meses_relacionamento_ate_corte = excluded.meses_relacionamento_ate_corte,
        n_dealers_usados_ate_corte = excluded.n_dealers_usados_ate_corte,
        km_max_ate_corte = excluded.km_max_ate_corte,
        pct_agenda_ate_corte = excluded.pct_agenda_ate_corte,
        intervalo_medio_revisoes_dias_ate_corte = excluded.intervalo_medio_revisoes_dias_ate_corte,
        dias_ate_primeira_revisao = excluded.dias_ate_primeira_revisao,
        idade_veiculo_meses_ate_corte = excluded.idade_veiculo_meses_ate_corte,
        payload_predict = excluded.payload_predict,
        payload_hash = excluded.payload_hash,
        status_predicao = 'pending',
        tentativas_predicao = 0,
        erro_predicao = null,
        predito_em = null,
        atualizado_em = current_timestamp
    returning id into v_snapshot_id;

    return v_snapshot_id;
end;
$$;

revoke all on function ml.refresh_feature_snapshot(varchar, date) from public;
revoke all on function ml.refresh_feature_snapshot(varchar, date) from anon;
revoke all on function ml.refresh_feature_snapshot(varchar, date) from authenticated;

drop function if exists ml.refresh_pending_features(date, integer);

create or replace function ml.refresh_pending_features(
    p_data_corte date,
    p_batch_size integer default 100
)
returns integer
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_item record;
    v_total integer := 0;
begin
    for v_item in
        select q.id, q.vin_hash
        from public.ml_feature_refresh_queue q
        where q.status = 'pending'
          and q.data_corte = p_data_corte
        order by q.criado_em
        limit greatest(1, coalesce(p_batch_size, 100))
        for update skip locked
    loop
        update public.ml_feature_refresh_queue
        set status = 'processing',
            erro = null,
            atualizado_em = current_timestamp
        where id = v_item.id;

        begin
            perform ml.refresh_feature_snapshot(v_item.vin_hash, p_data_corte);

            update public.ml_feature_refresh_queue
            set status = 'done',
                processado_em = current_timestamp,
                erro = null,
                atualizado_em = current_timestamp
            where id = v_item.id;

            v_total := v_total + 1;
        exception when others then
            update public.ml_feature_refresh_queue
            set status = 'failed',
                processado_em = current_timestamp,
                erro = left(sqlerrm, 1000),
                atualizado_em = current_timestamp
            where id = v_item.id;
        end;
    end loop;

    return v_total;
end;
$$;

revoke all on function ml.refresh_pending_features(date, integer) from public;
revoke all on function ml.refresh_pending_features(date, integer) from anon;
revoke all on function ml.refresh_pending_features(date, integer) from authenticated;

create or replace function ml.mark_prediction_completed(
    p_snapshot_id bigint
)
returns void
language plpgsql
security definer
set search_path = ''
as $$
begin
    update public.vin_share_feature_snapshots
    set status_predicao = 'completed',
        erro_predicao = null,
        predito_em = current_timestamp,
        atualizado_em = current_timestamp
    where id = p_snapshot_id;
end;
$$;

revoke all on function ml.mark_prediction_completed(bigint) from public;
revoke all on function ml.mark_prediction_completed(bigint) from anon;
revoke all on function ml.mark_prediction_completed(bigint) from authenticated;

create or replace function ml.mark_prediction_failed(
    p_snapshot_id bigint,
    p_error text,
    p_max_attempts integer
)
returns void
language plpgsql
security definer
set search_path = ''
as $$
begin
    update public.vin_share_feature_snapshots
    set tentativas_predicao = tentativas_predicao + 1,
        erro_predicao = left(coalesce(p_error, 'erro_desconhecido'), 2000),
        status_predicao = case
            when (tentativas_predicao + 1) >= greatest(1, coalesce(p_max_attempts, 3)) then 'failed'
            else 'retry'
        end,
        atualizado_em = current_timestamp
    where id = p_snapshot_id;
end;
$$;

revoke all on function ml.mark_prediction_failed(bigint, text, integer) from public;
revoke all on function ml.mark_prediction_failed(bigint, text, integer) from anon;
revoke all on function ml.mark_prediction_failed(bigint, text, integer) from authenticated;

create or replace function public.fn_enqueue_feature_refresh()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
    insert into public.ml_feature_refresh_queue (
        vin_hash,
        data_corte,
        status,
        motivo,
        erro,
        atualizado_em
    )
    values (
        new.vin_hash,
        current_date,
        'pending',
        'servico_alterado',
        null,
        current_timestamp
    )
    on conflict (vin_hash, data_corte)
    do update set
        status = 'pending',
        motivo = excluded.motivo,
        erro = null,
        atualizado_em = current_timestamp;

    return new;
end;
$$;

drop trigger if exists trg_enqueue_feature_refresh on public.vin_share_servicos;
create trigger trg_enqueue_feature_refresh
after insert or update on public.vin_share_servicos
for each row
execute function public.fn_enqueue_feature_refresh();

revoke all on function public.fn_enqueue_feature_refresh() from public;
revoke all on function public.fn_enqueue_feature_refresh() from anon;
revoke all on function public.fn_enqueue_feature_refresh() from authenticated;

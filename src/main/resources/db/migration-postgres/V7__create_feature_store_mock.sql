-- V7__create_feature_store_mock.sql
-- Feature store mock do Pulso de Retencao Ford.
-- O banco prepara snapshots e fila; o BFF orquestra a chamada HTTP para a IA.

create extension if not exists pgcrypto;

alter table public.veiculos
add column if not exists vin_hash varchar(80);

create unique index if not exists uk_veiculos_vin_hash
on public.veiculos (vin_hash)
where vin_hash is not null;


create table public.vin_share_servicos (
    id bigserial primary key,

    vin_hash varchar(80) not null,
    cliente_id bigint,
    veiculo_id bigint,

    country varchar(10),
    schedule_id bigint,
    maintenance_id bigint,
    service_order varchar(80),

    service_date date not null,
    service_open_date date,
    service_closed_date date,
    invoice_date date,
    sales_date date,
    delivery_date date,
    registration_date date,
    warranty_start_date date,

    service_dept_code varchar(40),
    service_repair_type_code varchar(40),
    service_type varchar(80),
    service_code varchar(40),
    maintenance_number integer,
    dealer_code varchar(40),
    main_source varchar(120),
    agenda_flag boolean not null default false,
    status_usa varchar(80),

    model_year integer not null,
    model_name varchar(80) not null,
    km numeric(12,2),

    origem varchar(120) not null default 'vin_share_Desafio_02.xlsx',
    criado_em timestamp(6) not null default current_timestamp,
    atualizado_em timestamp(6) not null default current_timestamp,

    constraint fk_vin_share_servicos_clientes
        foreign key (cliente_id)
        references public.clientes(id)
        on delete set null,
    constraint fk_vin_share_servicos_veiculos
        foreign key (veiculo_id)
        references public.veiculos(id)
        on delete set null
);

create index idx_vin_share_servicos_vin
on public.vin_share_servicos (vin_hash);

create index idx_vin_share_servicos_vin_data
on public.vin_share_servicos (vin_hash, service_date);

create index idx_vin_share_servicos_data
on public.vin_share_servicos (service_date);

create index idx_vin_share_servicos_modelo
on public.vin_share_servicos (model_name, model_year);

create trigger trg_vin_share_servicos_updated_at
before update on public.vin_share_servicos
for each row
execute function private.set_updated_at();

alter table public.vin_share_servicos enable row level security;
revoke all privileges on table public.vin_share_servicos from anon, authenticated, public;


create table public.vin_share_feature_snapshots (
    id bigserial primary key,

    veiculo_id bigint,
    cliente_id bigint,
    vin_hash varchar(80) not null,
    data_corte date not null,

    modelo varchar(80) not null,
    ano_modelo integer not null,

    qtde_revisoes_ate_corte integer,
    meses_desde_ultimo_servico_ate_corte numeric(8,2),
    meses_relacionamento_ate_corte numeric(8,2),
    n_dealers_usados_ate_corte integer,
    km_max_ate_corte numeric(12,2),
    pct_agenda_ate_corte numeric(5,4),
    intervalo_medio_revisoes_dias_ate_corte numeric(10,2),
    dias_ate_primeira_revisao integer,
    idade_veiculo_meses_ate_corte numeric(8,2),

    payload_predict jsonb not null,
    payload_hash varchar(64) not null,
    feature_version varchar(40) not null default 'vin_share_v1',
    imputation_report jsonb not null default '{}'::jsonb,

    criado_em timestamp(6) not null default current_timestamp,
    atualizado_em timestamp(6) not null default current_timestamp,

    constraint uk_vin_share_feature_snapshots_vin_corte
        unique (vin_hash, data_corte),
    constraint fk_vin_share_feature_snapshots_veiculos
        foreign key (veiculo_id)
        references public.veiculos(id)
        on delete set null,
    constraint fk_vin_share_feature_snapshots_clientes
        foreign key (cliente_id)
        references public.clientes(id)
        on delete set null
);

create index idx_feature_snapshots_vin
on public.vin_share_feature_snapshots (vin_hash);

create index idx_feature_snapshots_corte
on public.vin_share_feature_snapshots (data_corte);

create trigger trg_vin_share_feature_snapshots_updated_at
before update on public.vin_share_feature_snapshots
for each row
execute function private.set_updated_at();

alter table public.vin_share_feature_snapshots enable row level security;
revoke all privileges on table public.vin_share_feature_snapshots from anon, authenticated, public;


create table public.ml_feature_refresh_queue (
    id bigserial primary key,

    vin_hash varchar(80) not null,
    data_corte date not null,
    snapshot_id bigint,

    status varchar(40) not null default 'pending_features',
    motivo varchar(80) not null default 'servico_alterado',
    tentativas integer not null default 0,
    erro text,

    criado_em timestamp(6) not null default current_timestamp,
    atualizado_em timestamp(6) not null default current_timestamp,
    processado_em timestamp(6),

    constraint uk_ml_feature_refresh_queue_vin_corte
        unique (vin_hash, data_corte),
    constraint fk_ml_feature_refresh_queue_snapshot
        foreign key (snapshot_id)
        references public.vin_share_feature_snapshots(id)
        on delete set null,
    constraint ck_ml_feature_refresh_queue_status
        check (status in (
            'pending_features',
            'features_processing',
            'ready_to_predict',
            'prediction_processing',
            'completed',
            'failed'
        ))
);

create index idx_ml_queue_status_criado
on public.ml_feature_refresh_queue (status, criado_em);

create index idx_ml_queue_snapshot
on public.ml_feature_refresh_queue (snapshot_id);

create trigger trg_ml_feature_refresh_queue_updated_at
before update on public.ml_feature_refresh_queue
for each row
execute function private.set_updated_at();

alter table public.ml_feature_refresh_queue enable row level security;
revoke all privileges on table public.ml_feature_refresh_queue from anon, authenticated, public;


create table public.predicao_resultados (
    id bigserial primary key,

    queue_id bigint,
    snapshot_id bigint not null,
    veiculo_id bigint,
    cliente_id bigint,
    vin_hash varchar(80) not null,

    perfil varchar(80),
    risco varchar(30),
    score numeric(8,6),
    motivo_principal text,
    acao_recomendada text,
    canal_recomendado varchar(40),

    modelo_versao varchar(80),
    payload_resposta jsonb,

    criado_em timestamp(6) not null default current_timestamp,
    executado_em timestamp(6) not null default current_timestamp,

    constraint uk_predicao_resultados_queue
        unique (queue_id),
    constraint fk_predicao_resultados_queue
        foreign key (queue_id)
        references public.ml_feature_refresh_queue(id)
        on delete set null,
    constraint fk_predicao_resultados_snapshot
        foreign key (snapshot_id)
        references public.vin_share_feature_snapshots(id)
        on delete restrict,
    constraint fk_predicao_resultados_veiculos
        foreign key (veiculo_id)
        references public.veiculos(id)
        on delete set null,
    constraint fk_predicao_resultados_clientes
        foreign key (cliente_id)
        references public.clientes(id)
        on delete set null
);

create index idx_predicao_resultados_vin
on public.predicao_resultados (vin_hash);

create index idx_predicao_resultados_executado
on public.predicao_resultados (executado_em);

alter table public.predicao_resultados enable row level security;
revoke all privileges on table public.predicao_resultados from anon, authenticated, public;


create schema if not exists ml;
revoke all on schema ml from public;
revoke all on schema ml from anon;
revoke all on schema ml from authenticated;


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
    v_payload jsonb;
    v_imputation_report jsonb;
begin
    with servicos as (
        select *
        from public.vin_share_servicos
        where vin_hash = p_vin_hash
          and service_date <= p_data_corte
    ),
    base as (
        select
            p_vin_hash as vin_hash,
            p_data_corte as data_corte,
            max(v.id) as veiculo_id,
            max(coalesce(v.cliente_id, s.cliente_id)) as cliente_id,
            coalesce(max(v.modelo), max(s.model_name), 'DESCONHECIDO') as modelo,
            coalesce(max(v.ano), max(s.model_year), extract(year from p_data_corte)::integer) as ano_modelo,
            count(s.id)::integer as qtde_revisoes,
            round(((p_data_corte - max(s.service_date)) / 30.44)::numeric, 2) as meses_desde_ultimo,
            round(((p_data_corte - min(coalesce(s.sales_date, s.delivery_date, s.service_date))) / 30.44)::numeric, 2) as meses_relacionamento,
            count(distinct s.dealer_code)::integer as n_dealers,
            max(s.km) as km_max,
            round(avg(case when s.agenda_flag then 1.0 else 0.0 end)::numeric, 4) as pct_agenda,
            min(s.service_date) as primeira_revisao,
            min(coalesce(s.sales_date, s.delivery_date, s.service_date)) as inicio_relacionamento
        from servicos s
        left join public.veiculos v on v.vin_hash = s.vin_hash
    ),
    intervalo as (
        select avg(service_date - service_date_anterior)::numeric as intervalo_medio
        from (
            select
                service_date,
                lag(service_date) over (order by service_date) as service_date_anterior
            from servicos
        ) diff
        where service_date_anterior is not null
    ),
    mediana_km as (
        select percentile_cont(0.5) within group (order by km)::numeric(12,2) as valor
        from public.vin_share_servicos
        where km is not null
          and model_year = (select ano_modelo from base)
    ),
    mediana_intervalo as (
        select percentile_cont(0.5) within group (order by delta)::numeric(10,2) as valor
        from (
            select service_date - lag(service_date) over (partition by vin_hash order by service_date) as delta
            from public.vin_share_servicos
            where model_year = (select ano_modelo from base)
        ) x
        where delta is not null
    ),
    features as (
        select
            b.*,
            round(i.intervalo_medio, 2) as intervalo_medio,
            case
                when b.primeira_revisao is not null and b.inicio_relacionamento is not null
                    then greatest(0, b.primeira_revisao - b.inicio_relacionamento)
                else null
            end as dias_primeira_revisao,
            round(((p_data_corte - make_date(b.ano_modelo, 1, 1)) / 30.44)::numeric, 2) as idade_veiculo_meses,
            mk.valor as mediana_km,
            mi.valor as mediana_intervalo
        from base b
        cross join intervalo i
        cross join mediana_km mk
        cross join mediana_intervalo mi
    ),
    payload as (
        select
            f.*,
            jsonb_build_object(
                'ano_modelo', f.ano_modelo,
                'qtde_revisoes_ate_corte', coalesce(f.qtde_revisoes, 0),
                'meses_desde_ultimo_servico_ate_corte', coalesce(f.meses_desde_ultimo, 999),
                'meses_relacionamento_ate_corte', coalesce(f.meses_relacionamento, f.idade_veiculo_meses, 0),
                'n_dealers_usados_ate_corte', coalesce(f.n_dealers, 0),
                'km_max_ate_corte', coalesce(f.km_max, f.mediana_km, 0),
                'pct_agenda_ate_corte', coalesce(f.pct_agenda, 0),
                'intervalo_medio_revisoes_dias_ate_corte', coalesce(f.intervalo_medio, f.mediana_intervalo, 365),
                'dias_ate_primeira_revisao', coalesce(f.dias_primeira_revisao, 365),
                'idade_veiculo_meses_ate_corte', coalesce(f.idade_veiculo_meses, 0),
                'modelo', f.modelo
            ) as payload_predict,
            jsonb_strip_nulls(jsonb_build_object(
                'meses_desde_ultimo_servico_ate_corte',
                    case when f.meses_desde_ultimo is null then 'fallback_999' end,
                'meses_relacionamento_ate_corte',
                    case when f.meses_relacionamento is null then 'idade_veiculo_meses' end,
                'km_max_ate_corte',
                    case when f.km_max is null then 'median_model_year' end,
                'pct_agenda_ate_corte',
                    case when f.pct_agenda is null then 'fallback_0' end,
                'intervalo_medio_revisoes_dias_ate_corte',
                    case when f.intervalo_medio is null then 'median_model_year_or_365' end,
                'dias_ate_primeira_revisao',
                    case when f.dias_primeira_revisao is null then 'fallback_365' end
            )) as imputation_report
        from features f
    )
    select payload_predict, imputation_report
    into v_payload, v_imputation_report
    from payload;

    if v_payload is null then
        raise exception 'Nao ha dados para calcular snapshot do VIN % em %', p_vin_hash, p_data_corte;
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
        feature_version,
        imputation_report,
        atualizado_em
    )
    select
        p.veiculo_id,
        p.cliente_id,
        p.vin_hash,
        p.data_corte,
        p.modelo,
        p.ano_modelo,
        p.qtde_revisoes,
        p.meses_desde_ultimo,
        p.meses_relacionamento,
        p.n_dealers,
        p.km_max,
        p.pct_agenda,
        p.intervalo_medio,
        p.dias_primeira_revisao,
        p.idade_veiculo_meses,
        p.payload_predict,
        encode(extensions.digest(p.payload_predict::text, 'sha256'), 'hex'),
        'vin_share_v1',
        p.imputation_report,
        current_timestamp
    from (
        with servicos as (
            select *
            from public.vin_share_servicos
            where vin_hash = p_vin_hash
              and service_date <= p_data_corte
        ),
        base as (
            select
                p_vin_hash as vin_hash,
                p_data_corte as data_corte,
                max(v.id) as veiculo_id,
                max(coalesce(v.cliente_id, s.cliente_id)) as cliente_id,
                coalesce(max(v.modelo), max(s.model_name), 'DESCONHECIDO') as modelo,
                coalesce(max(v.ano), max(s.model_year), extract(year from p_data_corte)::integer) as ano_modelo,
                count(s.id)::integer as qtde_revisoes,
                round(((p_data_corte - max(s.service_date)) / 30.44)::numeric, 2) as meses_desde_ultimo,
                round(((p_data_corte - min(coalesce(s.sales_date, s.delivery_date, s.service_date))) / 30.44)::numeric, 2) as meses_relacionamento,
                count(distinct s.dealer_code)::integer as n_dealers,
                max(s.km) as km_max,
                round(avg(case when s.agenda_flag then 1.0 else 0.0 end)::numeric, 4) as pct_agenda,
                min(s.service_date) as primeira_revisao,
                min(coalesce(s.sales_date, s.delivery_date, s.service_date)) as inicio_relacionamento
            from servicos s
            left join public.veiculos v on v.vin_hash = s.vin_hash
        ),
        intervalo as (
            select avg(service_date - service_date_anterior)::numeric as intervalo_medio
            from (
                select
                    service_date,
                    lag(service_date) over (order by service_date) as service_date_anterior
                from servicos
            ) diff
            where service_date_anterior is not null
        ),
        mediana_km as (
            select percentile_cont(0.5) within group (order by km)::numeric(12,2) as valor
            from public.vin_share_servicos
            where km is not null
              and model_year = (select ano_modelo from base)
        ),
        mediana_intervalo as (
            select percentile_cont(0.5) within group (order by delta)::numeric(10,2) as valor
            from (
                select service_date - lag(service_date) over (partition by vin_hash order by service_date) as delta
                from public.vin_share_servicos
                where model_year = (select ano_modelo from base)
            ) x
            where delta is not null
        ),
        features as (
            select
                b.*,
                round(i.intervalo_medio, 2) as intervalo_medio,
                case
                    when b.primeira_revisao is not null and b.inicio_relacionamento is not null
                        then greatest(0, b.primeira_revisao - b.inicio_relacionamento)
                    else null
                end as dias_primeira_revisao,
                round(((p_data_corte - make_date(b.ano_modelo, 1, 1)) / 30.44)::numeric, 2) as idade_veiculo_meses,
                mk.valor as mediana_km,
                mi.valor as mediana_intervalo
            from base b
            cross join intervalo i
            cross join mediana_km mk
            cross join mediana_intervalo mi
        )
        select
            f.*,
            v_payload as payload_predict,
            v_imputation_report as imputation_report
        from features f
    ) p
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
        feature_version = excluded.feature_version,
        imputation_report = excluded.imputation_report,
        atualizado_em = current_timestamp
    returning id into v_snapshot_id;

    insert into public.ml_feature_refresh_queue (
        vin_hash,
        data_corte,
        snapshot_id,
        status,
        motivo,
        erro,
        atualizado_em
    )
    values (
        p_vin_hash,
        p_data_corte,
        v_snapshot_id,
        'ready_to_predict',
        'snapshot_atualizado',
        null,
        current_timestamp
    )
    on conflict (vin_hash, data_corte)
    do update set
        snapshot_id = excluded.snapshot_id,
        status = 'ready_to_predict',
        motivo = excluded.motivo,
        erro = null,
        atualizado_em = current_timestamp;

    return v_snapshot_id;
end;
$$;

revoke all on function ml.refresh_feature_snapshot(varchar, date) from public;
revoke all on function ml.refresh_feature_snapshot(varchar, date) from anon;
revoke all on function ml.refresh_feature_snapshot(varchar, date) from authenticated;


create or replace function ml.refresh_pending_features(
    p_data_corte date,
    p_limit integer default 100
)
returns integer
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_queue record;
    v_processados integer := 0;
begin
    for v_queue in
        select id, vin_hash
        from public.ml_feature_refresh_queue
        where status = 'pending_features'
          and data_corte = p_data_corte
        order by criado_em
        limit greatest(1, coalesce(p_limit, 100))
        for update skip locked
    loop
        update public.ml_feature_refresh_queue
        set status = 'features_processing',
            erro = null,
            atualizado_em = current_timestamp
        where id = v_queue.id;

        begin
            perform ml.refresh_feature_snapshot(v_queue.vin_hash, p_data_corte);
            v_processados := v_processados + 1;
        exception when others then
            update public.ml_feature_refresh_queue
            set status = 'failed',
                tentativas = tentativas + 1,
                erro = left(sqlerrm, 1000),
                processado_em = current_timestamp,
                atualizado_em = current_timestamp
            where id = v_queue.id;
        end;
    end loop;

    return v_processados;
end;
$$;

revoke all on function ml.refresh_pending_features(date, integer) from public;
revoke all on function ml.refresh_pending_features(date, integer) from anon;
revoke all on function ml.refresh_pending_features(date, integer) from authenticated;


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
        date '2024-10-31',
        'pending_features',
        'servico_insert_update',
        null,
        current_timestamp
    )
    on conflict (vin_hash, data_corte)
    do update set
        status = 'pending_features',
        motivo = 'servico_insert_update',
        erro = null,
        atualizado_em = current_timestamp;

    return new;
end;
$$;

create trigger trg_enqueue_feature_refresh
after insert or update on public.vin_share_servicos
for each row
execute function public.fn_enqueue_feature_refresh();

revoke all on function public.fn_enqueue_feature_refresh() from public;
revoke all on function public.fn_enqueue_feature_refresh() from anon;
revoke all on function public.fn_enqueue_feature_refresh() from authenticated;

comment on table public.vin_share_servicos is
'Fonte operacional Ford simulada com historico de servicos VIN Share para a demo.';

comment on table public.vin_share_feature_snapshots is
'Feature store mock com uma linha por vin_hash e data_corte. Status de processamento fica na fila.';

comment on table public.ml_feature_refresh_queue is
'Fila persistente de snapshots que o BFF reserva e envia para a IA em lote.';

comment on table public.predicao_resultados is
'Historico de resultados retornados pela IA, incluindo versao do modelo e payload de resposta.';

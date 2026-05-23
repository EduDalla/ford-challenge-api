ALTER TABLE veiculos ADD COLUMN vin_hash VARCHAR(80);

CREATE UNIQUE INDEX uk_veiculos_vin_hash ON veiculos (vin_hash);

CREATE TABLE vin_share_servicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vin_hash VARCHAR(80) NOT NULL,
    cliente_id BIGINT,
    veiculo_id BIGINT,
    country VARCHAR(10),
    schedule_id BIGINT,
    maintenance_id BIGINT,
    service_order VARCHAR(80),
    service_date DATE NOT NULL,
    service_open_date DATE,
    service_closed_date DATE,
    invoice_date DATE,
    sales_date DATE,
    delivery_date DATE,
    registration_date DATE,
    warranty_start_date DATE,
    service_dept_code VARCHAR(40),
    service_repair_type_code VARCHAR(40),
    service_type VARCHAR(80),
    service_code VARCHAR(40),
    maintenance_number INTEGER,
    dealer_code VARCHAR(40),
    main_source VARCHAR(120),
    agenda_flag BOOLEAN NOT NULL DEFAULT FALSE,
    status_usa VARCHAR(80),
    model_year INTEGER NOT NULL,
    model_name VARCHAR(80) NOT NULL,
    km DECIMAL(12,2),
    origem VARCHAR(120) NOT NULL DEFAULT 'vin_share_Desafio_02.xlsx',
    criado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vin_share_servicos_clientes FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_vin_share_servicos_veiculos FOREIGN KEY (veiculo_id) REFERENCES veiculos (id)
);

CREATE INDEX idx_vin_share_servicos_vin ON vin_share_servicos (vin_hash);
CREATE INDEX idx_vin_share_servicos_vin_data ON vin_share_servicos (vin_hash, service_date);
CREATE INDEX idx_vin_share_servicos_data ON vin_share_servicos (service_date);
CREATE INDEX idx_vin_share_servicos_modelo ON vin_share_servicos (model_name, model_year);

CREATE TABLE vin_share_feature_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    veiculo_id BIGINT,
    cliente_id BIGINT,
    vin_hash VARCHAR(80) NOT NULL,
    data_corte DATE NOT NULL,
    modelo VARCHAR(80) NOT NULL,
    ano_modelo INTEGER NOT NULL,
    qtde_revisoes_ate_corte INTEGER,
    meses_desde_ultimo_servico_ate_corte DECIMAL(8,2),
    meses_relacionamento_ate_corte DECIMAL(8,2),
    n_dealers_usados_ate_corte INTEGER,
    km_max_ate_corte DECIMAL(12,2),
    pct_agenda_ate_corte DECIMAL(5,4),
    intervalo_medio_revisoes_dias_ate_corte DECIMAL(10,2),
    dias_ate_primeira_revisao INTEGER,
    idade_veiculo_meses_ate_corte DECIMAL(8,2),
    payload_predict CLOB NOT NULL,
    payload_hash VARCHAR(64) NOT NULL,
    feature_version VARCHAR(40) NOT NULL DEFAULT 'vin_share_v1',
    imputation_report CLOB,
    criado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_vin_share_feature_snapshots_vin_corte UNIQUE (vin_hash, data_corte),
    CONSTRAINT fk_vin_share_feature_snapshots_veiculos FOREIGN KEY (veiculo_id) REFERENCES veiculos (id),
    CONSTRAINT fk_vin_share_feature_snapshots_clientes FOREIGN KEY (cliente_id) REFERENCES clientes (id)
);

CREATE INDEX idx_feature_snapshots_vin ON vin_share_feature_snapshots (vin_hash);
CREATE INDEX idx_feature_snapshots_corte ON vin_share_feature_snapshots (data_corte);

CREATE TABLE ml_feature_refresh_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vin_hash VARCHAR(80) NOT NULL,
    data_corte DATE NOT NULL,
    snapshot_id BIGINT,
    status VARCHAR(40) NOT NULL DEFAULT 'pending_features',
    motivo VARCHAR(80) NOT NULL DEFAULT 'servico_alterado',
    tentativas INTEGER NOT NULL DEFAULT 0,
    erro CLOB,
    criado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processado_em DATETIME(6),
    CONSTRAINT uk_ml_feature_refresh_queue_vin_corte UNIQUE (vin_hash, data_corte),
    CONSTRAINT fk_ml_feature_refresh_queue_snapshot FOREIGN KEY (snapshot_id) REFERENCES vin_share_feature_snapshots (id),
    CONSTRAINT ck_ml_feature_refresh_queue_status CHECK (status IN (
        'pending_features',
        'features_processing',
        'ready_to_predict',
        'prediction_processing',
        'completed',
        'failed'
    ))
);

CREATE INDEX idx_ml_queue_status_criado ON ml_feature_refresh_queue (status, criado_em);
CREATE INDEX idx_ml_queue_snapshot ON ml_feature_refresh_queue (snapshot_id);

CREATE TABLE predicao_resultados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    queue_id BIGINT,
    snapshot_id BIGINT NOT NULL,
    veiculo_id BIGINT,
    cliente_id BIGINT,
    vin_hash VARCHAR(80) NOT NULL,
    perfil VARCHAR(80),
    risco VARCHAR(30),
    score DECIMAL(8,6),
    motivo_principal CLOB,
    acao_recomendada CLOB,
    canal_recomendado VARCHAR(40),
    modelo_versao VARCHAR(80),
    payload_resposta CLOB,
    criado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    executado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_predicao_resultados_queue UNIQUE (queue_id),
    CONSTRAINT fk_predicao_resultados_queue FOREIGN KEY (queue_id) REFERENCES ml_feature_refresh_queue (id),
    CONSTRAINT fk_predicao_resultados_snapshot FOREIGN KEY (snapshot_id) REFERENCES vin_share_feature_snapshots (id),
    CONSTRAINT fk_predicao_resultados_veiculos FOREIGN KEY (veiculo_id) REFERENCES veiculos (id),
    CONSTRAINT fk_predicao_resultados_clientes FOREIGN KEY (cliente_id) REFERENCES clientes (id)
);

CREATE INDEX idx_predicao_resultados_vin ON predicao_resultados (vin_hash);
CREATE INDEX idx_predicao_resultados_executado ON predicao_resultados (executado_em);

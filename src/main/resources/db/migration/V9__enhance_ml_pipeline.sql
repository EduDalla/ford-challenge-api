ALTER TABLE clientes ADD COLUMN IF NOT EXISTS nome_simulado VARCHAR(120);
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS canal_preferido VARCHAR(40);
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS regiao VARCHAR(80);

ALTER TABLE veiculos ADD COLUMN IF NOT EXISTS sales_date DATE;
ALTER TABLE veiculos ADD COLUMN IF NOT EXISTS delivery_date DATE;

CREATE INDEX IF NOT EXISTS idx_vin_share_servicos_model_name ON vin_share_servicos (model_name);
CREATE INDEX IF NOT EXISTS idx_vin_share_servicos_dealer ON vin_share_servicos (dealer_code);

ALTER TABLE ml_feature_refresh_queue ADD COLUMN IF NOT EXISTS motivo VARCHAR(80) DEFAULT 'servico_alterado' NOT NULL;
ALTER TABLE ml_feature_refresh_queue ADD COLUMN IF NOT EXISTS processado_em DATETIME(6);
ALTER TABLE ml_feature_refresh_queue ADD COLUMN IF NOT EXISTS erro VARCHAR(255);
ALTER TABLE ml_feature_refresh_queue ADD COLUMN IF NOT EXISTS atualizado_em DATETIME(6) DEFAULT CURRENT_TIMESTAMP NOT NULL;

ALTER TABLE ml_feature_refresh_queue DROP CONSTRAINT IF EXISTS ck_ml_feature_refresh_queue_status;

UPDATE ml_feature_refresh_queue
SET status = CASE status
    WHEN 'pending_features' THEN 'pending'
    WHEN 'features_processing' THEN 'processing'
    WHEN 'ready_to_predict' THEN 'done'
    WHEN 'prediction_processing' THEN 'processing'
    WHEN 'completed' THEN 'done'
    ELSE 'failed'
END
WHERE status NOT IN ('pending', 'processing', 'done', 'failed');

ALTER TABLE ml_feature_refresh_queue ADD CONSTRAINT ck_ml_feature_refresh_queue_status
    CHECK (status IN ('pending', 'processing', 'done', 'failed'));

ALTER TABLE vin_share_feature_snapshots ADD COLUMN IF NOT EXISTS status_predicao VARCHAR(30) DEFAULT 'pending' NOT NULL;
ALTER TABLE vin_share_feature_snapshots ADD COLUMN IF NOT EXISTS tentativas_predicao INTEGER DEFAULT 0 NOT NULL;
ALTER TABLE vin_share_feature_snapshots ADD COLUMN IF NOT EXISTS erro_predicao VARCHAR(255);
ALTER TABLE vin_share_feature_snapshots ADD COLUMN IF NOT EXISTS predito_em DATETIME(6);
ALTER TABLE vin_share_feature_snapshots ADD COLUMN IF NOT EXISTS atualizado_em DATETIME(6) DEFAULT CURRENT_TIMESTAMP NOT NULL;

ALTER TABLE vin_share_feature_snapshots DROP CONSTRAINT IF EXISTS ck_vin_share_feature_snapshots_status_predicao;
ALTER TABLE vin_share_feature_snapshots ADD CONSTRAINT ck_vin_share_feature_snapshots_status_predicao
    CHECK (status_predicao IN ('pending', 'processing', 'completed', 'failed', 'retry'));

CREATE INDEX IF NOT EXISTS idx_feature_snapshots_status_predicao
    ON vin_share_feature_snapshots (status_predicao, criado_em);

CREATE INDEX IF NOT EXISTS idx_feature_snapshots_vin_data
    ON vin_share_feature_snapshots (vin_hash, data_corte);

CREATE TABLE IF NOT EXISTS job_execution_logs (
    id UUID PRIMARY KEY,
    job_name VARCHAR(120) NOT NULL,
    status VARCHAR(30) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    duration_ms BIGINT,
    total_processed INTEGER DEFAULT 0 NOT NULL,
    total_success INTEGER DEFAULT 0 NOT NULL,
    total_failed INTEGER DEFAULT 0 NOT NULL,
    error_message VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_job_execution_logs_job_started
    ON job_execution_logs (job_name, started_at);

CREATE INDEX IF NOT EXISTS idx_job_execution_logs_status
    ON job_execution_logs (status, started_at);

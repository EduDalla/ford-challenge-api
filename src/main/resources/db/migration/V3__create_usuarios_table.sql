CREATE SCHEMA IF NOT EXISTS private;

CREATE TABLE profiles (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(150),
    perfil VARCHAR(30) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    CONSTRAINT ck_profiles_perfil CHECK (perfil IN ('ADMIN', 'ANALISTA', 'GESTOR'))
);

CREATE INDEX idx_profiles_email
ON profiles (email);

INSERT INTO profiles (id, nome, email, perfil, ativo, criado_em, atualizado_em)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Consultor Demo',
    'consultor.demo@example.com',
    'ANALISTA',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

CREATE TABLE private.api_clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    client_id VARCHAR(100) NOT NULL,
    client_secret_hash VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    ultimo_uso_em DATETIME(6),
    CONSTRAINT uk_api_clients_client_id UNIQUE (client_id)
);

CREATE TABLE private.permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(80) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME(6) NOT NULL,
    CONSTRAINT uk_permissions_codigo UNIQUE (codigo)
);

CREATE TABLE private.api_client_permissions (
    api_client_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (api_client_id, permission_id),
    CONSTRAINT fk_api_client_permissions_client
        FOREIGN KEY (api_client_id)
        REFERENCES private.api_clients (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_api_client_permissions_permission
        FOREIGN KEY (permission_id)
        REFERENCES private.permissions (id)
);

INSERT INTO private.permissions (codigo, descricao, ativo, criado_em)
VALUES ('ml:predict', 'Permite chamar o endpoint de predicao ML.', TRUE, CURRENT_TIMESTAMP);

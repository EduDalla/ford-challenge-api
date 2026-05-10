CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL,
    telefone VARCHAR(20),
    documento VARCHAR(30) NOT NULL,
    segmento VARCHAR(80),
    nivel_risco VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP(6) NOT NULL,
    atualizado_em TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_clientes_email UNIQUE (email),
    CONSTRAINT uk_clientes_documento UNIQUE (documento)
);

CREATE TABLE interacoes (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    resultado VARCHAR(120) NOT NULL,
    data_interacao TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_interacoes_clientes
        FOREIGN KEY (cliente_id)
        REFERENCES clientes (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_interacoes_cliente_id ON interacoes (cliente_id);

CREATE TABLE informacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    CONSTRAINT uk_informacoes_nome UNIQUE (nome)
);

CREATE TABLE informacoes_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id UUID NOT NULL,
    informacao_id BIGINT NOT NULL,
    data_alerta DATE NOT NULL,
    criado_em DATETIME(6) NOT NULL,
    CONSTRAINT uk_informacoes_user_alerta UNIQUE (user_id, informacao_id, data_alerta),
    CONSTRAINT fk_informacoes_user_profile
        FOREIGN KEY (user_id)
        REFERENCES profiles (id),
    CONSTRAINT fk_informacoes_user_informacao
        FOREIGN KEY (informacao_id)
        REFERENCES informacoes (id)
);

CREATE INDEX idx_info_user_user_id ON informacoes_user (user_id);
CREATE INDEX idx_info_user_data_alerta ON informacoes_user (data_alerta);

INSERT INTO informacoes (nome, descricao, ativo, criado_em, atualizado_em)
SELECT 'Data de nascimento do cliente', 'Lembrete para contato em aniversario e ofertas personalizadas.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM informacoes WHERE nome = 'Data de nascimento do cliente');

INSERT INTO informacoes (nome, descricao, ativo, criado_em, atualizado_em)
SELECT 'Data da ultima revisao', 'Ajuda o consultor a antecipar nova manutencao e evitar inatividade.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM informacoes WHERE nome = 'Data da ultima revisao');

INSERT INTO informacoes (nome, descricao, ativo, criado_em, atualizado_em)
SELECT 'Garantia proxima do vencimento', 'Dispara contato preventivo para conversao de servicos e planos.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM informacoes WHERE nome = 'Garantia proxima do vencimento');

INSERT INTO informacoes (nome, descricao, ativo, criado_em, atualizado_em)
SELECT 'Aniversario de compra do veiculo', 'Momento para reforcar relacionamento e oferecer pacote comemorativo.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM informacoes WHERE nome = 'Aniversario de compra do veiculo');

INSERT INTO informacoes (nome, descricao, ativo, criado_em, atualizado_em)
SELECT 'Preferencia de canal', 'Indica se o cliente responde melhor por WhatsApp, telefone ou email.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM informacoes WHERE nome = 'Preferencia de canal');

INSERT INTO informacoes (nome, descricao, ativo, criado_em, atualizado_em)
SELECT 'Interesse em acessorios', 'Identifica oportunidade de venda consultiva com menor friccao.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM informacoes WHERE nome = 'Interesse em acessorios');

INSERT INTO informacoes (nome, descricao, ativo, criado_em, atualizado_em)
SELECT 'Interesse em upgrade de veiculo', 'Apoia oferta de troca para manter cliente no ecossistema da marca.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM informacoes WHERE nome = 'Interesse em upgrade de veiculo');

INSERT INTO informacoes (nome, descricao, ativo, criado_em, atualizado_em)
SELECT 'Historico de recusas de proposta', 'Permite ajustar argumento comercial com base em objecoes anteriores.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM informacoes WHERE nome = 'Historico de recusas de proposta');

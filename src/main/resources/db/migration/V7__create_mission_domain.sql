CREATE TABLE veiculos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    modelo VARCHAR(80) NOT NULL,
    ano INTEGER NOT NULL,
    vin_simulado VARCHAR(40) NOT NULL,
    km_atual INTEGER NOT NULL,
    ultima_revisao DATE NOT NULL,
    dias_sem_servico INTEGER NOT NULL,
    status_garantia VARCHAR(40) NOT NULL,
    criado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_veiculos_vin_simulado UNIQUE (vin_simulado),
    CONSTRAINT fk_veiculos_clientes
        FOREIGN KEY (cliente_id)
        REFERENCES clientes (id)
);

CREATE INDEX idx_veiculos_cliente_id ON veiculos (cliente_id);

CREATE TABLE missoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_cartao VARCHAR(40) NOT NULL,
    cliente_id BIGINT NOT NULL,
    veiculo_id BIGINT NOT NULL,
    responsavel_id UUID,
    perfil VARCHAR(80) NOT NULL,
    risco VARCHAR(20) NOT NULL,
    score INTEGER NOT NULL,
    prioridade_radar VARCHAR(2) NOT NULL,
    sinais_radar VARCHAR(1200) NOT NULL,
    motivo_principal VARCHAR(500) NOT NULL,
    acao_recomendada VARCHAR(500) NOT NULL,
    mensagem_sugerida VARCHAR(500) NOT NULL,
    valor_potencial DECIMAL(12,2) NOT NULL,
    impacto_vin_share_estimado DECIMAL(5,2) NOT NULL,
    prazo VARCHAR(80) NOT NULL,
    status VARCHAR(30) NOT NULL,
    criado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_missoes_codigo_cartao UNIQUE (codigo_cartao),
    CONSTRAINT fk_missoes_clientes
        FOREIGN KEY (cliente_id)
        REFERENCES clientes (id),
    CONSTRAINT fk_missoes_veiculos
        FOREIGN KEY (veiculo_id)
        REFERENCES veiculos (id),
    CONSTRAINT fk_missoes_responsavel
        FOREIGN KEY (responsavel_id)
        REFERENCES profiles (id),
    CONSTRAINT ck_missoes_risco
        CHECK (risco IN ('BAIXO', 'MEDIO', 'ALTO')),
    CONSTRAINT ck_missoes_prioridade
        CHECK (prioridade_radar IN ('P1', 'P2', 'P3')),
    CONSTRAINT ck_missoes_status
        CHECK (status IN ('EM_RISCO', 'ASSUMIDO', 'CONTATO_FEITO', 'RESPOSTA_RECEBIDA', 'AGENDADO', 'RECUPERADO', 'REPROGRAMAR', 'PERDIDO')),
    CONSTRAINT ck_missoes_score
        CHECK (score BETWEEN 0 AND 100)
);

CREATE INDEX idx_missoes_responsavel_id ON missoes (responsavel_id);
CREATE INDEX idx_missoes_status_prioridade ON missoes (status, prioridade_radar, score);
CREATE INDEX idx_missoes_cliente_id ON missoes (cliente_id);

CREATE TABLE missao_acoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    missao_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    canal VARCHAR(20) NOT NULL,
    observacao VARCHAR(500),
    criado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_missao_acoes_missoes
        FOREIGN KEY (missao_id)
        REFERENCES missoes (id),
    CONSTRAINT ck_missao_acoes_tipo
        CHECK (tipo IN ('EM_RISCO', 'ASSUMIDO', 'CONTATO_FEITO', 'RESPOSTA_RECEBIDA', 'AGENDADO', 'RECUPERADO', 'REPROGRAMAR', 'PERDIDO')),
    CONSTRAINT ck_missao_acoes_canal
        CHECK (canal IN ('WHATSAPP', 'TELEFONE', 'EMAIL'))
);

CREATE INDEX idx_missao_acoes_missao_id ON missao_acoes (missao_id);
CREATE INDEX idx_missao_acoes_criado_em ON missao_acoes (criado_em);

CREATE TABLE missao_resultados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    missao_id BIGINT NOT NULL,
    compareceu BOOLEAN,
    servico_pago BOOLEAN,
    receita_estimada DECIMAL(12,2),
    impacto_vin_share DECIMAL(5,2),
    proximo_passo VARCHAR(255),
    atualizado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_missao_resultados_missao UNIQUE (missao_id),
    CONSTRAINT fk_missao_resultados_missoes
        FOREIGN KEY (missao_id)
        REFERENCES missoes (id)
);

INSERT INTO clientes (nome, email, telefone, documento, segmento, nivel_risco, ativo, criado_em, atualizado_em)
SELECT 'Mariana Oliveira', 'mariana.oliveira.demo@example.com', '11987654321', '90000000001', 'Sao Paulo', 'ALTO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE documento = '90000000001');

INSERT INTO clientes (nome, email, telefone, documento, segmento, nivel_risco, ativo, criado_em, atualizado_em)
SELECT 'Carlos Mendes', 'carlos.mendes.demo@example.com', '11912345678', '90000000002', 'Campinas', 'MEDIO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE documento = '90000000002');

INSERT INTO veiculos (cliente_id, modelo, ano, vin_simulado, km_atual, ultima_revisao, dias_sem_servico, status_garantia)
SELECT c.id, 'Ford Ka', 2020, 'VIN-DEMO-0001', 48200, DATEADD('DAY', -210, CURRENT_DATE), 210, 'Garantia expirada'
FROM clientes c
WHERE c.documento = '90000000001'
  AND NOT EXISTS (SELECT 1 FROM veiculos WHERE vin_simulado = 'VIN-DEMO-0001');

INSERT INTO veiculos (cliente_id, modelo, ano, vin_simulado, km_atual, ultima_revisao, dias_sem_servico, status_garantia)
SELECT c.id, 'Ford EcoSport', 2019, 'VIN-DEMO-0002', 67200, DATEADD('DAY', -145, CURRENT_DATE), 145, 'Garantia expirada'
FROM clientes c
WHERE c.documento = '90000000002'
  AND NOT EXISTS (SELECT 1 FROM veiculos WHERE vin_simulado = 'VIN-DEMO-0002');

INSERT INTO missoes (
    codigo_cartao, cliente_id, veiculo_id, perfil, risco, score, prioridade_radar, sinais_radar,
    motivo_principal, acao_recomendada, mensagem_sugerida, valor_potencial,
    impacto_vin_share_estimado, prazo, status
)
SELECT
    'CARD-001', c.id, v.id, 'Cliente esquecido', 'ALTO', 87, 'P1',
    'Score ML de abandono: 87/100' || CHAR(10) || '142 dias sem revisao recomendada' || CHAR(10) || 'Alta chance de recuperar com contato rapido',
    'Cliente esta ha muitos meses sem retornar para revisao.',
    'Lembrete personalizado e agendamento facilitado.',
    'Ola, percebemos que seu Ford Ka pode estar perto de uma revisao. Posso te ajudar com um horario rapido na rede Ford?',
    980.00, 1.4, 'Hoje', 'EM_RISCO'
FROM clientes c
JOIN veiculos v ON v.cliente_id = c.id
WHERE c.documento = '90000000001'
  AND v.vin_simulado = 'VIN-DEMO-0001'
  AND NOT EXISTS (SELECT 1 FROM missoes WHERE codigo_cartao = 'CARD-001');

INSERT INTO missoes (
    codigo_cartao, cliente_id, veiculo_id, perfil, risco, score, prioridade_radar, sinais_radar,
    motivo_principal, acao_recomendada, mensagem_sugerida, valor_potencial,
    impacto_vin_share_estimado, prazo, status
)
SELECT
    'CARD-002', c.id, v.id, 'Cliente economico', 'MEDIO', 64, 'P2',
    'Score ML de abandono: 64/100' || CHAR(10) || 'Sensibilidade a preco' || CHAR(10) || 'Historico de comparacao antes de agendar',
    'Cliente costuma comparar preco antes de retornar.',
    'Oferecer pacote essencial com preco fechado.',
    'Ola, tenho uma opcao de pacote essencial para seu EcoSport com preco fechado. Posso te mostrar?',
    720.00, 0.8, 'Amanha', 'EM_RISCO'
FROM clientes c
JOIN veiculos v ON v.cliente_id = c.id
WHERE c.documento = '90000000002'
  AND v.vin_simulado = 'VIN-DEMO-0002'
  AND NOT EXISTS (SELECT 1 FROM missoes WHERE codigo_cartao = 'CARD-002');

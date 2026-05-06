INSERT INTO clientes (nome, email, telefone, documento, segmento, nivel_risco, ativo, criado_em, atualizado_em)
VALUES
    ('Ana Martins', 'ana.martins@example.com', '11999990001', '11122233344', 'Varejo', 'ALTO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Bruno Souza', 'bruno.souza@example.com', '11999990002', '22233344455', 'Serviços', 'MEDIO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Carla Pereira', 'carla.pereira@example.com', '11999990003', '33344455566', 'Tecnologia', 'CRITICO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO interacoes (cliente_id, tipo, descricao, resultado, data_interacao)
VALUES
    (1, 'TELEFONE', 'Contato para entender queda de uso do serviço.', 'Cliente solicitou nova proposta.', CURRENT_TIMESTAMP),
    (1, 'EMAIL', 'Envio de materiais com benefícios do plano atual.', 'Aguardando retorno.', CURRENT_TIMESTAMP),
    (3, 'WHATSAPP', 'Mensagem para agendar reunião de retenção.', 'Reunião marcada.', CURRENT_TIMESTAMP);

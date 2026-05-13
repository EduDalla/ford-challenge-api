ALTER TABLE clientes
    ADD COLUMN excluido_em DATETIME(6);

ALTER TABLE interacoes
    ADD COLUMN excluido_em DATETIME(6);

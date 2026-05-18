INSERT INTO private.permissions (codigo, descricao, ativo, criado_em)
SELECT 'clientes:read', 'Permite consultar clientes.', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM private.permissions WHERE codigo = 'clientes:read');

INSERT INTO private.permissions (codigo, descricao, ativo, criado_em)
SELECT 'clientes:write', 'Permite criar e atualizar clientes.', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM private.permissions WHERE codigo = 'clientes:write');

INSERT INTO private.permissions (codigo, descricao, ativo, criado_em)
SELECT 'clientes:delete', 'Permite remover clientes.', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM private.permissions WHERE codigo = 'clientes:delete');

INSERT INTO private.permissions (codigo, descricao, ativo, criado_em)
SELECT 'interacoes:read', 'Permite consultar interacoes.', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM private.permissions WHERE codigo = 'interacoes:read');

INSERT INTO private.permissions (codigo, descricao, ativo, criado_em)
SELECT 'interacoes:write', 'Permite criar interacoes.', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM private.permissions WHERE codigo = 'interacoes:write');

INSERT INTO private.permissions (codigo, descricao, ativo, criado_em)
SELECT 'interacoes:delete', 'Permite remover interacoes.', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM private.permissions WHERE codigo = 'interacoes:delete');

INSERT INTO private.permissions (codigo, descricao, ativo, criado_em)
SELECT 'ml:predict', 'Permite chamar o endpoint de predicao ML.', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM private.permissions WHERE codigo = 'ml:predict');

INSERT INTO private.api_clients (
    nome,
    client_id,
    client_secret_hash,
    ativo,
    criado_em,
    atualizado_em,
    ultimo_uso_em
)
SELECT
    'Python ML Service',
    'python-ml-service',
    '$2a$10$evSMnZM6vCe0pMUrgzXwFOoLB2OSuRdo30Dz95r1ZENU8S1nAKaWO',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL
WHERE NOT EXISTS (SELECT 1 FROM private.api_clients WHERE client_id = 'python-ml-service');

INSERT INTO private.api_client_permissions (api_client_id, permission_id)
SELECT ac.id, p.id
FROM private.api_clients ac
JOIN private.permissions p ON p.codigo IN (
    'clientes:read',
    'clientes:write',
    'clientes:delete',
    'interacoes:read',
    'interacoes:write',
    'interacoes:delete',
    'ml:predict'
)
WHERE ac.client_id = 'python-ml-service'
  AND NOT EXISTS (
      SELECT 1
      FROM private.api_client_permissions ap
      WHERE ap.api_client_id = ac.id
        AND ap.permission_id = p.id
  );

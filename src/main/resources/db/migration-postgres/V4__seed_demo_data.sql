-- V4__seed_demo_data.sql
-- Dados mínimos para demo.
-- Não cria usuário humano, porque usuário agora nasce via Supabase Auth.

insert into public.clientes (
    nome,
    email,
    telefone,
    documento,
    segmento,
    nivel_risco,
    ativo
)
values
    ('Ana Martins', 'ana.martins@example.com', '11999990001', '11122233344', 'Varejo', 'ALTO', true),
    ('Bruno Souza', 'bruno.souza@example.com', '11999990002', '22233344455', 'Serviços', 'MEDIO', true),
    ('Carla Pereira', 'carla.pereira@example.com', '11999990003', '33344455566', 'Tecnologia', 'CRITICO', true);

insert into public.interacoes (
    cliente_id,
    tipo,
    descricao,
    resultado,
    data_interacao
)
values
    (1, 'TELEFONE', 'Contato para entender queda de uso do serviço.', 'Cliente solicitou nova proposta.', current_timestamp),
    (1, 'EMAIL', 'Envio de materiais com benefícios do plano atual.', 'Aguardando retorno.', current_timestamp),
    (3, 'WHATSAPP', 'Mensagem para agendar reunião de retenção.', 'Reunião marcada.', current_timestamp);

insert into public.informacoes (
    nome,
    descricao,
    ativo
)
values
    ('cliente_alto_risco', 'Cliente classificado com alto risco de evasão.', true),
    ('cliente_critico', 'Cliente classificado com risco crítico de evasão.', true),
    ('sem_interacao_recente', 'Cliente sem interação recente registrada.', true),
    ('acao_recomendada', 'Cliente possui ação recomendada pendente.', true);

insert into private.permissions (
    codigo,
    descricao,
    ativo
)
values
    ('ml:predict', 'Permite chamar o endpoint de predição ML.', true),
    ('clientes:read', 'Permite consultar clientes.', true),
    ('clientes:write', 'Permite criar ou atualizar clientes.', true),
    ('interacoes:read', 'Permite consultar interações.', true),
    ('interacoes:write', 'Permite registrar interações.', true);
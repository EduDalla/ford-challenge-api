-- Executada somente pelo Compose local, fora do classpath da aplicacao.
-- O trigger da V1 cria public.profiles com a role de cada identidade.
insert into auth.users (id, email, raw_user_meta_data, raw_app_meta_data) values
('00000000-0000-4000-8000-000000000001', 'admin@local.example', '{"nome":"Admin Local"}', '{"perfil":"ADMIN"}'),
('00000000-0000-4000-8000-000000000002', 'gestor@local.example', '{"nome":"Gestor Local"}', '{"perfil":"GESTOR"}'),
('00000000-0000-4000-8000-000000000003', 'analista@local.example', '{"nome":"Analista Local"}', '{"perfil":"ANALISTA"}');

insert into private.api_clients (nome, client_id, client_secret_hash)
values ('Integracao Local', 'local-ml-client', extensions.crypt('local-ml-secret', extensions.gen_salt('bf', 10)));

insert into private.api_client_permissions (api_client_id, permission_id)
select c.id, p.id from private.api_clients c cross join private.permissions p
where c.client_id = 'local-ml-client' and p.codigo = 'ml:predict';

CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL,
    login VARCHAR(100) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(30) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT uk_usuarios_login UNIQUE (login)
);

CREATE TABLE usuario_autenticacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    tentativas_login INT NOT NULL DEFAULT 0,
    bloqueado_ate DATETIME(6),
    senha_alterada_em DATETIME(6) NOT NULL,
    ultimo_login_em DATETIME(6),
    CONSTRAINT uk_usuario_autenticacao_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_usuario_autenticacao_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id)
        ON DELETE CASCADE
);

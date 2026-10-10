-- Acesso por convite: o usuário é criado sem senha e define a própria senha por um link de uso único.
ALTER TABLE usuarios ALTER COLUMN senha_hash DROP NOT NULL;

CREATE TABLE convites_acesso (
    id          UUID PRIMARY KEY,
    usuario_id  UUID        NOT NULL REFERENCES usuarios (id),
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expira_em   TIMESTAMP WITH TIME ZONE NOT NULL,
    usado_em    TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_convites_usuario ON convites_acesso (usuario_id);

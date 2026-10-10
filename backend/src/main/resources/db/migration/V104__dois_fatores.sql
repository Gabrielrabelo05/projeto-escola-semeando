-- Verificação em duas etapas (RNF-01): segredo TOTP cifrado (AES-256-GCM), último passo aceito (contra reuso)
-- e hashes SHA-256 dos códigos de recuperação, separados por vírgula.
ALTER TABLE usuarios ADD COLUMN dois_fatores_segredo VARCHAR(512);
ALTER TABLE usuarios ADD COLUMN dois_fatores_ativo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE usuarios ADD COLUMN dois_fatores_ultimo_passo BIGINT;
ALTER TABLE usuarios ADD COLUMN codigos_recuperacao VARCHAR(2000);

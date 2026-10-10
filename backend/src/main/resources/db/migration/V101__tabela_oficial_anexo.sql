-- Numerada acima da V100 (vendor/postgresql) porque bancos existentes já aplicaram a V100.
-- Tabela oficial do Anexo didático: a primeira parcela substitui a taxa de matrícula, as cobranças guardam o valor
-- com desconto de pontualidade, os alunos podem ter atividades extracurriculares e o checklist de documentos passa
-- a ser o da escola.

-- 1. Primeira parcela (sinal do contrato) no lugar da taxa de matrícula.
ALTER TABLE matriculas RENAME COLUMN cobranca_taxa_id TO primeira_parcela_id;

UPDATE cobrancas
SET tipo = COALESCE((SELECT CASE WHEN m.nivel = 'EDUCACAO_INFANTIL' THEN 'TAXA_ASSOCIADO' ELSE 'MENSALIDADE' END
                     FROM matriculas m
                     WHERE m.primeira_parcela_id = cobrancas.id), 'MENSALIDADE')
WHERE tipo = 'TAXA_MATRICULA';

-- 2. Desconto de 10% para pagamento até o vencimento (nulo = sem desconto).
ALTER TABLE cobrancas ADD COLUMN valor_com_desconto NUMERIC(12, 2);

-- 3. Jiu Jitsu / Ballet.
CREATE TABLE aluno_atividades (
    aluno_id   UUID        NOT NULL REFERENCES alunos (id),
    atividade  VARCHAR(20) NOT NULL,
    PRIMARY KEY (aluno_id, atividade)
);

-- 4. Checklist de documentos do Anexo didático.
UPDATE matricula_documentos SET tipo = 'DOCUMENTO_ALUNO' WHERE tipo = 'CERTIDAO_NASCIMENTO';
UPDATE matricula_documentos SET tipo = 'CARTEIRA_VACINACAO' WHERE tipo = 'CARTAO_VACINACAO';
UPDATE matricula_documentos SET tipo = 'FOTOS_3X4' WHERE tipo = 'FOTO_3X4';
DELETE FROM matricula_documentos
WHERE tipo IN ('CARTAO_SUS', 'LAUDO_MEDICO', 'DECLARACAO_TRANSFERENCIA', 'HISTORICO_ESCOLAR');

INSERT INTO matricula_documentos (matricula_id, ordem, tipo)
SELECT m.id, COALESCE((SELECT MAX(d.ordem) FROM matricula_documentos d WHERE d.matricula_id = m.id), -1) + 1,
       'DOCUMENTO_AUTORIZADOS'
FROM matriculas m;

-- A coluna "ordem" é o índice da lista (@OrderColumn) e não pode ter buracos.
CREATE TABLE matricula_documentos_reordenados AS
SELECT matricula_id,
       CAST(ROW_NUMBER() OVER (PARTITION BY matricula_id ORDER BY ordem) - 1 AS INTEGER) AS ordem,
       tipo, arquivo, entregue_em
FROM matricula_documentos;

DELETE FROM matricula_documentos;

INSERT INTO matricula_documentos (matricula_id, ordem, tipo, arquivo, entregue_em)
SELECT matricula_id, ordem, tipo, arquivo, entregue_em FROM matricula_documentos_reordenados;

DROP TABLE matricula_documentos_reordenados;

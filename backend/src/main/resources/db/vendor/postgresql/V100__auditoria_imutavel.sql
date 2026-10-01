-- RNF-06: o log de auditoria é imutável. Qualquer UPDATE ou DELETE é rejeitado pelo banco.
CREATE OR REPLACE FUNCTION bloquear_alteracao_auditoria() RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Registros de auditoria são imutáveis (RNF-06)';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_auditoria_imutavel
    BEFORE UPDATE OR DELETE ON auditoria
    FOR EACH ROW EXECUTE FUNCTION bloquear_alteracao_auditoria();

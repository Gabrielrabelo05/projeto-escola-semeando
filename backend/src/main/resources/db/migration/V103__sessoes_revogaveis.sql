-- Sessões revogáveis: tokens emitidos antes deste instante são recusados (inativação, troca de senha).
ALTER TABLE usuarios ADD COLUMN sessoes_validas_desde TIMESTAMP WITH TIME ZONE;

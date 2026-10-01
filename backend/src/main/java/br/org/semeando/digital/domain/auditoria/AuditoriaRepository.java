package br.org.semeando.digital.domain.auditoria;

import java.util.List;

/** Somente inserção e leitura: o log de auditoria é imutável (RNF-06). */
public interface AuditoriaRepository {

    void registrar(RegistroAuditoria registro);

    List<RegistroAuditoria> consultar(String entidade, String entidadeId, int limite);
}

package br.org.semeando.digital.domain.auditoria;

import java.time.Instant;
import java.util.UUID;

/** RF-36 / RNF-06: registro imutável de toda operação sobre dados pessoais. */
public record RegistroAuditoria(UUID id, String usuario, AcaoAuditada acao, String entidade, String entidadeId,
        String detalhes, Instant ocorridoEm) {

    public static RegistroAuditoria de(String usuario, AcaoAuditada acao, String entidade, Object entidadeId,
            String detalhes, Instant agora) {
        return new RegistroAuditoria(UUID.randomUUID(), usuario, acao, entidade, String.valueOf(entidadeId), detalhes,
                agora);
    }
}

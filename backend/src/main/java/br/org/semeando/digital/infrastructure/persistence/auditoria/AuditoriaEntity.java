package br.org.semeando.digital.infrastructure.persistence.auditoria;

import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.auditoria.RegistroAuditoria;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "auditoria")
class AuditoriaEntity {

    @Id
    private UUID id;
    private String usuario;
    @Enumerated(EnumType.STRING)
    private AcaoAuditada acao;
    private String entidade;
    private String entidadeId;
    private String detalhes;
    private Instant ocorridoEm;

    protected AuditoriaEntity() {
    }

    static AuditoriaEntity de(RegistroAuditoria r) {
        AuditoriaEntity e = new AuditoriaEntity();
        e.id = r.id();
        e.usuario = r.usuario();
        e.acao = r.acao();
        e.entidade = r.entidade();
        e.entidadeId = r.entidadeId();
        e.detalhes = r.detalhes();
        e.ocorridoEm = r.ocorridoEm();
        return e;
    }

    RegistroAuditoria paraDominio() {
        return new RegistroAuditoria(id, usuario, acao, entidade, entidadeId, detalhes, ocorridoEm);
    }
}

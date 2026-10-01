package br.org.semeando.digital.application.auditoria;

import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.auditoria.AuditoriaRepository;
import br.org.semeando.digital.domain.auditoria.RegistroAuditoria;
import java.time.Clock;

/** RF-36: registra quem fez o quê, quando e sobre qual registro. */
public class Auditor {

    private final AuditoriaRepository repository;
    private final Clock clock;

    public Auditor(AuditoriaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public void registrar(Ator ator, AcaoAuditada acao, String entidade, Object id, String detalhes) {
        repository.registrar(RegistroAuditoria.de(ator.nome(), acao, entidade, id, detalhes, clock.instant()));
    }
}

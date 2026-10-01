package br.org.semeando.digital.application.auditoria;

import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.auditoria.AuditoriaRepository;
import br.org.semeando.digital.domain.auditoria.RegistroAuditoria;
import java.util.List;

@CasoDeUso
public class ConsultarAuditoriaUseCase {

    private static final int LIMITE_PADRAO = 200;

    private final AuditoriaRepository repository;

    public ConsultarAuditoriaUseCase(AuditoriaRepository repository) {
        this.repository = repository;
    }

    public List<RegistroAuditoria> executar(String entidade, String entidadeId) {
        return repository.consultar(entidade, entidadeId, LIMITE_PADRAO);
    }
}

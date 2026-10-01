package br.org.semeando.digital.infrastructure.persistence.auditoria;

import br.org.semeando.digital.domain.auditoria.AuditoriaRepository;
import br.org.semeando.digital.domain.auditoria.RegistroAuditoria;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
class AuditoriaRepositoryAdapter implements AuditoriaRepository {

    private final AuditoriaJpaRepository jpa;

    AuditoriaRepositoryAdapter(AuditoriaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void registrar(RegistroAuditoria registro) {
        jpa.save(AuditoriaEntity.de(registro));
    }

    @Override
    public List<RegistroAuditoria> consultar(String entidade, String entidadeId, int limite) {
        return jpa.consultar(entidade, entidadeId, PageRequest.of(0, limite)).stream()
                .map(AuditoriaEntity::paraDominio).toList();
    }
}

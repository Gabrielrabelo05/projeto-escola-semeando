package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import br.org.semeando.digital.domain.pedagogico.PlanoAula;
import br.org.semeando.digital.domain.pedagogico.PlanoAulaRepository;
import br.org.semeando.digital.domain.pedagogico.StatusPlanoAula;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class PlanoAulaRepositoryAdapter implements PlanoAulaRepository {

    private final PlanoAulaJpaRepository jpa;

    PlanoAulaRepositoryAdapter(PlanoAulaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public PlanoAula salvar(PlanoAula plano) {
        return jpa.save(PlanoAulaEntity.de(plano)).paraDominio();
    }

    @Override
    public Optional<PlanoAula> buscarPorId(UUID id) {
        return jpa.findById(id).map(PlanoAulaEntity::paraDominio);
    }

    @Override
    public List<PlanoAula> listar(UUID professorId, UUID turmaId, StatusPlanoAula status) {
        return jpa.filtrar(professorId, turmaId, status).stream().map(PlanoAulaEntity::paraDominio).toList();
    }
}

package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import br.org.semeando.digital.domain.pedagogico.PlanejamentoDiario;
import br.org.semeando.digital.domain.pedagogico.PlanejamentoDiarioRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class PlanejamentoDiarioRepositoryAdapter implements PlanejamentoDiarioRepository {

    private final PlanejamentoDiarioJpaRepository jpa;

    PlanejamentoDiarioRepositoryAdapter(PlanejamentoDiarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public PlanejamentoDiario salvar(PlanejamentoDiario planejamento) {
        return jpa.save(PlanejamentoDiarioEntity.de(planejamento)).paraDominio();
    }

    @Override
    public Optional<PlanejamentoDiario> buscarPorId(UUID id) {
        return jpa.findById(id).map(PlanejamentoDiarioEntity::paraDominio);
    }

    @Override
    public List<PlanejamentoDiario> listar(UUID professorId, UUID turmaId) {
        return jpa.filtrar(professorId, turmaId).stream().map(PlanejamentoDiarioEntity::paraDominio).toList();
    }
}

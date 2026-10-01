package br.org.semeando.digital.domain.pedagogico;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanejamentoDiarioRepository {

    PlanejamentoDiario salvar(PlanejamentoDiario planejamento);

    Optional<PlanejamentoDiario> buscarPorId(UUID id);

    List<PlanejamentoDiario> listar(UUID professorId, UUID turmaId);
}

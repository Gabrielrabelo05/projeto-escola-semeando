package br.org.semeando.digital.domain.pedagogico;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanoAulaRepository {

    PlanoAula salvar(PlanoAula plano);

    Optional<PlanoAula> buscarPorId(UUID id);

    List<PlanoAula> listar(UUID professorId, UUID turmaId, StatusPlanoAula status);
}

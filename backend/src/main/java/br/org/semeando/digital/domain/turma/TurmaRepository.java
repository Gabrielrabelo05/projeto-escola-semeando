package br.org.semeando.digital.domain.turma;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TurmaRepository {

    Turma salvar(Turma turma);

    Optional<Turma> buscarPorId(UUID id);

    List<Turma> listarPorAnoLetivo(int anoLetivo);

    List<Turma> listarTodas();
}

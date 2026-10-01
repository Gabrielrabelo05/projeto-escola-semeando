package br.org.semeando.digital.domain.pedagogico;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiarioClasseRepository {

    DiarioClasse salvar(DiarioClasse diario);

    Optional<DiarioClasse> buscarPorTurmaEData(UUID turmaId, LocalDate data);

    List<DiarioClasse> listarPorTurma(UUID turmaId, LocalDate inicio, LocalDate fim);

    List<DiarioClasse> listarPorPeriodo(LocalDate inicio, LocalDate fim);
}

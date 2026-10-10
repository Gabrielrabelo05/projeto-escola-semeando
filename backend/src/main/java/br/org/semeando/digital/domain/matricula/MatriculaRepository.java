package br.org.semeando.digital.domain.matricula;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatriculaRepository {

    Matricula salvar(Matricula matricula);

    Optional<Matricula> buscarPorId(UUID id);

    Optional<Matricula> buscarPorPrimeiraParcela(UUID cobrancaId);

    List<Matricula> listarPorAluno(UUID alunoId);

    /** Filtros opcionais (nulos são ignorados). */
    List<Matricula> listar(StatusMatricula status, Integer anoLetivo);

    long contarOcupandoVaga(UUID turmaId);

    List<UUID> alunosAtivosDaTurma(UUID turmaId);
}

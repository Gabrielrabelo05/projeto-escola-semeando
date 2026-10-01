package br.org.semeando.digital.domain.aluno;

import br.org.semeando.digital.domain.shared.Cpf;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlunoRepository {

    Aluno salvar(Aluno aluno);

    Optional<Aluno> buscarPorId(UUID id);

    List<Aluno> listar(StatusAluno status);

    List<Aluno> listarPorCpfResponsavel(Cpf cpf);

    List<Aluno> buscarPorTermo(String termo);
}

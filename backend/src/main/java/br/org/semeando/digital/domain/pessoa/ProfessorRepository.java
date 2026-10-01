package br.org.semeando.digital.domain.pessoa;

import br.org.semeando.digital.domain.shared.Cpf;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessorRepository {

    Professor salvar(Professor entidade);

    Optional<Professor> buscarPorId(UUID id);

    Optional<Professor> buscarPorCpf(Cpf cpf);

    List<Professor> listar(boolean incluirInativos);

    List<Professor> buscarPorTermo(String termo);
}

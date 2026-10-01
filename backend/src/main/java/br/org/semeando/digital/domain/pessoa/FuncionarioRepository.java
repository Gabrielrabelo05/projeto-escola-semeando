package br.org.semeando.digital.domain.pessoa;

import br.org.semeando.digital.domain.shared.Cpf;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuncionarioRepository {

    Funcionario salvar(Funcionario entidade);

    Optional<Funcionario> buscarPorId(UUID id);

    Optional<Funcionario> buscarPorCpf(Cpf cpf);

    List<Funcionario> listar(boolean incluirInativos);

    List<Funcionario> buscarPorTermo(String termo);
}

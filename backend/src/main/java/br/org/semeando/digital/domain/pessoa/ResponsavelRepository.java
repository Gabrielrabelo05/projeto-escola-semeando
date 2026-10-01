package br.org.semeando.digital.domain.pessoa;

import br.org.semeando.digital.domain.shared.Cpf;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResponsavelRepository {

    Responsavel salvar(Responsavel entidade);

    Optional<Responsavel> buscarPorId(UUID id);

    Optional<Responsavel> buscarPorCpf(Cpf cpf);

    List<Responsavel> listar(boolean incluirInativos);

    List<Responsavel> buscarPorTermo(String termo);
}

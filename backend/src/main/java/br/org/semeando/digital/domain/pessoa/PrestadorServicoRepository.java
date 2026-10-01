package br.org.semeando.digital.domain.pessoa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrestadorServicoRepository {

    PrestadorServico salvar(PrestadorServico prestador);

    Optional<PrestadorServico> buscarPorId(UUID id);

    List<PrestadorServico> listar(boolean incluirInativos);
}

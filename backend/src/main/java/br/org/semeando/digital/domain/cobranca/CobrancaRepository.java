package br.org.semeando.digital.domain.cobranca;

import br.org.semeando.digital.domain.shared.MesReferencia;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CobrancaRepository {

    Cobranca salvar(Cobranca cobranca);

    Optional<Cobranca> buscarPorId(UUID id);

    Optional<Cobranca> buscarPorIdCora(String idCora);

    boolean existe(UUID alunoId, TipoCobranca tipo, MesReferencia mes);

    List<Cobranca> listarPorMes(MesReferencia mes);

    List<Cobranca> listarPorAno(int ano);

    List<Cobranca> listarPorAluno(UUID alunoId);

    List<Cobranca> listarEmAberto();
}

package br.org.semeando.digital.domain.obrigacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ObrigacaoRepository {

    ObrigacaoRegulatoria salvar(ObrigacaoRegulatoria obrigacao);

    Optional<ObrigacaoRegulatoria> buscarPorId(UUID id);

    boolean existe(TipoObrigacao tipo, int ano, Integer bimestre);

    List<ObrigacaoRegulatoria> listar(Integer ano, StatusObrigacao status);

    List<ObrigacaoRegulatoria> listarAbertas();
}

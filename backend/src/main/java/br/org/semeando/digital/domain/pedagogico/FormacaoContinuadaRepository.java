package br.org.semeando.digital.domain.pedagogico;

import java.util.List;
import java.util.UUID;

public interface FormacaoContinuadaRepository {

    FormacaoContinuada salvar(FormacaoContinuada formacao);

    List<FormacaoContinuada> listar(UUID professorId);
}

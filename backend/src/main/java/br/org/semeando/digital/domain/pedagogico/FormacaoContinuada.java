package br.org.semeando.digital.domain.pedagogico;

import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.LocalDate;
import java.util.UUID;

/** RF-43: capacitações realizadas pelos professores. */
public record FormacaoContinuada(UUID id, UUID professorId, String tema, LocalDate data, int cargaHoraria,
        String instituicao) {

    public FormacaoContinuada {
        Validacoes.obrigatorio(id, "id");
        Validacoes.obrigatorio(professorId, "professor");
        Validacoes.obrigatorio(tema, "tema");
        Validacoes.obrigatorio(data, "data");
        Validacoes.positivo(cargaHoraria, "carga horária");
        Validacoes.obrigatorio(instituicao, "instituição");
    }

    public static FormacaoContinuada nova(UUID professorId, String tema, LocalDate data, int cargaHoraria,
            String instituicao) {
        return new FormacaoContinuada(UUID.randomUUID(), professorId, tema, data, cargaHoraria, instituicao);
    }
}

package br.org.semeando.digital.domain.turma;

import static br.org.semeando.digital.domain.turma.NivelEnsino.EDUCACAO_INFANTIL;
import static br.org.semeando.digital.domain.turma.NivelEnsino.ENSINO_FUNDAMENTAL;

/** Séries atendidas pela escola: Maternal I ao 5º ano. */
public enum Serie {
    MATERNAL_I("Maternal I", EDUCACAO_INFANTIL),
    MATERNAL_II("Maternal II", EDUCACAO_INFANTIL),
    INFANTIL_1("Infantil 1", EDUCACAO_INFANTIL),
    INFANTIL_2("Infantil 2", EDUCACAO_INFANTIL),
    PRIMEIRO_ANO("1º ano", ENSINO_FUNDAMENTAL),
    SEGUNDO_ANO("2º ano", ENSINO_FUNDAMENTAL),
    TERCEIRO_ANO("3º ano", ENSINO_FUNDAMENTAL),
    QUARTO_ANO("4º ano", ENSINO_FUNDAMENTAL),
    QUINTO_ANO("5º ano", ENSINO_FUNDAMENTAL);

    private final String descricao;
    private final NivelEnsino nivel;

    Serie(String descricao, NivelEnsino nivel) {
        this.descricao = descricao;
        this.nivel = nivel;
    }

    public String descricao() {
        return descricao;
    }

    public NivelEnsino nivel() {
        return nivel;
    }
}

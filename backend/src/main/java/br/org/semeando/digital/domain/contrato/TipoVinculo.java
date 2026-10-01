package br.org.semeando.digital.domain.contrato;

import br.org.semeando.digital.domain.turma.NivelEnsino;

/** RN-01: cada nível de ensino possui exatamente um tipo de vínculo, e eles nunca podem ser trocados. */
public enum TipoVinculo {
    FICHA_SOCIO(NivelEnsino.EDUCACAO_INFANTIL),
    CONTRATO_PRESTACAO_SERVICOS(NivelEnsino.ENSINO_FUNDAMENTAL);

    private final NivelEnsino nivel;

    TipoVinculo(NivelEnsino nivel) {
        this.nivel = nivel;
    }

    public static TipoVinculo para(NivelEnsino nivel) {
        return nivel == NivelEnsino.EDUCACAO_INFANTIL ? FICHA_SOCIO : CONTRATO_PRESTACAO_SERVICOS;
    }

    public NivelEnsino nivel() {
        return nivel;
    }
}

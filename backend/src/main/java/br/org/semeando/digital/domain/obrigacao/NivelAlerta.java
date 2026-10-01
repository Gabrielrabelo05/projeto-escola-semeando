package br.org.semeando.digital.domain.obrigacao;

/** RN-10: alertas com 30 e 15 dias de antecedência, além do aviso de prazo vencido. */
public enum NivelAlerta {
    TRINTA_DIAS(30),
    QUINZE_DIAS(15),
    VENCIDA(0);

    private final int dias;

    NivelAlerta(int dias) {
        this.dias = dias;
    }

    public int dias() {
        return dias;
    }
}

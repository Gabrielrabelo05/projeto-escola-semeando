package br.org.semeando.digital.domain.cobranca;

import br.org.semeando.digital.domain.shared.Validacoes;

/** Dados do boleto emitido pela plataforma Cora. */
public record Boleto(String idCora, String linhaDigitavel, String url) {

    public Boleto {
        Validacoes.obrigatorio(idCora, "id do boleto no Cora");
    }
}

package br.org.semeando.digital.domain.matricula;

import br.org.semeando.digital.domain.shared.Validacoes;

/** RF-04: tamanhos de farda vinculados à matrícula. */
public record Farda(String blusa, String calca, String tenis) {

    public Farda {
        Validacoes.obrigatorio(blusa, "tamanho da blusa");
        Validacoes.obrigatorio(calca, "tamanho da calça");
        Validacoes.obrigatorio(tenis, "tamanho do tênis");
    }
}

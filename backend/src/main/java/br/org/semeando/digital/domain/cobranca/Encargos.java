package br.org.semeando.digital.domain.cobranca;

import br.org.semeando.digital.domain.shared.Dinheiro;

public record Encargos(long diasAtraso, Dinheiro multa, Dinheiro juros, Dinheiro correcao) {

    public static Encargos nenhum() {
        return new Encargos(0, Dinheiro.ZERO, Dinheiro.ZERO, Dinheiro.ZERO);
    }

    public Dinheiro total() {
        return multa.somar(juros).somar(correcao);
    }
}

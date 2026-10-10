package br.org.semeando.digital.domain.contrato;

import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.Validacoes;

/**
 * Valor de uma parcela na tabela oficial: integral e com o desconto de pontualidade (pago até o vencimento).
 * Itens sem desconto (taxa de associado, atividades extracurriculares) têm os dois valores iguais.
 */
public record Mensalidade(Dinheiro integral, Dinheiro comDesconto) {

    public Mensalidade {
        Validacoes.obrigatorio(integral, "valor integral");
        Validacoes.obrigatorio(comDesconto, "valor com desconto");
        if (comDesconto.maiorQue(integral)) {
            throw new DomainException("CONFIG", "O valor com desconto não pode superar o valor integral");
        }
    }

    public static Mensalidade semDesconto(Dinheiro valor) {
        return new Mensalidade(valor, valor);
    }

    public static Mensalidade de(String integral, String comDesconto) {
        return new Mensalidade(Dinheiro.de(integral), Dinheiro.de(comDesconto));
    }

    public boolean possuiDesconto() {
        return !integral.equals(comDesconto);
    }

    public Dinheiro desconto() {
        return Dinheiro.de(integral.valor().subtract(comDesconto.valor()));
    }
}

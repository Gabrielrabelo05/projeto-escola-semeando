package br.org.semeando.digital.domain.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Valor monetário em reais, sempre com duas casas decimais. */
public record Dinheiro(BigDecimal valor) implements Comparable<Dinheiro> {

    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    public Dinheiro {
        Validacoes.obrigatorio(valor, "valor");
        if (valor.signum() < 0) {
            throw new DomainException("VALOR_INVALIDO", "Valor monetário não pode ser negativo");
        }
        valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static Dinheiro de(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public static Dinheiro de(BigDecimal valor) {
        return new Dinheiro(valor);
    }

    public Dinheiro somar(Dinheiro outro) {
        return new Dinheiro(valor.add(outro.valor));
    }

    public Dinheiro multiplicar(BigDecimal fator) {
        return new Dinheiro(valor.multiply(fator));
    }

    public Dinheiro multiplicar(long fator) {
        return multiplicar(BigDecimal.valueOf(fator));
    }

    public boolean maiorQue(Dinheiro outro) {
        return compareTo(outro) > 0;
    }

    @Override
    public int compareTo(Dinheiro outro) {
        return valor.compareTo(outro.valor);
    }

    @Override
    public String toString() {
        return "R$ " + valor.toPlainString().replace('.', ',');
    }
}

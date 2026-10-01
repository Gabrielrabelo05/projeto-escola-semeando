package br.org.semeando.digital.domain.shared;

import java.time.LocalDate;
import java.time.YearMonth;

/** Mês de competência de uma cobrança (ex.: 2026-03). */
public record MesReferencia(YearMonth valor) implements Comparable<MesReferencia> {

    public MesReferencia {
        Validacoes.obrigatorio(valor, "mês de referência");
    }

    public static MesReferencia de(String texto) {
        return new MesReferencia(YearMonth.parse(texto));
    }

    public static MesReferencia de(int ano, int mes) {
        return new MesReferencia(YearMonth.of(ano, mes));
    }

    public LocalDate dia(int dia) {
        return valor.atDay(Math.min(dia, valor.lengthOfMonth()));
    }

    @Override
    public int compareTo(MesReferencia outro) {
        return valor.compareTo(outro.valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}

package br.org.semeando.digital.domain.cobranca;

import java.math.BigDecimal;
import java.time.YearMonth;

/** Índice de inflação usado na correção do principal em atrasos de 30 dias ou mais (INPC/IBGE). */
public interface IndiceCorrecaoMonetaria {

    IndiceCorrecaoMonetaria NENHUM = (inicio, fim) -> BigDecimal.ZERO;

    /** Variação acumulada (fração, ex.: 0.0123 = 1,23%) dos meses de {@code inicio} a {@code fim}, inclusive. */
    BigDecimal variacaoAcumulada(YearMonth inicio, YearMonth fim);
}

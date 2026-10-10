package br.org.semeando.digital.domain.cobranca;

import br.org.semeando.digital.domain.shared.Dinheiro;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

/**
 * RN-03 / RF-25 e cláusula de encargos do contrato: multa de 2% e juros de 0,033% ao dia sobre a parcela após
 * o vencimento. Não há acréscimos se o pagamento ocorrer até o 5º dia útil do mês do vencimento, e com atraso de
 * 30 dias ou mais o principal é corrigido pelo INPC acumulado nos meses cheios em atraso.
 */
public final class CalculadoraEncargos {

    public static final BigDecimal PERCENTUAL_MULTA = new BigDecimal("0.02");
    public static final BigDecimal PERCENTUAL_JUROS_DIA = new BigDecimal("0.00033");
    public static final int DIA_UTIL_CARENCIA = 5;
    public static final int DIAS_PARA_CORRECAO = 30;

    private CalculadoraEncargos() {
    }

    public static Encargos calcular(Dinheiro valor, LocalDate vencimento, LocalDate referencia,
            IndiceCorrecaoMonetaria indice) {
        if (!referencia.isAfter(limiteSemAcrescimos(vencimento))) {
            return Encargos.nenhum();
        }
        long dias = ChronoUnit.DAYS.between(vencimento, referencia);
        Dinheiro multa = valor.multiplicar(PERCENTUAL_MULTA);
        Dinheiro juros = valor.multiplicar(PERCENTUAL_JUROS_DIA.multiply(BigDecimal.valueOf(dias)));
        return new Encargos(dias, multa, juros, correcao(valor, vencimento, referencia, dias, indice));
    }

    /** O que vier por último: o vencimento ou o 5º dia útil do mês do vencimento. */
    public static LocalDate limiteSemAcrescimos(LocalDate vencimento) {
        LocalDate carencia = DiasUteis.enesimoDiaUtil(YearMonth.from(vencimento), DIA_UTIL_CARENCIA);
        return carencia.isAfter(vencimento) ? carencia : vencimento;
    }

    private static Dinheiro correcao(Dinheiro valor, LocalDate vencimento, LocalDate referencia, long dias,
            IndiceCorrecaoMonetaria indice) {
        if (dias < DIAS_PARA_CORRECAO) {
            return Dinheiro.ZERO;
        }
        YearMonth primeiro = YearMonth.from(vencimento).plusMonths(1);
        YearMonth ultimo = YearMonth.from(referencia).minusMonths(1);
        if (primeiro.isAfter(ultimo)) {
            return Dinheiro.ZERO;
        }
        BigDecimal variacao = indice.variacaoAcumulada(primeiro, ultimo);
        return variacao.signum() > 0 ? valor.multiplicar(variacao) : Dinheiro.ZERO;
    }
}

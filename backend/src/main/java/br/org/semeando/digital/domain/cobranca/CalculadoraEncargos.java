package br.org.semeando.digital.domain.cobranca;

import br.org.semeando.digital.domain.shared.Dinheiro;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** RN-03 / RF-25: multa de 2% e juros de 0,033% ao dia sobre o valor da parcela após o vencimento. */
public final class CalculadoraEncargos {

    public static final BigDecimal PERCENTUAL_MULTA = new BigDecimal("0.02");
    public static final BigDecimal PERCENTUAL_JUROS_DIA = new BigDecimal("0.00033");

    private CalculadoraEncargos() {
    }

    public static Encargos calcular(Dinheiro valor, LocalDate vencimento, LocalDate referencia) {
        long dias = ChronoUnit.DAYS.between(vencimento, referencia);
        if (dias <= 0) {
            return Encargos.nenhum();
        }
        Dinheiro multa = valor.multiplicar(PERCENTUAL_MULTA);
        Dinheiro juros = valor.multiplicar(PERCENTUAL_JUROS_DIA.multiply(BigDecimal.valueOf(dias)));
        return new Encargos(dias, multa, juros);
    }
}

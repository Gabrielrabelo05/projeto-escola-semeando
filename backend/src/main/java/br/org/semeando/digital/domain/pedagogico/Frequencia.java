package br.org.semeando.digital.domain.pedagogico;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/** RF-44: percentual de presença do aluno, com alerta abaixo de 75%. */
public record Frequencia(UUID alunoId, int aulas, int presencas) {

    public static final BigDecimal MINIMO = new BigDecimal("75.00");

    public BigDecimal percentual() {
        if (aulas == 0) {
            return new BigDecimal("100.00");
        }
        return BigDecimal.valueOf(presencas * 100L).divide(BigDecimal.valueOf(aulas), 2, RoundingMode.HALF_UP);
    }

    public int faltas() {
        return aulas - presencas;
    }

    public boolean emAlerta() {
        return percentual().compareTo(MINIMO) < 0;
    }
}

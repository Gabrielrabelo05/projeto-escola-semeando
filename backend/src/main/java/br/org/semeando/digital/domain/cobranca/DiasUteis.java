package br.org.semeando.digital.domain.cobranca;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

/** Dias úteis considerados de segunda a sexta (feriados não são descontados). */
public final class DiasUteis {

    private DiasUteis() {
    }

    public static LocalDate enesimoDiaUtil(YearMonth mes, int n) {
        LocalDate dia = mes.atDay(1);
        int contados = 0;
        while (true) {
            if (dia.getDayOfWeek() != DayOfWeek.SATURDAY && dia.getDayOfWeek() != DayOfWeek.SUNDAY
                    && ++contados == n) {
                return dia;
            }
            dia = dia.plusDays(1);
        }
    }
}

package br.org.semeando.digital.domain.pedagogico;

import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.LocalDate;
import java.util.UUID;

/** RF-42: evento do calendário letivo, visível a todos os perfis. */
public record EventoCalendario(UUID id, String titulo, TipoEvento tipo, LocalDate inicio, LocalDate fim) {

    public EventoCalendario {
        Validacoes.obrigatorio(id, "id");
        Validacoes.obrigatorio(titulo, "título");
        Validacoes.obrigatorio(tipo, "tipo");
        Validacoes.obrigatorio(inicio, "data de início");
        fim = fim == null ? inicio : fim;
        if (fim.isBefore(inicio)) {
            throw new DomainException("DATA_INVALIDA", "Fim do evento deve ser após o início");
        }
    }

    public static EventoCalendario novo(String titulo, TipoEvento tipo, LocalDate inicio, LocalDate fim) {
        return new EventoCalendario(UUID.randomUUID(), titulo, tipo, inicio, fim);
    }
}

package br.org.semeando.digital.domain.pedagogico;

import java.util.List;
import java.util.UUID;

public interface CalendarioRepository {

    EventoCalendario salvar(EventoCalendario evento);

    void remover(UUID id);

    List<EventoCalendario> listarPorAno(int ano);
}

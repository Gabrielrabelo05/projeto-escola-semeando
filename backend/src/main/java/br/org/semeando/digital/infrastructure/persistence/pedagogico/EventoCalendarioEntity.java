package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import br.org.semeando.digital.domain.pedagogico.EventoCalendario;
import br.org.semeando.digital.domain.pedagogico.TipoEvento;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "eventos_calendario")
class EventoCalendarioEntity {

    @Id
    private UUID id;
    private String titulo;
    @Enumerated(EnumType.STRING)
    private TipoEvento tipo;
    private LocalDate inicio;
    private LocalDate fim;

    protected EventoCalendarioEntity() {
    }

    static EventoCalendarioEntity de(EventoCalendario e) {
        EventoCalendarioEntity entity = new EventoCalendarioEntity();
        entity.id = e.id();
        entity.titulo = e.titulo();
        entity.tipo = e.tipo();
        entity.inicio = e.inicio();
        entity.fim = e.fim();
        return entity;
    }

    EventoCalendario paraDominio() {
        return new EventoCalendario(id, titulo, tipo, inicio, fim);
    }
}

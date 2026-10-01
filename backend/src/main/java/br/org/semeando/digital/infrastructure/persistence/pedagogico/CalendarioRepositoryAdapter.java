package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import br.org.semeando.digital.domain.pedagogico.CalendarioRepository;
import br.org.semeando.digital.domain.pedagogico.EventoCalendario;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class CalendarioRepositoryAdapter implements CalendarioRepository {

    private final EventoCalendarioJpaRepository jpa;

    CalendarioRepositoryAdapter(EventoCalendarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public EventoCalendario salvar(EventoCalendario evento) {
        return jpa.save(EventoCalendarioEntity.de(evento)).paraDominio();
    }

    @Override
    public void remover(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    public List<EventoCalendario> listarPorAno(int ano) {
        return jpa.findByInicioBetweenOrderByInicio(LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31)).stream()
                .map(EventoCalendarioEntity::paraDominio).toList();
    }
}

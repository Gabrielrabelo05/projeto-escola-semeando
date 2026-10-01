package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface EventoCalendarioJpaRepository extends JpaRepository<EventoCalendarioEntity, UUID> {

    List<EventoCalendarioEntity> findByInicioBetweenOrderByInicio(LocalDate inicio, LocalDate fim);
}

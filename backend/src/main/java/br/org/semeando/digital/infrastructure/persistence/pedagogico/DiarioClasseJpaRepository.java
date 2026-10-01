package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface DiarioClasseJpaRepository extends JpaRepository<DiarioClasseEntity, UUID> {

    Optional<DiarioClasseEntity> findByTurmaIdAndData(UUID turmaId, LocalDate data);

    List<DiarioClasseEntity> findByTurmaIdAndDataBetweenOrderByData(UUID turmaId, LocalDate inicio, LocalDate fim);

    List<DiarioClasseEntity> findByDataBetweenOrderByData(LocalDate inicio, LocalDate fim);
}

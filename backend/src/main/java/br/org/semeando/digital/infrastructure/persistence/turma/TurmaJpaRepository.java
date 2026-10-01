package br.org.semeando.digital.infrastructure.persistence.turma;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface TurmaJpaRepository extends JpaRepository<TurmaEntity, UUID> {

    List<TurmaEntity> findByAnoLetivoOrderBySerieAscNomeAsc(int anoLetivo);

    List<TurmaEntity> findAllByOrderByAnoLetivoDescSerieAscNomeAsc();
}

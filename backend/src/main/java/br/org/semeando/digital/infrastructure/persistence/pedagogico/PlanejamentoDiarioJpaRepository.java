package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PlanejamentoDiarioJpaRepository extends JpaRepository<PlanejamentoDiarioEntity, UUID> {

    @Query("""
            select p from PlanejamentoDiarioEntity p
            where (:professorId is null or p.professorId = :professorId)
              and (:turmaId is null or p.turmaId = :turmaId)
            order by p.data desc""")
    List<PlanejamentoDiarioEntity> filtrar(@Param("professorId") UUID professorId, @Param("turmaId") UUID turmaId);
}

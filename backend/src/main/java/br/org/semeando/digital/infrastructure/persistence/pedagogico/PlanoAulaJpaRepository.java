package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import br.org.semeando.digital.domain.pedagogico.StatusPlanoAula;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PlanoAulaJpaRepository extends JpaRepository<PlanoAulaEntity, UUID> {

    @Query("""
            select p from PlanoAulaEntity p
            where (:professorId is null or p.professorId = :professorId)
              and (:turmaId is null or p.turmaId = :turmaId)
              and (:status is null or p.status = :status)
            order by p.data desc""")
    List<PlanoAulaEntity> filtrar(@Param("professorId") UUID professorId, @Param("turmaId") UUID turmaId,
            @Param("status") StatusPlanoAula status);
}

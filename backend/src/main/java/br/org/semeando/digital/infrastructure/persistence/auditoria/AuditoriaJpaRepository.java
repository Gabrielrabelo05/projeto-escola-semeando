package br.org.semeando.digital.infrastructure.persistence.auditoria;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Expõe apenas inserção e consulta: não há update nem delete (RNF-06). */
interface AuditoriaJpaRepository extends Repository<AuditoriaEntity, UUID> {

    AuditoriaEntity save(AuditoriaEntity entity);

    @Query("""
            select a from AuditoriaEntity a
            where (:entidade is null or a.entidade = :entidade)
              and (:entidadeId is null or a.entidadeId = :entidadeId)
            order by a.ocorridoEm desc""")
    List<AuditoriaEntity> consultar(@Param("entidade") String entidade, @Param("entidadeId") String entidadeId,
            Pageable pagina);
}

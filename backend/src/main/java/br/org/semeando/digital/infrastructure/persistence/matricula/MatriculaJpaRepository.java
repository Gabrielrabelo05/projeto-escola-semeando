package br.org.semeando.digital.infrastructure.persistence.matricula;

import br.org.semeando.digital.domain.matricula.StatusMatricula;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface MatriculaJpaRepository extends JpaRepository<MatriculaEntity, UUID> {

    Optional<MatriculaEntity> findByCobrancaTaxaId(UUID cobrancaId);

    List<MatriculaEntity> findByAlunoId(UUID alunoId);

    @Query("""
            select m from MatriculaEntity m
            where (:status is null or m.status = :status)
              and (:ano is null or m.anoLetivo = :ano)""")
    List<MatriculaEntity> filtrar(@Param("status") StatusMatricula status, @Param("ano") Integer ano);

    long countByTurmaIdAndStatusIn(UUID turmaId, Collection<StatusMatricula> status);

    @Query("select m.alunoId from MatriculaEntity m where m.turmaId = :turmaId and m.status = :status")
    List<UUID> alunosDaTurma(@Param("turmaId") UUID turmaId, @Param("status") StatusMatricula status);
}

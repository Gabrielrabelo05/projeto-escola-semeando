package br.org.semeando.digital.infrastructure.persistence.contrato;

import br.org.semeando.digital.domain.contrato.StatusDocumentoVinculo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface DocumentoVinculoJpaRepository extends JpaRepository<DocumentoVinculoEntity, UUID> {

    Optional<DocumentoVinculoEntity> findFirstByMatriculaIdAndStatusNotOrderByVersaoDesc(UUID matriculaId,
            StatusDocumentoVinculo status);

    Optional<DocumentoVinculoEntity> findFirstByAlunoIdAndAnoLetivoAndStatusNotOrderByGeradoEmDesc(UUID alunoId,
            int anoLetivo, StatusDocumentoVinculo status);

    List<DocumentoVinculoEntity> findByNumero(UUID numero);

    @Query("""
            select d from DocumentoVinculoEntity d
            where (:alunoId is null or d.alunoId = :alunoId)
              and (:ano is null or d.anoLetivo = :ano)
              and (:status is null or d.status = :status)
            order by d.geradoEm desc""")
    List<DocumentoVinculoEntity> pesquisar(@Param("alunoId") UUID alunoId, @Param("ano") Integer ano,
            @Param("status") StatusDocumentoVinculo status);
}

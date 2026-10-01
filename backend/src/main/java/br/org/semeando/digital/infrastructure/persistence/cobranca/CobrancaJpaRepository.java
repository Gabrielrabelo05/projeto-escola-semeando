package br.org.semeando.digital.infrastructure.persistence.cobranca;

import br.org.semeando.digital.domain.cobranca.StatusCobranca;
import br.org.semeando.digital.domain.cobranca.TipoCobranca;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CobrancaJpaRepository extends JpaRepository<CobrancaEntity, UUID> {

    Optional<CobrancaEntity> findByBoletoIdCora(String idCora);

    boolean existsByAlunoIdAndTipoAndMesReferenciaAndStatusNot(UUID alunoId, TipoCobranca tipo, String mes,
            StatusCobranca status);

    List<CobrancaEntity> findByMesReferenciaOrderByVencimento(String mes);

    List<CobrancaEntity> findByMesReferenciaStartingWithOrderByVencimento(String ano);

    List<CobrancaEntity> findByAlunoIdOrderByVencimentoDesc(UUID alunoId);

    List<CobrancaEntity> findByStatusOrderByVencimento(StatusCobranca status);
}

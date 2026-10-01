package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface FormacaoContinuadaJpaRepository extends JpaRepository<FormacaoContinuadaEntity, UUID> {

    List<FormacaoContinuadaEntity> findByProfessorIdOrderByDataDesc(UUID professorId);

    List<FormacaoContinuadaEntity> findAllByOrderByDataDesc();
}

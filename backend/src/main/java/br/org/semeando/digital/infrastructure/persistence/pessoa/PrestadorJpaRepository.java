package br.org.semeando.digital.infrastructure.persistence.pessoa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PrestadorJpaRepository extends JpaRepository<PrestadorEntity, UUID> {

    List<PrestadorEntity> findByAtivoTrueOrderByNome();

    List<PrestadorEntity> findAllByOrderByNome();
}

package br.org.semeando.digital.infrastructure.persistence.material;

import br.org.semeando.digital.domain.material.StatusLista;
import br.org.semeando.digital.domain.turma.Serie;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ListaMateriaisJpaRepository extends JpaRepository<ListaMateriaisEntity, UUID> {

    Optional<ListaMateriaisEntity> findFirstBySerieAndStatus(Serie serie, StatusLista status);

    List<ListaMateriaisEntity> findBySerieOrderByAnoLetivoDesc(Serie serie);

    List<ListaMateriaisEntity> findAllByOrderByAnoLetivoDescSerieAsc();
}

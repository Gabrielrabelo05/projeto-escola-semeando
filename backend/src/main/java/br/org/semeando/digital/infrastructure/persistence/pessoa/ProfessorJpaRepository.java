package br.org.semeando.digital.infrastructure.persistence.pessoa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ProfessorJpaRepository extends JpaRepository<ProfessorEntity, UUID> {

    Optional<ProfessorEntity> findByCpf(String cpf);

    List<ProfessorEntity> findByAtivoTrueOrderByNome();

    List<ProfessorEntity> findAllByOrderByNome();

    @Query("select p from ProfessorEntity p where lower(p.nome) like lower(concat('%', :termo, '%')) "
            + "or p.cpf = :digitos order by p.nome")
    List<ProfessorEntity> buscar(@Param("termo") String termo, @Param("digitos") String digitos);
}

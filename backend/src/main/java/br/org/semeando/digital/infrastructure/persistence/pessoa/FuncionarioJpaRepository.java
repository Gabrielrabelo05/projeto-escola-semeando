package br.org.semeando.digital.infrastructure.persistence.pessoa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface FuncionarioJpaRepository extends JpaRepository<FuncionarioEntity, UUID> {

    Optional<FuncionarioEntity> findByCpf(String cpf);

    List<FuncionarioEntity> findByAtivoTrueOrderByNome();

    List<FuncionarioEntity> findAllByOrderByNome();

    @Query("select f from FuncionarioEntity f where lower(f.nome) like lower(concat('%', :termo, '%')) "
            + "or f.cpf = :digitos order by f.nome")
    List<FuncionarioEntity> buscar(@Param("termo") String termo, @Param("digitos") String digitos);
}

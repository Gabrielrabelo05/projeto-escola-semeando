package br.org.semeando.digital.infrastructure.persistence.pessoa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ResponsavelJpaRepository extends JpaRepository<ResponsavelEntity, UUID> {

    Optional<ResponsavelEntity> findByCpf(String cpf);

    List<ResponsavelEntity> findByAtivoTrueOrderByNome();

    List<ResponsavelEntity> findAllByOrderByNome();

    @Query("select r from ResponsavelEntity r where lower(r.nome) like lower(concat('%', :termo, '%')) "
            + "or r.cpf = :digitos order by r.nome")
    List<ResponsavelEntity> buscar(@Param("termo") String termo, @Param("digitos") String digitos);
}

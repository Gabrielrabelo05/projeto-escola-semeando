package br.org.semeando.digital.infrastructure.persistence.aluno;

import br.org.semeando.digital.domain.aluno.StatusAluno;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface AlunoJpaRepository extends JpaRepository<AlunoEntity, UUID> {

    List<AlunoEntity> findByStatusOrderByNome(StatusAluno status);

    List<AlunoEntity> findAllByOrderByNome();

    List<AlunoEntity> findByCpfResponsavelOrderByNome(String cpf);

    @Query("""
            select a from AlunoEntity a
            where lower(a.nome) like lower(concat('%', :termo, '%'))
               or a.cpfResponsavel = :digitos
               or cast(a.id as string) like concat(lower(:termo), '%')
            order by a.nome""")
    List<AlunoEntity> buscar(@Param("termo") String termo, @Param("digitos") String digitos);
}

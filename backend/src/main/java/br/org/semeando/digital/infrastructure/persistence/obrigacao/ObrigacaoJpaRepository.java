package br.org.semeando.digital.infrastructure.persistence.obrigacao;

import br.org.semeando.digital.domain.obrigacao.StatusObrigacao;
import br.org.semeando.digital.domain.obrigacao.TipoObrigacao;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ObrigacaoJpaRepository extends JpaRepository<ObrigacaoEntity, UUID> {

    @Query("""
            select count(o) > 0 from ObrigacaoEntity o
            where o.tipo = :tipo and o.anoReferencia = :ano
              and ((:bimestre is null and o.bimestre is null) or o.bimestre = :bimestre)""")
    boolean existe(@Param("tipo") TipoObrigacao tipo, @Param("ano") int ano, @Param("bimestre") Integer bimestre);

    @Query("""
            select o from ObrigacaoEntity o
            where (:ano is null or o.anoReferencia = :ano)
              and (:status is null or o.status = :status)
            order by o.prazo""")
    List<ObrigacaoEntity> filtrar(@Param("ano") Integer ano, @Param("status") StatusObrigacao status);

    List<ObrigacaoEntity> findByStatusNotOrderByPrazo(StatusObrigacao status);
}

package br.org.semeando.digital.infrastructure.persistence.obrigacao;

import br.org.semeando.digital.domain.obrigacao.ObrigacaoRegulatoria;
import br.org.semeando.digital.domain.obrigacao.ObrigacaoRepository;
import br.org.semeando.digital.domain.obrigacao.StatusObrigacao;
import br.org.semeando.digital.domain.obrigacao.TipoObrigacao;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class ObrigacaoRepositoryAdapter implements ObrigacaoRepository {

    private final ObrigacaoJpaRepository jpa;

    ObrigacaoRepositoryAdapter(ObrigacaoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ObrigacaoRegulatoria salvar(ObrigacaoRegulatoria obrigacao) {
        return jpa.save(ObrigacaoEntity.de(obrigacao)).paraDominio();
    }

    @Override
    public Optional<ObrigacaoRegulatoria> buscarPorId(UUID id) {
        return jpa.findById(id).map(ObrigacaoEntity::paraDominio);
    }

    @Override
    public boolean existe(TipoObrigacao tipo, int ano, Integer bimestre) {
        return jpa.existe(tipo, ano, bimestre);
    }

    @Override
    public List<ObrigacaoRegulatoria> listar(Integer ano, StatusObrigacao status) {
        return jpa.filtrar(ano, status).stream().map(ObrigacaoEntity::paraDominio).toList();
    }

    @Override
    public List<ObrigacaoRegulatoria> listarAbertas() {
        return jpa.findByStatusNotOrderByPrazo(StatusObrigacao.CONCLUIDA).stream().map(ObrigacaoEntity::paraDominio)
                .toList();
    }
}

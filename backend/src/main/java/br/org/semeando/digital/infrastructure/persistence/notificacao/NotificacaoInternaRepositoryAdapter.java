package br.org.semeando.digital.infrastructure.persistence.notificacao;

import br.org.semeando.digital.domain.notificacao.NotificacaoInterna;
import br.org.semeando.digital.domain.notificacao.NotificacaoInternaRepository;
import br.org.semeando.digital.domain.usuario.Perfil;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class NotificacaoInternaRepositoryAdapter implements NotificacaoInternaRepository {

    private final NotificacaoInternaJpaRepository jpa;

    NotificacaoInternaRepositoryAdapter(NotificacaoInternaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public NotificacaoInterna salvar(NotificacaoInterna notificacao) {
        return jpa.save(NotificacaoInternaEntity.de(notificacao)).paraDominio();
    }

    @Override
    public Optional<NotificacaoInterna> buscarPorId(UUID id) {
        return jpa.findById(id).map(NotificacaoInternaEntity::paraDominio);
    }

    @Override
    public List<NotificacaoInterna> listarPorPerfis(Collection<Perfil> perfis, boolean somenteNaoLidas) {
        var lista = somenteNaoLidas ? jpa.findTop100ByPerfilDestinoInAndLidaFalseOrderByCriadaEmDesc(perfis)
                : jpa.findTop100ByPerfilDestinoInOrderByCriadaEmDesc(perfis);
        return lista.stream().map(NotificacaoInternaEntity::paraDominio).toList();
    }
}

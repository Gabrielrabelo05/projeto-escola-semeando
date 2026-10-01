package br.org.semeando.digital.domain.notificacao;

import br.org.semeando.digital.domain.usuario.Perfil;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificacaoInternaRepository {

    NotificacaoInterna salvar(NotificacaoInterna notificacao);

    Optional<NotificacaoInterna> buscarPorId(UUID id);

    List<NotificacaoInterna> listarPorPerfis(Collection<Perfil> perfis, boolean somenteNaoLidas);
}

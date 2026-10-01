package br.org.semeando.digital.application.notificacao;

import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.notificacao.NotificacaoInterna;
import br.org.semeando.digital.domain.notificacao.NotificacaoInternaRepository;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import br.org.semeando.digital.domain.usuario.Perfil;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Avisos internos exibidos no painel (diretor e administrador veem tudo). */
@CasoDeUso
public class NotificacoesUseCase {

    private final NotificacaoInternaRepository repository;

    public NotificacoesUseCase(NotificacaoInternaRepository repository) {
        this.repository = repository;
    }

    public List<NotificacaoInterna> minhas(Ator ator, boolean somenteNaoLidas) {
        return repository.listarPorPerfis(perfisVisiveis(ator), somenteNaoLidas);
    }

    public NotificacaoInterna marcarComoLida(UUID id) {
        NotificacaoInterna notificacao = repository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação", id));
        return repository.salvar(notificacao.marcarComoLida());
    }

    private static Set<Perfil> perfisVisiveis(Ator ator) {
        return ator.possuiPerfil(Perfil.ADMIN, Perfil.DIRETOR) ? EnumSet.allOf(Perfil.class)
                : EnumSet.of(ator.perfil());
    }
}

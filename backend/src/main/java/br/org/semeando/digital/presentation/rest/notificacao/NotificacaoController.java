package br.org.semeando.digital.presentation.rest.notificacao;

import br.org.semeando.digital.application.notificacao.NotificacoesUseCase;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.notificacao.NotificacaoInterna;
import br.org.semeando.digital.presentation.rest.shared.Perfis;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificacoes")
@PreAuthorize(Perfis.TODOS)
public class NotificacaoController {

    private final NotificacoesUseCase notificacoes;

    public NotificacaoController(NotificacoesUseCase notificacoes) {
        this.notificacoes = notificacoes;
    }

    @GetMapping
    public List<NotificacaoResponse> minhas(@RequestParam(defaultValue = "false") boolean naoLidas, Ator ator) {
        return notificacoes.minhas(ator, naoLidas).stream().map(NotificacaoResponse::de).toList();
    }

    @PostMapping("/{id}/lida")
    public NotificacaoResponse marcarComoLida(@PathVariable UUID id) {
        return NotificacaoResponse.de(notificacoes.marcarComoLida(id));
    }

    public record NotificacaoResponse(UUID id, String perfilDestino, String titulo, String mensagem,
            Instant criadaEm, boolean lida) {

        static NotificacaoResponse de(NotificacaoInterna n) {
            return new NotificacaoResponse(n.id(), n.perfilDestino().name(), n.titulo(), n.mensagem(), n.criadaEm(),
                    n.lida());
        }
    }
}

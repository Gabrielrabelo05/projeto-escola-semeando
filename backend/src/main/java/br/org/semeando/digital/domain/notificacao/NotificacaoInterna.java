package br.org.semeando.digital.domain.notificacao;

import br.org.semeando.digital.domain.usuario.Perfil;
import java.time.Instant;
import java.util.UUID;

/** Aviso exibido no painel do sistema para um perfil (ex.: alertas de prazo regulatório – RF-52). */
public record NotificacaoInterna(UUID id, Perfil perfilDestino, String titulo, String mensagem, Instant criadaEm,
        boolean lida) {

    public static NotificacaoInterna nova(Perfil perfil, String titulo, String mensagem, Instant agora) {
        return new NotificacaoInterna(UUID.randomUUID(), perfil, titulo, mensagem, agora, false);
    }

    public NotificacaoInterna marcarComoLida() {
        return new NotificacaoInterna(id, perfilDestino, titulo, mensagem, criadaEm, true);
    }
}

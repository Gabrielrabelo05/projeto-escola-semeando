package br.org.semeando.digital.application.port;

import br.org.semeando.digital.domain.usuario.Usuario;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmissorToken {

    /** Tempo para digitar o código do aplicativo depois de acertar a senha. */
    Duration VALIDADE_DESAFIO = Duration.ofMinutes(5);

    /** {@code comSegundoFator}: o login conferiu o código do aplicativo (exigido para quem tem 2FA). */
    String emitir(Usuario usuario, boolean comSegundoFator);

    /** Token curto que só prova a senha e não abre sessão: serve apenas para a etapa do código. */
    String emitirDesafio(Usuario usuario);

    Optional<Desafio> lerDesafio(String token);

    record Desafio(UUID usuarioId, Instant emitidoEm) {
    }
}

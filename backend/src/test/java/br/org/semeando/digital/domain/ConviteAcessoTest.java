package br.org.semeando.digital.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.org.semeando.digital.domain.usuario.ConviteAcesso;
import br.org.semeando.digital.domain.usuario.Perfil;
import br.org.semeando.digital.domain.usuario.Usuario;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ConviteAcessoTest {

    private final Instant agora = Instant.parse("2026-10-10T12:00:00Z");
    private final Usuario professor = Usuario.convidado("Carlos Eduardo Nunes", "carlos@example.com",
            Perfil.PROFESSOR, null);

    @Test
    void usuarioConvidadoNaoTemSenhaAteUsarOLink() {
        assertThat(professor.possuiSenha()).isFalse();
        professor.alterarSenha("hash");
        assertThat(professor.possuiSenha()).isTrue();
    }

    @Test
    void linkGuardaApenasOHashEValeUmaVezPorSeteDias() {
        ConviteAcesso convite = ConviteAcesso.emitir(professor.id(), "token-secreto", agora);

        assertThat(convite.tokenHash()).isEqualTo(ConviteAcesso.hash("token-secreto")).doesNotContain("token");
        assertThat(convite.validoEm(agora.plus(Duration.ofDays(6)))).isTrue();
        assertThat(convite.validoEm(agora.plus(Duration.ofDays(7)))).isFalse();

        convite.usar(agora);
        assertThatThrownBy(() -> convite.usar(agora)).hasFieldOrPropertyWithValue("codigo", "CONVITE_INVALIDO");
    }

    @Test
    void novoLinkRevogaOAnterior() {
        ConviteAcesso anterior = ConviteAcesso.emitir(professor.id(), "antigo", agora);
        anterior.revogar(agora);
        assertThat(anterior.validoEm(agora)).isFalse();
    }
}

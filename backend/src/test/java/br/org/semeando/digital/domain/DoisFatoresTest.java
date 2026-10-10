package br.org.semeando.digital.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.org.semeando.digital.domain.usuario.Perfil;
import br.org.semeando.digital.domain.usuario.PoliticaDoisFatores;
import br.org.semeando.digital.domain.usuario.Totp;
import br.org.semeando.digital.domain.usuario.Usuario;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DoisFatoresTest {

    /** Segredo dos vetores de teste da RFC 6238 (SHA-1): "12345678901234567890". */
    private static final String SEGREDO_RFC = Totp.base32("12345678901234567890".getBytes(StandardCharsets.US_ASCII));

    private final Instant agora = Instant.parse("2026-10-10T12:00:00Z");

    @Test
    void geraOsCodigosDaRfc6238() {
        assertThat(SEGREDO_RFC).isEqualTo("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ");
        assertThat(Totp.codigo(SEGREDO_RFC, Totp.passo(Instant.ofEpochSecond(59)))).isEqualTo("287082");
        assertThat(Totp.codigo(SEGREDO_RFC, Totp.passo(Instant.ofEpochSecond(1111111109)))).isEqualTo("081804");
        assertThat(Totp.codigo(SEGREDO_RFC, Totp.passo(Instant.ofEpochSecond(1234567890)))).isEqualTo("005924");
    }

    @Test
    void aceitaUmPassoDeToleranciaERecusaCodigoAntigoOuMalFormado() {
        long passo = Totp.passo(agora);
        assertThat(Totp.passoQueConfere(SEGREDO_RFC, Totp.codigo(SEGREDO_RFC, passo - 1), agora)).hasValue(passo - 1);
        assertThat(Totp.passoQueConfere(SEGREDO_RFC, Totp.codigo(SEGREDO_RFC, passo - 2), agora)).isEmpty();
        assertThat(Totp.passoQueConfere(SEGREDO_RFC, "12345", agora)).isEmpty();
        assertThat(Totp.passoQueConfere(SEGREDO_RFC, null, agora)).isEmpty();
    }

    @Test
    void codigoDoAplicativoNaoPodeSerReusadoERecuperacaoValeUmaVez() {
        Usuario admin = Usuario.novo("Renata Moura", "renata@example.com", "hash", Perfil.ADMIN, null);
        admin.prepararDoisFatores(SEGREDO_RFC);
        long passo = admin.conferirCodigoTotp(Totp.codigo(SEGREDO_RFC, Totp.passo(agora)), agora).orElseThrow();
        admin.ativarDoisFatores(passo, List.of("hash-recuperacao"), agora);

        assertThat(admin.conferirCodigoTotp(Totp.codigo(SEGREDO_RFC, passo), agora)).isEmpty();
        assertThat(admin.conferirCodigoTotp(Totp.codigo(SEGREDO_RFC, passo + 1), agora)).hasValue(passo + 1);
        assertThat(admin.usarCodigoRecuperacao("hash-recuperacao")).isTrue();
        assertThat(admin.usarCodigoRecuperacao("hash-recuperacao")).isFalse();
        assertThatThrownBy(() -> admin.prepararDoisFatores("OUTRO"))
                .hasFieldOrPropertyWithValue("codigo", "DOIS_FATORES_ATIVO");
    }

    @Test
    void ativarOuDesativarEncerraAsSessoesAnteriores() {
        Usuario secretaria = Usuario.novo("Paula Reis", "paula@example.com", "hash", Perfil.SECRETARIA, null);
        secretaria.prepararDoisFatores(SEGREDO_RFC);
        secretaria.ativarDoisFatores(Totp.passo(agora), List.of(), agora);
        assertThat(secretaria.aceitaSessaoEmitidaEm(agora.minusSeconds(60))).isFalse();

        secretaria.desativarDoisFatores(agora.plusSeconds(120));
        assertThat(secretaria.doisFatoresAtivo()).isFalse();
        assertThat(secretaria.aceitaSessaoEmitidaEm(agora)).isFalse();
    }

    @Test
    void politicaExigeParaAdministradorEParaQuemAtivou() {
        PoliticaDoisFatores politica = new PoliticaDoisFatores(Set.of(Perfil.ADMIN));
        Usuario admin = Usuario.novo("Renata Moura", "renata@example.com", "hash", Perfil.ADMIN, null);
        Usuario professor = Usuario.novo("Carlos Nunes", "carlos@example.com", "hash", Perfil.PROFESSOR, null);
        assertThat(politica.exige(admin)).isTrue();
        assertThat(politica.exige(professor)).isFalse();

        professor.prepararDoisFatores(SEGREDO_RFC);
        professor.ativarDoisFatores(Totp.passo(agora), List.of(), agora);
        assertThat(politica.exige(professor)).isTrue();
    }

    @Test
    void uriDoQrCodeSegueOFormatoOtpauth() {
        assertThat(Totp.uri("Semeando Digital", "renata@example.com", SEGREDO_RFC))
                .isEqualTo("otpauth://totp/Semeando%20Digital:renata%40example.com?secret=" + SEGREDO_RFC
                        + "&issuer=Semeando%20Digital&algorithm=SHA1&digits=6&period=30");
    }
}

package br.org.semeando.digital.domain.usuario;

import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Link para o usuário definir (ou redefinir) a própria senha. Só o hash do token é guardado, e o link vale
 * {@link #VALIDADE} e uma única vez.
 */
public class ConviteAcesso {

    public static final Duration VALIDADE = Duration.ofDays(7);

    private final UUID id;
    private final UUID usuarioId;
    private final String tokenHash;
    private final Instant expiraEm;
    private Instant usadoEm;

    public ConviteAcesso(UUID id, UUID usuarioId, String tokenHash, Instant expiraEm, Instant usadoEm) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.usuarioId = Validacoes.obrigatorio(usuarioId, "usuário");
        this.tokenHash = Validacoes.obrigatorio(tokenHash, "token");
        this.expiraEm = Validacoes.obrigatorio(expiraEm, "validade");
        this.usadoEm = usadoEm;
    }

    public static ConviteAcesso emitir(UUID usuarioId, String token, Instant agora) {
        return new ConviteAcesso(UUID.randomUUID(), usuarioId, hash(token), agora.plus(VALIDADE), null);
    }

    public static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    public boolean validoEm(Instant agora) {
        return usadoEm == null && agora.isBefore(expiraEm);
    }

    public void garantirValido(Instant agora) {
        if (!validoEm(agora)) {
            throw new DomainException("CONVITE_INVALIDO",
                    "Este link de acesso expirou ou já foi usado. Peça um novo link à secretaria.");
        }
    }

    public void usar(Instant agora) {
        garantirValido(agora);
        this.usadoEm = agora;
    }

    /** Um novo link invalida os anteriores ainda não usados. */
    public void revogar(Instant agora) {
        if (usadoEm == null) {
            this.usadoEm = agora;
        }
    }

    public UUID id() {
        return id;
    }

    public UUID usuarioId() {
        return usuarioId;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public Instant expiraEm() {
        return expiraEm;
    }

    public Instant usadoEm() {
        return usadoEm;
    }
}

package br.org.semeando.digital.infrastructure.persistence.usuario;

import br.org.semeando.digital.domain.usuario.ConviteAcesso;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "convites_acesso")
class ConviteAcessoEntity {

    @Id
    private UUID id;
    private UUID usuarioId;
    private String tokenHash;
    private Instant expiraEm;
    private Instant usadoEm;

    protected ConviteAcessoEntity() {
    }

    static ConviteAcessoEntity de(ConviteAcesso c) {
        ConviteAcessoEntity e = new ConviteAcessoEntity();
        e.id = c.id();
        e.usuarioId = c.usuarioId();
        e.tokenHash = c.tokenHash();
        e.expiraEm = c.expiraEm();
        e.usadoEm = c.usadoEm();
        return e;
    }

    ConviteAcesso paraDominio() {
        return new ConviteAcesso(id, usuarioId, tokenHash, expiraEm, usadoEm);
    }
}

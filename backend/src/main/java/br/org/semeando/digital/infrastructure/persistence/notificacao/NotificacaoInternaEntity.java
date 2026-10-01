package br.org.semeando.digital.infrastructure.persistence.notificacao;

import br.org.semeando.digital.domain.notificacao.NotificacaoInterna;
import br.org.semeando.digital.domain.usuario.Perfil;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notificacoes_internas")
class NotificacaoInternaEntity {

    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private Perfil perfilDestino;
    private String titulo;
    private String mensagem;
    private Instant criadaEm;
    private boolean lida;

    protected NotificacaoInternaEntity() {
    }

    static NotificacaoInternaEntity de(NotificacaoInterna n) {
        NotificacaoInternaEntity e = new NotificacaoInternaEntity();
        e.id = n.id();
        e.perfilDestino = n.perfilDestino();
        e.titulo = n.titulo();
        e.mensagem = n.mensagem();
        e.criadaEm = n.criadaEm();
        e.lida = n.lida();
        return e;
    }

    NotificacaoInterna paraDominio() {
        return new NotificacaoInterna(id, perfilDestino, titulo, mensagem, criadaEm, lida);
    }
}

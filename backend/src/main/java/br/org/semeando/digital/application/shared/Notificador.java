package br.org.semeando.digital.application.shared;

import br.org.semeando.digital.application.port.NotificacaoGateway;
import br.org.semeando.digital.application.port.NotificacaoGateway.Mensagem;
import br.org.semeando.digital.domain.notificacao.NotificacaoInterna;
import br.org.semeando.digital.domain.notificacao.NotificacaoInternaRepository;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.usuario.Perfil;
import java.time.Clock;

/** Centraliza o envio de avisos externos (e-mail/WhatsApp) e internos (painel). */
public class Notificador {

    private final NotificacaoGateway gateway;
    private final NotificacaoInternaRepository internas;
    private final Clock clock;

    public Notificador(NotificacaoGateway gateway, NotificacaoInternaRepository internas, Clock clock) {
        this.gateway = gateway;
        this.internas = internas;
        this.clock = clock;
    }

    public void responsavel(Responsavel responsavel, String assunto, String corpo) {
        gateway.enviar(new Mensagem(responsavel.nome(), responsavel.contato().email(),
                responsavel.contato().telefone(), assunto, corpo));
    }

    public void equipe(Perfil perfil, String titulo, String mensagem) {
        internas.salvar(NotificacaoInterna.nova(perfil, titulo, mensagem, clock.instant()));
    }
}

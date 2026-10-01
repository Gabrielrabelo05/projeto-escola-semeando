package br.org.semeando.digital.infrastructure.gateway.notificacao;

import br.org.semeando.digital.application.port.NotificacaoGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementação de desenvolvimento: registra no log o e-mail/WhatsApp que seria enviado.
 * Em produção substitua por um adaptador SMTP / WhatsApp Business API.
 */
@Component
class LogNotificacaoGateway implements NotificacaoGateway {

    private static final Logger LOG = LoggerFactory.getLogger(LogNotificacaoGateway.class);

    @Override
    public void enviar(Mensagem mensagem) {
        LOG.info("[NOTIFICACAO] para={} <{}> tel={} assunto='{}'\n{}", mensagem.nomeDestinatario(), mensagem.email(),
                mensagem.telefone(), mensagem.assunto(), mensagem.corpo());
    }
}

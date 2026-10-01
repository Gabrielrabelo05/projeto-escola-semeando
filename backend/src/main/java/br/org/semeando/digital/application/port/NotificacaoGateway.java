package br.org.semeando.digital.application.port;

/** Porta de saída para e-mail / WhatsApp (RF-11, RF-21, RF-30, RF-52). */
public interface NotificacaoGateway {

    void enviar(Mensagem mensagem);

    record Mensagem(String nomeDestinatario, String email, String telefone, String assunto, String corpo) {
    }
}

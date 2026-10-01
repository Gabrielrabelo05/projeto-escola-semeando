package br.org.semeando.digital.application.obrigacao;

import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.application.shared.Notificador;
import br.org.semeando.digital.domain.obrigacao.NivelAlerta;
import br.org.semeando.digital.domain.obrigacao.ObrigacaoRegulatoria;
import br.org.semeando.digital.domain.obrigacao.ObrigacaoRepository;
import br.org.semeando.digital.domain.usuario.Perfil;
import java.time.Clock;
import java.time.LocalDate;

/** RF-52 / RN-10: notifica a equipe com 30 e 15 dias de antecedência (e no vencimento). */
@CasoDeUso
public class VerificarAlertasObrigacoesUseCase {

    private final ObrigacaoRepository obrigacoes;
    private final Notificador notificador;
    private final Clock clock;

    public VerificarAlertasObrigacoesUseCase(ObrigacaoRepository obrigacoes, Notificador notificador, Clock clock) {
        this.obrigacoes = obrigacoes;
        this.notificador = notificador;
        this.clock = clock;
    }

    public int executar() {
        LocalDate hoje = LocalDate.now(clock);
        int enviados = 0;
        for (ObrigacaoRegulatoria obrigacao : obrigacoes.listarAbertas()) {
            var alerta = obrigacao.alertaDevido(hoje);
            if (alerta.isEmpty()) {
                continue;
            }
            String mensagem = mensagem(obrigacao, alerta.get(), hoje);
            notificador.equipe(Perfil.DIRETOR, "Prazo regulatório: " + obrigacao.nome(), mensagem);
            notificador.equipe(Perfil.SECRETARIA, "Prazo regulatório: " + obrigacao.nome(), mensagem);
            obrigacao.registrarAlertaEnviado(alerta.get());
            obrigacoes.salvar(obrigacao);
            enviados++;
        }
        return enviados;
    }

    private static String mensagem(ObrigacaoRegulatoria obrigacao, NivelAlerta alerta, LocalDate hoje) {
        return alerta == NivelAlerta.VENCIDA
                ? "PRAZO VENCIDO em " + obrigacao.prazo() + ". Responsável: " + obrigacao.responsavel()
                : "Faltam " + obrigacao.diasRestantes(hoje) + " dias (prazo " + obrigacao.prazo() + "). Responsável: "
                        + obrigacao.responsavel();
    }
}

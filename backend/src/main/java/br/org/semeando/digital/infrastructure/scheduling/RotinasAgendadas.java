package br.org.semeando.digital.infrastructure.scheduling;

import br.org.semeando.digital.application.cobranca.GerarCobrancasMensaisUseCase;
import br.org.semeando.digital.application.obrigacao.VerificarAlertasObrigacoesUseCase;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.shared.MesReferencia;
import java.time.Clock;
import java.time.YearMonth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/** Rotinas automáticas: boletos mensais (RF-24) e alertas de prazos regulatórios (RF-52). */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "semeando.agendamentos.habilitado", havingValue = "true", matchIfMissing = true)
public class RotinasAgendadas {

    private static final Logger LOG = LoggerFactory.getLogger(RotinasAgendadas.class);

    private final GerarCobrancasMensaisUseCase gerarCobrancas;
    private final VerificarAlertasObrigacoesUseCase verificarAlertas;
    private final Clock clock;

    public RotinasAgendadas(GerarCobrancasMensaisUseCase gerarCobrancas,
            VerificarAlertasObrigacoesUseCase verificarAlertas, Clock clock) {
        this.gerarCobrancas = gerarCobrancas;
        this.verificarAlertas = verificarAlertas;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 6 1 * *", zone = "America/Fortaleza")
    public void gerarBoletosDoMes() {
        var resultado = gerarCobrancas.executar(new MesReferencia(YearMonth.now(clock)), Ator.sistema());
        LOG.info("Boletos de {}: {} emitidos, {} ignorados", resultado.mes(), resultado.geradas().size(),
                resultado.ignoradas().size());
    }

    @Scheduled(cron = "0 0 7 * * *", zone = "America/Fortaleza")
    public void alertarPrazosRegulatorios() {
        LOG.info("Alertas regulatórios enviados: {}", verificarAlertas.executar());
    }
}

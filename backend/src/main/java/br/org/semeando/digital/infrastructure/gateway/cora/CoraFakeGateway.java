package br.org.semeando.digital.infrastructure.gateway.cora;

import br.org.semeando.digital.application.port.CoraGateway;
import br.org.semeando.digital.domain.cobranca.Boleto;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulação local da API do Cora para desenvolvimento e testes (RN-08: nenhum dado real sai do ambiente).
 * O pagamento é confirmado chamando o webhook {@code POST /api/webhooks/cora}.
 */
public class CoraFakeGateway implements CoraGateway {

    private static final Logger LOG = LoggerFactory.getLogger(CoraFakeGateway.class);

    @Override
    public Boleto emitirBoleto(SolicitacaoBoleto solicitacao) {
        String id = "fake-" + UUID.randomUUID();
        LOG.info("[CORA-FAKE] boleto {} emitido: {} {} (desconto {}) venc. {}", id, solicitacao.descricao(),
                solicitacao.valor(), solicitacao.desconto(), solicitacao.vencimento());
        return new Boleto(id, linhaDigitavel(), "https://cora.fake/boletos/" + id);
    }

    @Override
    public void cancelarBoleto(String idCora) {
        LOG.info("[CORA-FAKE] boleto {} cancelado", idCora);
    }

    private static String linhaDigitavel() {
        StringBuilder linha = new StringBuilder("40390");
        ThreadLocalRandom aleatorio = ThreadLocalRandom.current();
        while (linha.length() < 47) {
            linha.append(aleatorio.nextInt(10));
        }
        return linha.toString();
    }
}

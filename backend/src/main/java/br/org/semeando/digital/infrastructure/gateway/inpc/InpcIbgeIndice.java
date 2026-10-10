package br.org.semeando.digital.infrastructure.gateway.inpc;

import br.org.semeando.digital.domain.cobranca.IndiceCorrecaoMonetaria;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

/**
 * INPC mensal (tabela 1736, variável 44 do SIDRA/IBGE), atualizado a cada 12 horas. Se a API estiver
 * indisponível, usa a última série obtida; meses ainda não publicados não entram na correção.
 */
public class InpcIbgeIndice implements IndiceCorrecaoMonetaria {

    private static final Logger LOG = LoggerFactory.getLogger(InpcIbgeIndice.class);
    private static final String CAMINHO = "/values/t/1736/n1/all/v/44/p/last 60?formato=json";
    private static final Duration VALIDADE = Duration.ofHours(12);
    private static final DateTimeFormatter MES_SIDRA = DateTimeFormatter.ofPattern("yyyyMM");

    private final RestClient http;
    private final Clock clock;
    private final Map<YearMonth, BigDecimal> variacoes = new ConcurrentHashMap<>();
    private volatile Instant atualizadoEm = Instant.EPOCH;

    public InpcIbgeIndice(RestClient http, Clock clock) {
        this.http = http;
        this.clock = clock;
    }

    @Override
    public BigDecimal variacaoAcumulada(YearMonth inicio, YearMonth fim) {
        atualizarSeNecessario();
        BigDecimal fator = BigDecimal.ONE;
        for (YearMonth mes = inicio; !mes.isAfter(fim); mes = mes.plusMonths(1)) {
            BigDecimal percentual = variacoes.getOrDefault(mes, BigDecimal.ZERO);
            fator = fator.multiply(BigDecimal.ONE.add(percentual.movePointLeft(2)), MathContext.DECIMAL64);
        }
        return fator.subtract(BigDecimal.ONE);
    }

    private synchronized void atualizarSeNecessario() {
        Instant agora = clock.instant();
        if (atualizadoEm.plus(VALIDADE).isAfter(agora)) {
            return;
        }
        atualizadoEm = agora;
        try {
            List<Map<String, String>> linhas = http.get().uri(CAMINHO).retrieve()
                    .body(new ParameterizedTypeReference<>() { });
            if (linhas == null) {
                return;
            }
            // A primeira linha do SIDRA é o cabeçalho; "V" pode vir como "..." quando o mês não foi publicado.
            linhas.stream().skip(1)
                    .filter(linha -> linha.get("V") != null && linha.get("V").matches("-?\\d+(\\.\\d+)?"))
                    .forEach(linha -> variacoes.put(YearMonth.parse(linha.get("D3C"), MES_SIDRA),
                            new BigDecimal(linha.get("V"))));
        } catch (RuntimeException e) {
            LOG.warn("INPC indisponível no IBGE; a correção usa a última série obtida: {}", e.getMessage());
        }
    }
}

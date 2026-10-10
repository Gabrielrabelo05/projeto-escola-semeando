package br.org.semeando.digital.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import br.org.semeando.digital.infrastructure.gateway.inpc.InpcIbgeIndice;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class InpcIbgeIndiceTest {

    private static final String SIDRA = """
            [{"V":"Valor","D3C":"Mês (Código)"},
             {"V":"1.00","D3C":"202604"},
             {"V":"-0.50","D3C":"202605"},
             {"V":"...","D3C":"202606"}]
            """;

    private final RestClient.Builder builder = RestClient.builder().baseUrl("https://apisidra.ibge.gov.br");
    private final MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void acumulaAsVariacoesMensaisPublicadasEIgnoraMesesSemDado() {
        servidor.expect(requestTo("https://apisidra.ibge.gov.br/values/t/1736/n1/all/v/44/p/last%2060?formato=json"))
                .andRespond(withSuccess(SIDRA, MediaType.APPLICATION_JSON));
        var indice = new InpcIbgeIndice(builder.build(), clock);

        // (1 + 0,01) * (1 - 0,005) - 1 = 0,00495; junho ainda não foi publicado.
        assertThat(indice.variacaoAcumulada(YearMonth.of(2026, 4), YearMonth.of(2026, 6)))
                .isEqualByComparingTo(new BigDecimal("0.00495"));
        // A série fica em cache: a segunda consulta não chama o IBGE de novo.
        assertThat(indice.variacaoAcumulada(YearMonth.of(2026, 4), YearMonth.of(2026, 4)))
                .isEqualByComparingTo(new BigDecimal("0.01"));
        servidor.verify();
    }

    @Test
    void ibgeIndisponivelNaoImpedeOCalculo() {
        servidor.expect(requestTo("https://apisidra.ibge.gov.br/values/t/1736/n1/all/v/44/p/last%2060?formato=json"))
                .andRespond(withServerError());
        var indice = new InpcIbgeIndice(builder.build(), clock);

        assertThat(indice.variacaoAcumulada(YearMonth.of(2026, 4), YearMonth.of(2026, 5))).isZero();
    }
}

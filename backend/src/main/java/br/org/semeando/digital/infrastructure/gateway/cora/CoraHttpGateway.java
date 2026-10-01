package br.org.semeando.digital.infrastructure.gateway.cora;

import br.org.semeando.digital.application.port.CoraGateway;
import br.org.semeando.digital.domain.cobranca.Boleto;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Integração com a API de boletos do Cora (RF-24). Requer token OAuth e certificado mTLS
 * configurados no {@link RestClient} – ver README.
 */
public class CoraHttpGateway implements CoraGateway {

    private final RestClient http;

    public CoraHttpGateway(RestClient http) {
        this.http = http;
    }

    @Override
    public Boleto emitirBoleto(SolicitacaoBoleto s) {
        Map<String, Object> corpo = Map.of(
                "code", s.referenciaExterna(),
                "customer", Map.of(
                        "name", s.nomePagador(),
                        "email", s.emailPagador() == null ? "" : s.emailPagador(),
                        "document", Map.of("identity", s.documentoPagador(), "type", "CPF")),
                "services", List.of(Map.of(
                        "name", s.descricao(),
                        "amount", s.valor().valor().movePointRight(2).intValueExact())),
                "payment_terms", Map.of(
                        "due_date", s.vencimento().toString(),
                        "fine", Map.of("rate", 2.0),
                        "interest", Map.of("rate", 1.0)),
                "payment_forms", List.of("BANK_SLIP", "PIX"));
        RespostaFatura resposta = http.post().uri("/v2/invoices")
                .header("Idempotency-Key", s.referenciaExterna())
                .contentType(MediaType.APPLICATION_JSON)
                .body(corpo)
                .retrieve()
                .body(RespostaFatura.class);
        if (resposta == null) {
            throw new IllegalStateException("Resposta vazia da API do Cora");
        }
        return new Boleto(resposta.id(), resposta.bankSlip() == null ? null : resposta.bankSlip().digitable(),
                resposta.bankSlip() == null ? null : resposta.bankSlip().url());
    }

    @Override
    public void cancelarBoleto(String idCora) {
        http.delete().uri("/v2/invoices/{id}", idCora).retrieve().toBodilessEntity();
    }

    record RespostaFatura(String id, BankSlip bankSlip) {
    }

    record BankSlip(String digitable, String url) {
    }
}

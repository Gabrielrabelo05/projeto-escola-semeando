package br.org.semeando.digital.application.port;

import br.org.semeando.digital.domain.cobranca.Boleto;
import br.org.semeando.digital.domain.shared.Dinheiro;
import java.time.LocalDate;

/** Porta de saída para a plataforma de cobrança Cora (RF-03 / RF-24). */
public interface CoraGateway {

    Boleto emitirBoleto(SolicitacaoBoleto solicitacao);

    void cancelarBoleto(String idCora);

    /** {@code desconto} vale para pagamento até o vencimento (zero quando não há desconto). */
    record SolicitacaoBoleto(String referenciaExterna, String nomePagador, String documentoPagador,
            String emailPagador, Dinheiro valor, Dinheiro desconto, LocalDate vencimento, String descricao) {
    }
}

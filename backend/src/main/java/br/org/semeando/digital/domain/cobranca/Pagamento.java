package br.org.semeando.digital.domain.cobranca;

import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.LocalDate;

/** RF-27 / RF-28: pagamento confirmado (manual ou via webhook), com o usuário que registrou. */
public record Pagamento(LocalDate pagoEm, Dinheiro valorPago, FormaPagamento forma, String registradoPor) {

    public Pagamento {
        Validacoes.obrigatorio(pagoEm, "data do pagamento");
        Validacoes.obrigatorio(valorPago, "valor pago");
        Validacoes.obrigatorio(forma, "forma de pagamento");
        Validacoes.obrigatorio(registradoPor, "usuário que registrou");
    }
}

package br.org.semeando.digital.presentation.rest.cobranca;

import br.org.semeando.digital.application.cobranca.ConsultarFinanceiroUseCase.CobrancaComAluno;
import br.org.semeando.digital.application.cobranca.ConsultarFinanceiroUseCase.Inadimplente;
import br.org.semeando.digital.application.cobranca.ConsultarFinanceiroUseCase.RelatorioFinanceiro;
import br.org.semeando.digital.domain.cobranca.Cobranca;
import br.org.semeando.digital.domain.cobranca.FormaPagamento;
import br.org.semeando.digital.presentation.rest.shared.Dtos.AlunoResumo;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class CobrancaDtos {

    private CobrancaDtos() {
    }

    public record PagamentoManualRequest(@NotNull LocalDate pagoEm, @NotNull @DecimalMin("0.01") BigDecimal valorPago,
            @NotNull FormaPagamento forma) {
    }

    public record GerarMensalRequest(@NotNull String mes) {
    }

    /** Evento enviado pelo Cora quando uma fatura é paga (RF-28). */
    public record WebhookCoraRequest(@NotNull String id, String status, @JsonAlias("paid_at") LocalDate paidAt,
            @JsonAlias("total_paid") BigDecimal totalPaid) {
    }

    public record CobrancaResponse(UUID id, AlunoResumo aluno, String tipo, String mesReferencia, BigDecimal valor,
            LocalDate vencimento, String status, boolean vencida, String idBoletoCora, String linhaDigitavel,
            String urlBoleto, LocalDate pagoEm, BigDecimal valorPago, String formaPagamento, BigDecimal valorAtualizado) {

        public static CobrancaResponse de(CobrancaComAluno item, Clock clock) {
            Cobranca c = item.cobranca();
            LocalDate hoje = LocalDate.now(clock);
            var boleto = c.boleto();
            var pagamento = c.pagamento();
            return new CobrancaResponse(c.id(), AlunoResumo.de(item.aluno()), c.tipo().name(),
                    c.mesReferencia().toString(), c.valor().valor(), c.vencimento(), c.status().name(),
                    c.vencidaEm(hoje), boleto == null ? null : boleto.idCora(),
                    boleto == null ? null : boleto.linhaDigitavel(), boleto == null ? null : boleto.url(),
                    pagamento == null ? null : pagamento.pagoEm(),
                    pagamento == null ? null : pagamento.valorPago().valor(),
                    pagamento == null ? null : pagamento.forma().name(), c.valorAtualizadoEm(hoje).valor());
        }
    }

    public record InadimplenteResponse(UUID cobrancaId, AlunoResumo aluno, String mesReferencia, LocalDate vencimento,
            long diasAtraso, BigDecimal valorOriginal, BigDecimal multa, BigDecimal juros, BigDecimal valorAtualizado) {

        public static InadimplenteResponse de(Inadimplente i) {
            return new InadimplenteResponse(i.cobranca().id(), AlunoResumo.de(i.aluno()),
                    i.cobranca().mesReferencia().toString(), i.cobranca().vencimento(), i.encargos().diasAtraso(),
                    i.cobranca().valor().valor(), i.encargos().multa().valor(), i.encargos().juros().valor(),
                    i.valorAtualizado().valor());
        }
    }

    public record SerieMensal(String mes, BigDecimal previsto, BigDecimal recebido) {
    }

    public record RelatorioResponse(String mes, BigDecimal previsto, BigDecimal recebido, BigDecimal inadimplente,
            BigDecimal projecaoAnual, List<SerieMensal> serieAnual) {

        public static RelatorioResponse de(RelatorioFinanceiro r) {
            return new RelatorioResponse(r.mes().toString(), r.previsto().valor(), r.recebido().valor(),
                    r.inadimplente().valor(), r.projecaoAnual().valor(), r.serieAnual().stream()
                            .map(s -> new SerieMensal(s.mes(), s.previsto().valor(), s.recebido().valor())).toList());
        }
    }

    public record ResultadoGeracaoResponse(String mes, List<String> geradas, List<String> ignoradas) {
    }
}

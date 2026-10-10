package br.org.semeando.digital.infrastructure.persistence.cobranca;

import br.org.semeando.digital.domain.cobranca.Boleto;
import br.org.semeando.digital.domain.cobranca.Cobranca;
import br.org.semeando.digital.domain.cobranca.FormaPagamento;
import br.org.semeando.digital.domain.cobranca.Pagamento;
import br.org.semeando.digital.domain.cobranca.StatusCobranca;
import br.org.semeando.digital.domain.cobranca.TipoCobranca;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.MesReferencia;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cobrancas")
class CobrancaEntity {

    @Id
    private UUID id;
    private UUID alunoId;
    @Enumerated(EnumType.STRING)
    private TipoCobranca tipo;
    private String mesReferencia;
    private BigDecimal valor;
    private BigDecimal valorComDesconto;
    private LocalDate vencimento;
    @Enumerated(EnumType.STRING)
    private StatusCobranca status;
    private String boletoIdCora;
    private String boletoLinhaDigitavel;
    private String boletoUrl;
    private LocalDate pagoEm;
    private BigDecimal valorPago;
    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;
    private String registradoPor;

    protected CobrancaEntity() {
    }

    static CobrancaEntity de(Cobranca c) {
        CobrancaEntity e = new CobrancaEntity();
        e.id = c.id();
        e.alunoId = c.alunoId();
        e.tipo = c.tipo();
        e.mesReferencia = c.mesReferencia().toString();
        e.valor = c.valor().valor();
        e.valorComDesconto = c.valorComDesconto().valor();
        e.vencimento = c.vencimento();
        e.status = c.status();
        if (c.boleto() != null) {
            e.boletoIdCora = c.boleto().idCora();
            e.boletoLinhaDigitavel = c.boleto().linhaDigitavel();
            e.boletoUrl = c.boleto().url();
        }
        if (c.pagamento() != null) {
            e.pagoEm = c.pagamento().pagoEm();
            e.valorPago = c.pagamento().valorPago().valor();
            e.formaPagamento = c.pagamento().forma();
            e.registradoPor = c.pagamento().registradoPor();
        }
        return e;
    }

    Cobranca paraDominio() {
        Boleto boleto = boletoIdCora == null ? null : new Boleto(boletoIdCora, boletoLinhaDigitavel, boletoUrl);
        Pagamento pagamento = pagoEm == null ? null
                : new Pagamento(pagoEm, Dinheiro.de(valorPago), formaPagamento, registradoPor);
        return new Cobranca(id, alunoId, tipo, MesReferencia.de(mesReferencia), Dinheiro.de(valor),
                valorComDesconto == null ? null : Dinheiro.de(valorComDesconto), vencimento, status, boleto, pagamento);
    }
}

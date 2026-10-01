package br.org.semeando.digital.domain.cobranca;

import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.MesReferencia;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.LocalDate;
import java.util.UUID;

public class Cobranca {

    private final UUID id;
    private final UUID alunoId;
    private final TipoCobranca tipo;
    private final MesReferencia mesReferencia;
    private final Dinheiro valor;
    private final LocalDate vencimento;
    private StatusCobranca status;
    private Boleto boleto;
    private Pagamento pagamento;

    public Cobranca(UUID id, UUID alunoId, TipoCobranca tipo, MesReferencia mesReferencia, Dinheiro valor,
            LocalDate vencimento, StatusCobranca status, Boleto boleto, Pagamento pagamento) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.alunoId = Validacoes.obrigatorio(alunoId, "aluno");
        this.tipo = Validacoes.obrigatorio(tipo, "tipo");
        this.mesReferencia = Validacoes.obrigatorio(mesReferencia, "mês de referência");
        this.valor = Validacoes.obrigatorio(valor, "valor");
        this.vencimento = Validacoes.obrigatorio(vencimento, "vencimento");
        this.status = Validacoes.obrigatorio(status, "status");
        this.boleto = boleto;
        this.pagamento = pagamento;
    }

    public static Cobranca emitir(UUID alunoId, TipoCobranca tipo, MesReferencia mes, Dinheiro valor,
            LocalDate vencimento, Boleto boleto) {
        return new Cobranca(UUID.randomUUID(), alunoId, tipo, mes, valor, vencimento, StatusCobranca.EMITIDA,
                boleto, null);
    }

    /** RF-27 / RF-28: baixa do título. Pagamento em duplicidade é ignorado de forma idempotente. */
    public boolean confirmarPagamento(Pagamento pagamento) {
        if (status == StatusCobranca.PAGA) {
            return false;
        }
        if (status == StatusCobranca.CANCELADA) {
            throw new DomainException("STATUS_INVALIDO", "Cobrança cancelada não pode receber pagamento");
        }
        this.pagamento = Validacoes.obrigatorio(pagamento, "pagamento");
        this.status = StatusCobranca.PAGA;
        return true;
    }

    public void cancelar() {
        if (status == StatusCobranca.PAGA) {
            throw new DomainException("STATUS_INVALIDO", "Cobrança paga não pode ser cancelada");
        }
        this.status = StatusCobranca.CANCELADA;
    }

    /** RF-51: "vencido" é derivado da data – o boleto continua emitido no Cora. */
    public boolean vencidaEm(LocalDate data) {
        return status == StatusCobranca.EMITIDA && data.isAfter(vencimento);
    }

    public Encargos encargosEm(LocalDate data) {
        return vencidaEm(data) ? CalculadoraEncargos.calcular(valor, vencimento, data) : Encargos.nenhum();
    }

    public Dinheiro valorAtualizadoEm(LocalDate data) {
        return valor.somar(encargosEm(data).total());
    }

    public UUID id() {
        return id;
    }

    public UUID alunoId() {
        return alunoId;
    }

    public TipoCobranca tipo() {
        return tipo;
    }

    public MesReferencia mesReferencia() {
        return mesReferencia;
    }

    public Dinheiro valor() {
        return valor;
    }

    public LocalDate vencimento() {
        return vencimento;
    }

    public StatusCobranca status() {
        return status;
    }

    public Boleto boleto() {
        return boleto;
    }

    public Pagamento pagamento() {
        return pagamento;
    }
}

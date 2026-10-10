package br.org.semeando.digital.domain.cobranca;

import br.org.semeando.digital.domain.contrato.Mensalidade;
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
    private final Dinheiro valorComDesconto;
    private final LocalDate vencimento;
    private StatusCobranca status;
    private Boleto boleto;
    private Pagamento pagamento;

    public Cobranca(UUID id, UUID alunoId, TipoCobranca tipo, MesReferencia mesReferencia, Dinheiro valor,
            Dinheiro valorComDesconto, LocalDate vencimento, StatusCobranca status, Boleto boleto,
            Pagamento pagamento) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.alunoId = Validacoes.obrigatorio(alunoId, "aluno");
        this.tipo = Validacoes.obrigatorio(tipo, "tipo");
        this.mesReferencia = Validacoes.obrigatorio(mesReferencia, "mês de referência");
        this.valor = Validacoes.obrigatorio(valor, "valor");
        this.valorComDesconto = valorComDesconto == null ? valor : valorComDesconto;
        this.vencimento = Validacoes.obrigatorio(vencimento, "vencimento");
        this.status = Validacoes.obrigatorio(status, "status");
        this.boleto = boleto;
        this.pagamento = pagamento;
    }

    /** {@code boleto} é nulo para cobranças pagas na secretaria (taxa de associado). */
    public static Cobranca emitir(UUID alunoId, TipoCobranca tipo, MesReferencia mes, Mensalidade valores,
            LocalDate vencimento, Boleto boleto) {
        return new Cobranca(UUID.randomUUID(), alunoId, tipo, mes, valores.integral(), valores.comDesconto(),
                vencimento, StatusCobranca.EMITIDA, boleto, null);
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

    public Encargos encargosEm(LocalDate data, IndiceCorrecaoMonetaria indice) {
        return vencidaEm(data) && tipo.sujeitaAEncargos()
                ? CalculadoraEncargos.calcular(valor, vencimento, data, indice) : Encargos.nenhum();
    }

    /** Valor a pagar na data: com desconto até o vencimento; depois, integral mais encargos. */
    public Dinheiro valorDevidoEm(LocalDate data, IndiceCorrecaoMonetaria indice) {
        if (!data.isAfter(vencimento)) {
            return valorComDesconto;
        }
        return valor.somar(encargosEm(data, indice).total());
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

    public Dinheiro valorComDesconto() {
        return valorComDesconto;
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

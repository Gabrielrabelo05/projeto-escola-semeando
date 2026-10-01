package br.org.semeando.digital.application.cobranca;

import br.org.semeando.digital.application.port.CoraGateway;
import br.org.semeando.digital.application.port.CoraGateway.SolicitacaoBoleto;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.cobranca.Boleto;
import br.org.semeando.digital.domain.cobranca.Cobranca;
import br.org.semeando.digital.domain.cobranca.CobrancaRepository;
import br.org.semeando.digital.domain.cobranca.TipoCobranca;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.MesReferencia;
import java.time.LocalDate;
import java.util.UUID;

/** Emite o boleto no Cora e registra a cobrança correspondente. */
public class EmissorCobranca {

    private final CoraGateway cora;
    private final CobrancaRepository cobrancas;

    public EmissorCobranca(CoraGateway cora, CobrancaRepository cobrancas) {
        this.cora = cora;
        this.cobrancas = cobrancas;
    }

    public Cobranca emitir(Aluno aluno, Responsavel pagador, TipoCobranca tipo, MesReferencia mes, Dinheiro valor,
            LocalDate vencimento) {
        String referencia = tipo + "-" + mes + "-" + aluno.id().toString().substring(0, 8) + "-"
                + UUID.randomUUID().toString().substring(0, 4);
        Boleto boleto = cora.emitirBoleto(new SolicitacaoBoleto(referencia, pagador.nome(), pagador.cpf().numero(),
                pagador.contato().email(), valor, vencimento, descricao(tipo, mes, aluno)));
        return cobrancas.salvar(Cobranca.emitir(aluno.id(), tipo, mes, valor, vencimento, boleto));
    }

    private static String descricao(TipoCobranca tipo, MesReferencia mes, Aluno aluno) {
        String rotulo = switch (tipo) {
            case TAXA_MATRICULA -> "Taxa de matrícula";
            case MENSALIDADE -> "Mensalidade";
            case TAXA_ASSOCIADO -> "Taxa de associado";
        };
        return rotulo + " " + mes + " - " + aluno.nome();
    }
}

package br.org.semeando.digital.application.cobranca;

import br.org.semeando.digital.application.port.CoraGateway;
import br.org.semeando.digital.application.port.CoraGateway.SolicitacaoBoleto;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.cobranca.Boleto;
import br.org.semeando.digital.domain.cobranca.Cobranca;
import br.org.semeando.digital.domain.cobranca.CobrancaRepository;
import br.org.semeando.digital.domain.cobranca.TipoCobranca;
import br.org.semeando.digital.domain.contrato.Mensalidade;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.shared.MesReferencia;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Registra a cobrança. A taxa de associado da Educação Infantil é paga na secretaria, sem boleto; as demais
 * cobranças têm boleto emitido no Cora (com o desconto de pontualidade, quando houver).
 */
public class EmissorCobranca {

    private final CoraGateway cora;
    private final CobrancaRepository cobrancas;

    public EmissorCobranca(CoraGateway cora, CobrancaRepository cobrancas) {
        this.cora = cora;
        this.cobrancas = cobrancas;
    }

    public Cobranca emitir(Aluno aluno, Responsavel pagador, TipoCobranca tipo, MesReferencia mes,
            Mensalidade valores, LocalDate vencimento) {
        Boleto boleto = tipo == TipoCobranca.TAXA_ASSOCIADO ? null
                : cora.emitirBoleto(new SolicitacaoBoleto(referencia(tipo, mes, aluno), pagador.nome(),
                        pagador.cpf().numero(), pagador.contato().email(), valores.integral(), valores.desconto(),
                        vencimento, tipo.descricao() + " " + mes + " - " + aluno.nome()));
        return cobrancas.salvar(Cobranca.emitir(aluno.id(), tipo, mes, valores, vencimento, boleto));
    }

    private static String referencia(TipoCobranca tipo, MesReferencia mes, Aluno aluno) {
        return tipo + "-" + mes + "-" + aluno.id().toString().substring(0, 8) + "-"
                + UUID.randomUUID().toString().substring(0, 4);
    }
}

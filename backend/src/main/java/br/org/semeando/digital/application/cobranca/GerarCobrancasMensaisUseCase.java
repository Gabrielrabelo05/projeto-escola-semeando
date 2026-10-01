package br.org.semeando.digital.application.cobranca;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.aluno.StatusAluno;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.cobranca.CobrancaRepository;
import br.org.semeando.digital.domain.cobranca.PoliticaEmissaoBoleto;
import br.org.semeando.digital.domain.cobranca.TipoCobranca;
import br.org.semeando.digital.domain.contrato.DocumentoVinculo;
import br.org.semeando.digital.domain.contrato.DocumentoVinculoRepository;
import br.org.semeando.digital.domain.contrato.TabelaValores;
import br.org.semeando.digital.domain.contrato.TipoVinculo;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.pessoa.ResponsavelRepository;
import br.org.semeando.digital.domain.shared.MesReferencia;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RF-23 / RF-24: para cada aluno ativo gera a cobrança mensal conforme o vínculo
 * (Taxa de Associado para Infantil, Mensalidade para Fundamental), respeitando a RN-07.
 * Execução idempotente: meses já cobrados são ignorados.
 */
@CasoDeUso
public class GerarCobrancasMensaisUseCase {

    private final AlunoRepository alunos;
    private final ResponsavelRepository responsaveis;
    private final DocumentoVinculoRepository vinculos;
    private final CobrancaRepository cobrancas;
    private final EmissorCobranca emissor;
    private final TabelaValores tabela;
    private final Auditor auditor;

    public GerarCobrancasMensaisUseCase(AlunoRepository alunos, ResponsavelRepository responsaveis,
            DocumentoVinculoRepository vinculos, CobrancaRepository cobrancas, EmissorCobranca emissor,
            TabelaValores tabela, Auditor auditor) {
        this.alunos = alunos;
        this.responsaveis = responsaveis;
        this.vinculos = vinculos;
        this.cobrancas = cobrancas;
        this.emissor = emissor;
        this.tabela = tabela;
        this.auditor = auditor;
    }

    public ResultadoGeracao executar(MesReferencia mes, Ator ator) {
        List<String> geradas = new ArrayList<>();
        List<String> ignoradas = new ArrayList<>();
        for (Aluno aluno : alunos.listar(StatusAluno.ATIVO)) {
            Optional<DocumentoVinculo> vinculo = vinculos.buscarVigentePorAlunoEAno(aluno.id(), mes.valor().getYear());
            if (!PoliticaEmissaoBoleto.permite(aluno, vinculo)) {
                ignoradas.add(aluno.nome() + ": RN-07 – sem contrato/ficha de sócio assinado");
                continue;
            }
            TipoVinculo tipoVinculo = vinculo.get().tipo();
            TipoCobranca tipo = tipoVinculo == TipoVinculo.FICHA_SOCIO ? TipoCobranca.TAXA_ASSOCIADO
                    : TipoCobranca.MENSALIDADE;
            if (cobrancas.existe(aluno.id(), tipo, mes)) {
                ignoradas.add(aluno.nome() + ": cobrança de " + mes + " já emitida");
                continue;
            }
            Responsavel pagador = responsaveis.buscarPorId(aluno.responsavelId()).orElseThrow();
            emissor.emitir(aluno, pagador, tipo, mes, tabela.valorMensalPara(tipoVinculo),
                    mes.dia(tabela.diaVencimento()));
            geradas.add(aluno.nome());
        }
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "Cobranca", mes,
                "Geração mensal: " + geradas.size() + " emitidas, " + ignoradas.size() + " ignoradas");
        return new ResultadoGeracao(mes, geradas, ignoradas);
    }

    public record ResultadoGeracao(MesReferencia mes, List<String> geradas, List<String> ignoradas) {
    }
}

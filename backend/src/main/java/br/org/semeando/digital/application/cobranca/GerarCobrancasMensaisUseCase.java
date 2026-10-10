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
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.matricula.StatusMatricula;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.pessoa.ResponsavelRepository;
import br.org.semeando.digital.domain.shared.MesReferencia;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import br.org.semeando.digital.domain.turma.Serie;
import br.org.semeando.digital.domain.turma.TurmaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RF-23 / RF-24: para cada aluno ativo gera a cobrança mensal conforme o vínculo e a série (taxa de associado para
 * o Infantil; mensalidade de R$ 310,00 no 1º ano e R$ 375,00 do 2º ao 5º ano), respeitando a RN-07. Alunos
 * inscritos em Jiu Jitsu / Ballet recebem também a cobrança das atividades extracurriculares.
 * Execução idempotente: meses já cobrados são ignorados.
 */
@CasoDeUso
public class GerarCobrancasMensaisUseCase {

    private final AlunoRepository alunos;
    private final ResponsavelRepository responsaveis;
    private final DocumentoVinculoRepository vinculos;
    private final MatriculaRepository matriculas;
    private final TurmaRepository turmas;
    private final CobrancaRepository cobrancas;
    private final EmissorCobranca emissor;
    private final TabelaValores tabela;
    private final Auditor auditor;

    public GerarCobrancasMensaisUseCase(AlunoRepository alunos, ResponsavelRepository responsaveis,
            DocumentoVinculoRepository vinculos, MatriculaRepository matriculas, TurmaRepository turmas,
            CobrancaRepository cobrancas, EmissorCobranca emissor, TabelaValores tabela, Auditor auditor) {
        this.alunos = alunos;
        this.responsaveis = responsaveis;
        this.vinculos = vinculos;
        this.matriculas = matriculas;
        this.turmas = turmas;
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
            TipoCobranca tipo = vinculo.get().tipo() == TipoVinculo.FICHA_SOCIO ? TipoCobranca.TAXA_ASSOCIADO
                    : TipoCobranca.MENSALIDADE;
            boolean principalPendente = !cobrancas.existe(aluno.id(), tipo, mes);
            boolean atividadesPendentes = !aluno.atividades().isEmpty()
                    && !cobrancas.existe(aluno.id(), TipoCobranca.EXTRACURRICULAR, mes);
            if (!principalPendente) {
                ignoradas.add(aluno.nome() + ": cobrança de " + mes + " já emitida");
            }
            if (!principalPendente && !atividadesPendentes) {
                continue;
            }
            Responsavel pagador = responsaveis.buscarPorId(aluno.responsavelId()).orElseThrow();
            if (principalPendente) {
                emissor.emitir(aluno, pagador, tipo, mes, tabela.valorMensalPara(serie(aluno, vinculo.get())),
                        mes.dia(tabela.diaVencimento()));
                geradas.add(aluno.nome());
            }
            if (atividadesPendentes) {
                emissor.emitir(aluno, pagador, TipoCobranca.EXTRACURRICULAR, mes,
                        tabela.extracurriculares(aluno.atividades().size()), mes.dia(tabela.diaVencimento()));
                geradas.add(aluno.nome() + " (atividades extracurriculares)");
            }
        }
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "Cobranca", mes,
                "Geração mensal: " + geradas.size() + " emitidas, " + ignoradas.size() + " ignoradas");
        return new ResultadoGeracao(mes, geradas, ignoradas);
    }

    /** Série da matrícula ativa no ano do vínculo (após uma transferência de turma, a mais recente). */
    private Serie serie(Aluno aluno, DocumentoVinculo vinculo) {
        Matricula matricula = matriculas.listarPorAluno(aluno.id()).stream()
                .filter(m -> m.status() == StatusMatricula.ATIVA && m.anoLetivo() == vinculo.anoLetivo())
                .findFirst()
                .or(() -> matriculas.buscarPorId(vinculo.matriculaId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Matrícula", vinculo.matriculaId()));
        return turmas.buscarPorId(matricula.turmaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turma", matricula.turmaId())).serie();
    }

    public record ResultadoGeracao(MesReferencia mes, List<String> geradas, List<String> ignoradas) {
    }
}

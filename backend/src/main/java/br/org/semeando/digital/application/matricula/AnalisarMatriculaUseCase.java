package br.org.semeando.digital.application.matricula;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.cobranca.EmissorCobranca;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.application.shared.Notificador;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.cobranca.Cobranca;
import br.org.semeando.digital.domain.cobranca.TipoCobranca;
import br.org.semeando.digital.domain.contrato.DocumentoVinculo;
import br.org.semeando.digital.domain.contrato.DocumentoVinculoRepository;
import br.org.semeando.digital.domain.contrato.GeradorDocumentoVinculo;
import br.org.semeando.digital.domain.contrato.TabelaValores;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.shared.MesReferencia;
import br.org.semeando.digital.domain.turma.Turma;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

/**
 * RF-02: aprovação ou rejeição da matrícula pela secretaria.
 * A aprovação gera o vínculo contratual correto (passos 6–8) e o boleto da taxa de matrícula (RF-03).
 */
@CasoDeUso
public class AnalisarMatriculaUseCase {

    private static final int DIAS_PARA_VENCIMENTO_TAXA = 5;

    private final MatriculaContexto contexto;
    private final MatriculaRepository matriculas;
    private final DocumentoVinculoRepository vinculos;
    private final GeradorDocumentoVinculo gerador;
    private final TabelaValores tabela;
    private final EmissorCobranca emissor;
    private final Notificador notificador;
    private final Auditor auditor;
    private final Clock clock;

    public AnalisarMatriculaUseCase(MatriculaContexto contexto, MatriculaRepository matriculas,
            DocumentoVinculoRepository vinculos, GeradorDocumentoVinculo gerador, TabelaValores tabela,
            EmissorCobranca emissor, Notificador notificador, Auditor auditor, Clock clock) {
        this.contexto = contexto;
        this.matriculas = matriculas;
        this.vinculos = vinculos;
        this.gerador = gerador;
        this.tabela = tabela;
        this.emissor = emissor;
        this.notificador = notificador;
        this.auditor = auditor;
        this.clock = clock;
    }

    public DocumentoVinculo aprovar(UUID matriculaId, Ator ator) {
        Matricula matricula = contexto.matricula(matriculaId);
        Turma turma = contexto.turma(matricula.turmaId());
        Aluno aluno = contexto.aluno(matricula.alunoId());
        Responsavel responsavel = contexto.responsavel(aluno.responsavelId());

        contexto.garantirMatriculaUnica(matricula);
        turma.garantirVaga(matriculas.contarOcupandoVaga(turma.id()));
        DocumentoVinculo vinculo = gerador.gerarComTabelaOficial(aluno, responsavel, matricula, ator.nome(),
                clock.instant());
        matricula.aprovar();

        LocalDate hoje = LocalDate.now(clock);
        Cobranca taxa = emissor.emitir(aluno, responsavel, TipoCobranca.TAXA_MATRICULA,
                new MesReferencia(YearMonth.from(hoje)), tabela.taxaMatricula(),
                hoje.plusDays(DIAS_PARA_VENCIMENTO_TAXA));
        matricula.vincularCobrancaTaxa(taxa.id());

        matriculas.salvar(matricula);
        vinculos.salvar(vinculo);
        auditor.registrar(ator, AcaoAuditada.APROVACAO, "Matricula", matricula.id(), "Matrícula aprovada");
        notificador.responsavel(responsavel, "Matrícula aprovada",
                "A matrícula de " + aluno.nome() + " foi aprovada. Assine o " + vinculo.tipo()
                        + " no portal e pague a taxa de matrícula (" + tabela.taxaMatricula() + ") até "
                        + taxa.vencimento() + ". Pendências de documentos: " + matricula.pendencias(hoje).size());
        return vinculo;
    }

    public Matricula rejeitar(UUID matriculaId, String justificativa, Ator ator) {
        Matricula matricula = contexto.matricula(matriculaId);
        Aluno aluno = contexto.aluno(matricula.alunoId());
        matricula.rejeitar(justificativa);
        matriculas.salvar(matricula);
        auditor.registrar(ator, AcaoAuditada.REJEICAO, "Matricula", matricula.id(), justificativa);
        notificador.responsavel(contexto.responsavel(aluno.responsavelId()), "Matrícula não aprovada",
                "A pré-matrícula de " + aluno.nome() + " não foi aprovada. Motivo: " + justificativa);
        return matricula;
    }
}

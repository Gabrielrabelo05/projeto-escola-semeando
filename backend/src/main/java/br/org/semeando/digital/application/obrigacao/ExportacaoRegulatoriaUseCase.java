package br.org.semeando.digital.application.obrigacao;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.matricula.StatusMatricula;
import br.org.semeando.digital.domain.obrigacao.TipoObrigacao;
import br.org.semeando.digital.domain.pedagogico.CalculadoraFrequencia;
import br.org.semeando.digital.domain.pedagogico.DiarioClasse;
import br.org.semeando.digital.domain.pedagogico.DiarioClasseRepository;
import br.org.semeando.digital.domain.pedagogico.Frequencia;
import br.org.semeando.digital.domain.shared.IdentidadeInstitucional;
import br.org.semeando.digital.domain.turma.Turma;
import br.org.semeando.digital.domain.turma.TurmaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** RF-49 (Censo Escolar) e RF-50 (Sistema Presença): exportação assistida em CSV. */
@CasoDeUso
public class ExportacaoRegulatoriaUseCase {

    private static final String SEPARADOR = ";";

    private final AlunoRepository alunos;
    private final MatriculaRepository matriculas;
    private final TurmaRepository turmas;
    private final DiarioClasseRepository diarios;
    private final Auditor auditor;

    public ExportacaoRegulatoriaUseCase(AlunoRepository alunos, MatriculaRepository matriculas,
            TurmaRepository turmas, DiarioClasseRepository diarios, Auditor auditor) {
        this.alunos = alunos;
        this.matriculas = matriculas;
        this.turmas = turmas;
        this.diarios = diarios;
        this.auditor = auditor;
    }

    public String censoEscolar(int ano, Ator ator) {
        Map<UUID, Turma> turmasPorId = turmasDoAno(ano);
        StringBuilder csv = new StringBuilder("# " + IdentidadeInstitucional.cabecalho() + "\n");
        csv.append(String.join(SEPARADOR, "aluno", "data_nascimento", "turma", "serie", "nivel",
                "necessidades_especiais", "status")).append('\n');
        for (Matricula m : matriculas.listar(StatusMatricula.ATIVA, ano)) {
            Aluno a = alunos.buscarPorId(m.alunoId()).orElseThrow();
            Turma t = turmasPorId.get(m.turmaId());
            csv.append(String.join(SEPARADOR, a.nome(), a.dataNascimento().toString(), t.nome(), t.serie().name(),
                    t.nivel().name(), texto(a.informacoesMedicas().necessidadesEspeciais()), a.status().name()))
                    .append('\n');
        }
        auditor.registrar(ator, AcaoAuditada.EXPORTACAO, "CensoEscolar", ano, "Exportação do Censo");
        return csv.toString();
    }

    public String sistemaPresenca(int ano, int bimestre, Ator ator) {
        LocalDate fim = TipoObrigacao.SISTEMA_PRESENCA.prazo(ano, bimestre);
        LocalDate inicio = bimestre == 1 ? LocalDate.of(ano, 1, 1)
                : TipoObrigacao.SISTEMA_PRESENCA.prazo(ano, bimestre - 1).plusDays(1);
        Map<UUID, Turma> turmasPorId = turmasDoAno(ano);
        List<DiarioClasse> doPeriodo = diarios.listarPorPeriodo(inicio, fim);
        Map<UUID, List<DiarioClasse>> porTurma = doPeriodo.stream()
                .collect(Collectors.groupingBy(DiarioClasse::turmaId));

        StringBuilder csv = new StringBuilder("# " + IdentidadeInstitucional.cabecalho() + " - " + bimestre
                + "º bimestre " + ano + " (" + inicio + " a " + fim + ")\n");
        csv.append(String.join(SEPARADOR, "turma", "aluno", "aulas", "presencas", "faltas", "percentual",
                "abaixo_75")).append('\n');
        porTurma.forEach((turmaId, lista) -> CalculadoraFrequencia.daTurma(lista).forEach(f -> csv.append(
                linhaFrequencia(turmasPorId.get(turmaId), f)).append('\n')));
        auditor.registrar(ator, AcaoAuditada.EXPORTACAO, "SistemaPresenca", ano + "-B" + bimestre,
                "Exportação de frequência");
        return csv.toString();
    }

    private String linhaFrequencia(Turma turma, Frequencia f) {
        String nome = alunos.buscarPorId(f.alunoId()).map(Aluno::nome).orElse(f.alunoId().toString());
        return String.join(SEPARADOR, turma == null ? "?" : turma.nome(), nome, String.valueOf(f.aulas()),
                String.valueOf(f.presencas()), String.valueOf(f.faltas()), f.percentual().toPlainString(),
                f.emAlerta() ? "SIM" : "NAO");
    }

    private Map<UUID, Turma> turmasDoAno(int ano) {
        return turmas.listarPorAnoLetivo(ano).stream().collect(Collectors.toMap(Turma::id, Function.identity()));
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.replace(SEPARADOR, ",");
    }
}

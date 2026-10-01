package br.org.semeando.digital.application.matricula;

import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.aluno.StatusAluno;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.matricula.StatusMatricula;
import br.org.semeando.digital.domain.turma.NivelEnsino;
import br.org.semeando.digital.domain.turma.Turma;
import br.org.semeando.digital.domain.turma.TurmaRepository;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** RF-09 (vagas), RF-10 (histórico) e RF-12 (relatório de matrículas). */
@CasoDeUso
public class ConsultarMatriculasUseCase {

    private final MatriculaContexto contexto;
    private final MatriculaRepository matriculas;
    private final TurmaRepository turmas;
    private final AlunoRepository alunos;

    public ConsultarMatriculasUseCase(MatriculaContexto contexto, MatriculaRepository matriculas,
            TurmaRepository turmas, AlunoRepository alunos) {
        this.contexto = contexto;
        this.matriculas = matriculas;
        this.turmas = turmas;
        this.alunos = alunos;
    }

    public MatriculaDetalhada detalhar(UUID id) {
        Matricula matricula = contexto.matricula(id);
        return new MatriculaDetalhada(matricula, contexto.aluno(matricula.alunoId()),
                contexto.turma(matricula.turmaId()));
    }

    public List<MatriculaDetalhada> listar(StatusMatricula status, Integer anoLetivo) {
        List<Matricula> encontradas = matriculas.listar(status, anoLetivo);
        Map<UUID, Turma> turmasPorId = turmas.listarTodas().stream()
                .collect(Collectors.toMap(Turma::id, Function.identity()));
        return encontradas.stream()
                .sorted(Comparator.comparing(Matricula::dataSolicitacao).reversed())
                .map(m -> new MatriculaDetalhada(m, contexto.aluno(m.alunoId()), turmasPorId.get(m.turmaId())))
                .toList();
    }

    public List<Matricula> historicoDoAluno(UUID alunoId) {
        contexto.aluno(alunoId);
        return matriculas.listarPorAluno(alunoId).stream()
                .sorted(Comparator.comparing(Matricula::dataSolicitacao).reversed())
                .toList();
    }

    public List<OcupacaoTurma> vagasPorTurma(int anoLetivo) {
        return turmas.listarPorAnoLetivo(anoLetivo).stream()
                .map(turma -> {
                    long ocupadas = matriculas.contarOcupandoVaga(turma.id());
                    return new OcupacaoTurma(turma, ocupadas, turma.vagasDisponiveis(ocupadas));
                })
                .toList();
    }

    public RelatorioMatriculas relatorio(int anoLetivo) {
        List<OcupacaoTurma> ocupacao = vagasPorTurma(anoLetivo);
        Map<NivelEnsino, Long> porNivel = new EnumMap<>(NivelEnsino.class);
        ocupacao.forEach(o -> porNivel.merge(o.turma().nivel(), o.ocupadas(), Long::sum));
        Map<StatusAluno, Long> porStatus = new EnumMap<>(StatusAluno.class);
        for (StatusAluno status : StatusAluno.values()) {
            porStatus.put(status, (long) alunos.listar(status).size());
        }
        return new RelatorioMatriculas(anoLetivo, ocupacao, porNivel, porStatus);
    }

    public record MatriculaDetalhada(Matricula matricula, Aluno aluno, Turma turma) {
    }

    public record OcupacaoTurma(Turma turma, long ocupadas, long vagas) {
    }

    public record RelatorioMatriculas(int anoLetivo, List<OcupacaoTurma> turmas, Map<NivelEnsino, Long> porNivel,
            Map<StatusAluno, Long> porStatus) {
    }
}

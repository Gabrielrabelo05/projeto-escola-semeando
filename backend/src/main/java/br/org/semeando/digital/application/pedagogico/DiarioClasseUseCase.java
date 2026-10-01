package br.org.semeando.digital.application.pedagogico;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.pedagogico.CalculadoraFrequencia;
import br.org.semeando.digital.domain.pedagogico.DiarioClasse;
import br.org.semeando.digital.domain.pedagogico.DiarioClasseRepository;
import br.org.semeando.digital.domain.pedagogico.Frequencia;
import br.org.semeando.digital.domain.pedagogico.Presenca;
import br.org.semeando.digital.domain.shared.DomainException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** RF-40 (diário), RF-44 (frequência por aluno) e RF-45 (frequência por turma). */
@CasoDeUso
public class DiarioClasseUseCase {

    private final DiarioClasseRepository diarios;
    private final MatriculaRepository matriculas;
    private final AlunoRepository alunos;
    private final ProfessorLogado professorLogado;
    private final Auditor auditor;

    public DiarioClasseUseCase(DiarioClasseRepository diarios, MatriculaRepository matriculas,
            AlunoRepository alunos, ProfessorLogado professorLogado, Auditor auditor) {
        this.diarios = diarios;
        this.matriculas = matriculas;
        this.alunos = alunos;
        this.professorLogado = professorLogado;
        this.auditor = auditor;
    }

    /** Lista de chamada: alunos ativos da turma. */
    public List<Aluno> chamada(UUID turmaId) {
        return matriculas.alunosAtivosDaTurma(turmaId).stream()
                .flatMap(id -> alunos.buscarPorId(id).stream())
                .toList();
    }

    public DiarioClasse registrar(UUID turmaId, LocalDate data, String conteudo, List<Presenca> presencas,
            Ator ator) {
        Set<UUID> daTurma = Set.copyOf(matriculas.alunosAtivosDaTurma(turmaId));
        if (presencas.stream().anyMatch(p -> !daTurma.contains(p.alunoId()))) {
            throw new DomainException("ALUNO_FORA_DA_TURMA", "Há alunos na chamada que não pertencem à turma");
        }
        UUID professorId = professorLogado.idDe(ator);
        DiarioClasse diario = diarios.buscarPorTurmaEData(turmaId, data)
                .map(existente -> {
                    existente.registrar(conteudo, presencas);
                    return existente;
                })
                .orElseGet(() -> DiarioClasse.registrar(professorId, turmaId, data, conteudo, presencas));
        auditor.registrar(ator, AcaoAuditada.EDICAO, "DiarioClasse", diario.id(), "Chamada de " + data);
        return diarios.salvar(diario);
    }

    public List<DiarioClasse> listar(UUID turmaId, LocalDate inicio, LocalDate fim) {
        return diarios.listarPorTurma(turmaId, inicio, fim);
    }

    public Frequencia frequenciaDoAluno(UUID alunoId, UUID turmaId, LocalDate inicio, LocalDate fim) {
        return CalculadoraFrequencia.doAluno(alunoId, diarios.listarPorTurma(turmaId, inicio, fim));
    }

    public List<FrequenciaAluno> frequenciaDaTurma(UUID turmaId, LocalDate inicio, LocalDate fim) {
        return CalculadoraFrequencia.daTurma(diarios.listarPorTurma(turmaId, inicio, fim)).stream()
                .map(f -> new FrequenciaAluno(alunos.buscarPorId(f.alunoId()).map(Aluno::nome).orElse("?"), f))
                .toList();
    }

    public record FrequenciaAluno(String nomeAluno, Frequencia frequencia) {
    }
}

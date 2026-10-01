package br.org.semeando.digital.application.matricula;

import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.matricula.StatusMatricula;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.pessoa.ResponsavelRepository;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import br.org.semeando.digital.domain.shared.RegraNegocioVioladaException;
import br.org.semeando.digital.domain.turma.Turma;
import br.org.semeando.digital.domain.turma.TurmaRepository;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/** Carrega os agregados envolvidos no processo de matrícula e aplica verificações compartilhadas. */
public class MatriculaContexto {

    private static final Set<StatusMatricula> EM_VIGOR = EnumSet.of(StatusMatricula.AGUARDANDO_ANALISE,
            StatusMatricula.APROVADA, StatusMatricula.ATIVA);

    private final MatriculaRepository matriculas;
    private final AlunoRepository alunos;
    private final TurmaRepository turmas;
    private final ResponsavelRepository responsaveis;

    public MatriculaContexto(MatriculaRepository matriculas, AlunoRepository alunos, TurmaRepository turmas,
            ResponsavelRepository responsaveis) {
        this.matriculas = matriculas;
        this.alunos = alunos;
        this.turmas = turmas;
        this.responsaveis = responsaveis;
    }

    public Matricula matricula(UUID id) {
        return matriculas.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Matrícula", id));
    }

    public Aluno aluno(UUID id) {
        return alunos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", id));
    }

    public Turma turma(UUID id) {
        return turmas.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Turma", id));
    }

    public Responsavel responsavel(UUID id) {
        return responsaveis.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Responsável", id));
    }

    public Aluno salvar(Aluno aluno) {
        return alunos.salvar(aluno);
    }

    /** RN-04: um aluno só pode estar matriculado em uma turma por vez. */
    public void garantirMatriculaUnica(Matricula candidata) {
        boolean possuiOutra = matriculas.listarPorAluno(candidata.alunoId()).stream()
                .filter(m -> !m.id().equals(candidata.id()))
                .filter(m -> m.anoLetivo() == candidata.anoLetivo())
                .anyMatch(m -> EM_VIGOR.contains(m.status()));
        if (possuiOutra) {
            throw new RegraNegocioVioladaException("RN-04",
                    "O aluno já possui matrícula em vigor no ano letivo " + candidata.anoLetivo());
        }
    }
}

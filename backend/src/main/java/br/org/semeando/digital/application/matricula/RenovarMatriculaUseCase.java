package br.org.semeando.digital.application.matricula;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.application.shared.Notificador;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.matricula.TipoDocumento;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.turma.Turma;
import br.org.semeando.digital.domain.usuario.Perfil;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

/** RF-08: renovação para alunos já cadastrados, reaproveitando dados e documentos vigentes. */
@CasoDeUso
public class RenovarMatriculaUseCase {

    private final MatriculaContexto contexto;
    private final MatriculaRepository matriculas;
    private final Notificador notificador;
    private final Auditor auditor;
    private final Clock clock;

    public RenovarMatriculaUseCase(MatriculaContexto contexto, MatriculaRepository matriculas,
            Notificador notificador, Auditor auditor, Clock clock) {
        this.contexto = contexto;
        this.matriculas = matriculas;
        this.notificador = notificador;
        this.auditor = auditor;
        this.clock = clock;
    }

    public Matricula executar(UUID alunoId, UUID novaTurmaId, Ator ator) {
        Aluno aluno = contexto.aluno(alunoId);
        Turma turma = contexto.turma(novaTurmaId);
        Matricula anterior = matriculas.listarPorAluno(alunoId).stream()
                .max(Comparator.comparingInt(Matricula::anoLetivo))
                .orElseThrow(() -> new DomainException("SEM_HISTORICO",
                        "Aluno sem matrícula anterior – utilize a pré-matrícula"));

        LocalDate hoje = LocalDate.now(clock);
        Matricula renovacao = anterior.renovar(turma.id(), turma.nivel(), turma.anoLetivo(), hoje);
        contexto.garantirMatriculaUnica(renovacao);
        matriculas.salvar(renovacao);

        String pendencias = renovacao.pendencias(hoje).stream().map(TipoDocumento::descricao)
                .collect(Collectors.joining(", "));
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "Matricula", renovacao.id(),
                "Renovação de " + aluno.nome() + " para " + turma.nome());
        notificador.equipe(Perfil.SECRETARIA, "Renovação de matrícula", aluno.nome() + " → " + turma.nome());
        notificador.responsavel(contexto.responsavel(aluno.responsavelId()), "Renovação de matrícula registrada",
                pendencias.isEmpty() ? "Nenhum documento pendente."
                        : "Documentos vencidos ou novos a enviar: " + pendencias);
        return renovacao;
    }
}

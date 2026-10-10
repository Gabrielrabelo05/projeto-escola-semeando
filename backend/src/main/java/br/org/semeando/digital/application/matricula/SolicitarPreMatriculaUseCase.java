package br.org.semeando.digital.application.matricula;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.application.shared.Notificador;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.pessoa.Contato;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.pessoa.ResponsavelRepository;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import br.org.semeando.digital.domain.turma.Turma;
import br.org.semeando.digital.domain.turma.TurmaRepository;
import br.org.semeando.digital.domain.usuario.Perfil;
import java.time.Clock;
import java.time.LocalDate;

/** RF-01 / passos 1–3 do fluxo de matrícula. */
@CasoDeUso
public class SolicitarPreMatriculaUseCase {

    private final AlunoRepository alunos;
    private final ResponsavelRepository responsaveis;
    private final MatriculaRepository matriculas;
    private final TurmaRepository turmas;
    private final Notificador notificador;
    private final Auditor auditor;
    private final Clock clock;

    public SolicitarPreMatriculaUseCase(AlunoRepository alunos, ResponsavelRepository responsaveis,
            MatriculaRepository matriculas, TurmaRepository turmas, Notificador notificador, Auditor auditor,
            Clock clock) {
        this.alunos = alunos;
        this.responsaveis = responsaveis;
        this.matriculas = matriculas;
        this.turmas = turmas;
        this.notificador = notificador;
        this.auditor = auditor;
        this.clock = clock;
    }

    public Matricula executar(PreMatriculaCommand comando, Ator ator) {
        Turma turma = turmas.buscarPorId(comando.turmaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turma", comando.turmaId()));
        Responsavel responsavel = registrarResponsavel(comando.responsavel());
        var dados = comando.aluno();
        Aluno aluno = alunos.salvar(Aluno.preMatricular(dados.nome(), dados.dataNascimento(), responsavel.id(),
                responsavel.cpf(), dados.informacoesMedicas(), dados.hospitalEmergencia(), dados.autorizados()));
        Matricula matricula = matriculas.salvar(Matricula.solicitar(aluno.id(), turma.id(), turma.nivel(),
                turma.anoLetivo(), comando.vindoDeEscolaParticular(), LocalDate.now(clock)));

        auditor.registrar(ator, AcaoAuditada.CRIACAO, "Matricula", matricula.id(),
                "Pré-matrícula de " + aluno.nome() + " em " + turma.nome());
        notificador.equipe(Perfil.SECRETARIA, "Nova pré-matrícula",
                aluno.nome() + " aguarda análise para a turma " + turma.nome());
        notificador.responsavel(responsavel, "Pré-matrícula recebida",
                "Recebemos a pré-matrícula de " + aluno.nome() + ". Status: aguardando análise da secretaria.");
        return matricula;
    }

    private Responsavel registrarResponsavel(PreMatriculaCommand.DadosResponsavel dados) {
        Cpf cpf = Cpf.de(dados.cpf());
        Contato contato = new Contato(dados.email(), dados.telefone());
        Responsavel responsavel = responsaveis.buscarPorCpf(cpf)
                .map(existente -> {
                    existente.atualizar(dados.nome(), contato, dados.parentesco());
                    return existente;
                })
                .orElseGet(() -> Responsavel.novo(dados.nome(), cpf, contato, dados.parentesco()));
        return responsaveis.salvar(responsavel);
    }
}

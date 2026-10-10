package br.org.semeando.digital.application.matricula;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.port.ArmazenamentoArquivos;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.application.shared.Notificador;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.matricula.Farda;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.matricula.StatusMatricula;
import br.org.semeando.digital.domain.matricula.TipoDocumento;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.turma.Turma;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

/** RF-04 (farda), RF-06 (documentos), RN-04 (transferência) e cancelamento. */
@CasoDeUso
public class GerenciarMatriculaUseCase {

    private final MatriculaContexto contexto;
    private final MatriculaRepository matriculas;
    private final ArmazenamentoArquivos arquivos;
    private final Notificador notificador;
    private final Auditor auditor;
    private final Clock clock;

    public GerenciarMatriculaUseCase(MatriculaContexto contexto, MatriculaRepository matriculas,
            ArmazenamentoArquivos arquivos, Notificador notificador, Auditor auditor, Clock clock) {
        this.contexto = contexto;
        this.matriculas = matriculas;
        this.arquivos = arquivos;
        this.notificador = notificador;
        this.auditor = auditor;
        this.clock = clock;
    }

    public Matricula registrarFarda(UUID matriculaId, Farda farda, Ator ator) {
        Matricula matricula = contexto.matricula(matriculaId);
        matricula.registrarFarda(farda);
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Matricula", matriculaId, "Farda: " + farda);
        return matriculas.salvar(matricula);
    }

    public Matricula enviarDocumento(UUID matriculaId, TipoDocumento tipo, String nomeArquivo, byte[] conteudo,
            Ator ator) {
        Matricula matricula = contexto.matricula(matriculaId);
        String caminho = arquivos.salvar("matriculas/" + matriculaId, nomeArquivo, conteudo);
        matricula.registrarEntregaDocumento(tipo, caminho, LocalDate.now(clock));
        auditor.registrar(ator, AcaoAuditada.UPLOAD, "Matricula", matriculaId, tipo.descricao());
        return matriculas.salvar(matricula);
    }

    /** Passo 10 do fluxo: avisa o responsável sobre documentos pendentes. */
    public void notificarPendencias(UUID matriculaId) {
        Matricula matricula = contexto.matricula(matriculaId);
        var pendencias = matricula.pendencias(LocalDate.now(clock));
        if (pendencias.isEmpty()) {
            return;
        }
        Aluno aluno = contexto.aluno(matricula.alunoId());
        String lista = pendencias.stream().map(TipoDocumento::descricao).collect(Collectors.joining(", "));
        notificador.responsavel(contexto.responsavel(aluno.responsavelId()), "Documentos pendentes",
                "Para concluir a matrícula de " + aluno.nome() + " envie: " + lista);
    }

    /** RN-04: troca de turma encerra a matrícula atual (histórico) e cria uma nova ativa. */
    public Matricula transferirTurma(UUID matriculaId, UUID novaTurmaId, Ator ator) {
        Matricula atual = contexto.matricula(matriculaId);
        Turma destino = contexto.turma(novaTurmaId);
        if (destino.anoLetivo() != atual.anoLetivo() || destino.nivel() != atual.nivel()) {
            throw new DomainException("TRANSFERENCIA_INVALIDA",
                    "A turma de destino deve ser do mesmo ano letivo e nível de ensino");
        }
        destino.garantirVaga(matriculas.contarOcupandoVaga(destino.id()));
        atual.encerrarPorTransferencia();
        Matricula nova = new Matricula(UUID.randomUUID(), atual.alunoId(), destino.id(), atual.nivel(),
                atual.anoLetivo(), atual.tipo(), StatusMatricula.ATIVA,
                LocalDate.now(clock), null, atual.farda(), atual.documentos(), atual.primeiraParcelaId());
        matriculas.salvar(atual);
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Matricula", matriculaId,
                "Transferido para a turma " + destino.nome());
        return matriculas.salvar(nova);
    }

    public Matricula cancelar(UUID matriculaId, Ator ator) {
        Matricula matricula = contexto.matricula(matriculaId);
        matricula.cancelar();
        Aluno aluno = contexto.aluno(matricula.alunoId());
        aluno.cancelar();
        contexto.salvar(aluno);
        auditor.registrar(ator, AcaoAuditada.INATIVACAO, "Matricula", matriculaId, "Matrícula cancelada");
        return matriculas.salvar(matricula);
    }
}

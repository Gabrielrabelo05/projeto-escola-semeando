package br.org.semeando.digital.application.cadastro;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import br.org.semeando.digital.domain.turma.Serie;
import br.org.semeando.digital.domain.turma.Turma;
import br.org.semeando.digital.domain.turma.TurmaRepository;
import java.util.List;
import java.util.UUID;

@CasoDeUso
public class TurmasUseCase {

    private final TurmaRepository turmas;
    private final Auditor auditor;

    public TurmasUseCase(TurmaRepository turmas, Auditor auditor) {
        this.turmas = turmas;
        this.auditor = auditor;
    }

    public List<Turma> listar(Integer anoLetivo) {
        return anoLetivo == null ? turmas.listarTodas() : turmas.listarPorAnoLetivo(anoLetivo);
    }

    public Turma buscar(UUID id) {
        return turmas.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Turma", id));
    }

    public Turma criar(String nome, Serie serie, int anoLetivo, int capacidade, UUID professorId, Ator ator) {
        Turma turma = turmas.salvar(Turma.nova(nome, serie, anoLetivo, capacidade, professorId));
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "Turma", turma.id(), turma.nome());
        return turma;
    }

    public Turma atualizar(UUID id, String nome, int capacidade, UUID professorId, Ator ator) {
        Turma turma = buscar(id);
        turma.atualizar(nome, capacidade, professorId);
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Turma", id, turma.nome());
        return turmas.salvar(turma);
    }
}

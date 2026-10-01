package br.org.semeando.digital.application.pedagogico;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.shared.AcessoNegadoException;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.application.shared.Notificador;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.pedagogico.ConteudoPlano;
import br.org.semeando.digital.domain.pedagogico.PlanoAula;
import br.org.semeando.digital.domain.pedagogico.PlanoAulaRepository;
import br.org.semeando.digital.domain.pedagogico.StatusPlanoAula;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import br.org.semeando.digital.domain.usuario.Perfil;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** RF-39 / RF-46 */
@CasoDeUso
public class PlanosAulaUseCase {

    private final PlanoAulaRepository planos;
    private final ProfessorLogado professorLogado;
    private final Notificador notificador;
    private final Auditor auditor;

    public PlanosAulaUseCase(PlanoAulaRepository planos, ProfessorLogado professorLogado, Notificador notificador,
            Auditor auditor) {
        this.planos = planos;
        this.professorLogado = professorLogado;
        this.notificador = notificador;
        this.auditor = auditor;
    }

    public List<PlanoAula> listar(UUID turmaId, StatusPlanoAula status, Ator ator) {
        UUID professorId = ator.perfil() == Perfil.PROFESSOR ? professorLogado.idDe(ator) : null;
        return planos.listar(professorId, turmaId, status);
    }

    public PlanoAula criar(UUID turmaId, LocalDate data, ConteudoPlano conteudo, Ator ator) {
        PlanoAula plano = planos.salvar(PlanoAula.criar(professorLogado.idDe(ator), turmaId, data, conteudo));
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "PlanoAula", plano.id(), conteudo.objetivo());
        return plano;
    }

    public PlanoAula editar(UUID id, LocalDate data, ConteudoPlano conteudo, Ator ator) {
        PlanoAula plano = doProfessor(id, ator);
        plano.editar(data, conteudo);
        return planos.salvar(plano);
    }

    public PlanoAula submeter(UUID id, Ator ator) {
        PlanoAula plano = doProfessor(id, ator);
        plano.submeter();
        notificador.equipe(Perfil.DIRETOR, "Plano de aula para aprovação", plano.conteudo().objetivo());
        return planos.salvar(plano);
    }

    public PlanoAula aprovar(UUID id, String comentario, Ator ator) {
        PlanoAula plano = buscar(id);
        plano.aprovar(comentario);
        auditor.registrar(ator, AcaoAuditada.APROVACAO, "PlanoAula", id, comentario);
        return planos.salvar(plano);
    }

    public PlanoAula devolver(UUID id, String comentario, Ator ator) {
        PlanoAula plano = buscar(id);
        plano.devolver(comentario);
        auditor.registrar(ator, AcaoAuditada.REJEICAO, "PlanoAula", id, comentario);
        return planos.salvar(plano);
    }

    private PlanoAula doProfessor(UUID id, Ator ator) {
        PlanoAula plano = buscar(id);
        if (!plano.pertenceA(professorLogado.idDe(ator))) {
            throw new AcessoNegadoException("Plano de aula pertence a outro professor");
        }
        return plano;
    }

    private PlanoAula buscar(UUID id) {
        return planos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Plano de aula", id));
    }
}

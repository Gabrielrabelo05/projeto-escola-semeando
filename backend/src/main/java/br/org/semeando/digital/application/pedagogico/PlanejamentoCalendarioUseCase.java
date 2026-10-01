package br.org.semeando.digital.application.pedagogico;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.pedagogico.CalendarioRepository;
import br.org.semeando.digital.domain.pedagogico.EventoCalendario;
import br.org.semeando.digital.domain.pedagogico.FormacaoContinuada;
import br.org.semeando.digital.domain.pedagogico.FormacaoContinuadaRepository;
import br.org.semeando.digital.domain.pedagogico.PlanejamentoDiario;
import br.org.semeando.digital.domain.pedagogico.PlanejamentoDiarioRepository;
import br.org.semeando.digital.domain.pedagogico.TipoEvento;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import br.org.semeando.digital.domain.usuario.Perfil;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** RF-41 (planejamento diário), RF-42 (calendário letivo) e RF-43 (formação continuada). */
@CasoDeUso
public class PlanejamentoCalendarioUseCase {

    private final PlanejamentoDiarioRepository planejamentos;
    private final CalendarioRepository calendario;
    private final FormacaoContinuadaRepository formacoes;
    private final ProfessorLogado professorLogado;
    private final Auditor auditor;

    public PlanejamentoCalendarioUseCase(PlanejamentoDiarioRepository planejamentos, CalendarioRepository calendario,
            FormacaoContinuadaRepository formacoes, ProfessorLogado professorLogado, Auditor auditor) {
        this.planejamentos = planejamentos;
        this.calendario = calendario;
        this.formacoes = formacoes;
        this.professorLogado = professorLogado;
        this.auditor = auditor;
    }

    public List<PlanejamentoDiario> planejamentos(UUID turmaId, Ator ator) {
        UUID professorId = ator.perfil() == Perfil.PROFESSOR ? professorLogado.idDe(ator) : null;
        return planejamentos.listar(professorId, turmaId);
    }

    public PlanejamentoDiario registrarPlanejamento(UUID turmaId, LocalDate data, String roteiro, Ator ator) {
        return planejamentos.salvar(PlanejamentoDiario.criar(professorLogado.idDe(ator), turmaId, data, roteiro));
    }

    public PlanejamentoDiario comentarPlanejamento(UUID id, String comentario, Ator ator) {
        PlanejamentoDiario planejamento = planejamentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Planejamento", id));
        planejamento.comentar(comentario);
        auditor.registrar(ator, AcaoAuditada.EDICAO, "PlanejamentoDiario", id, "Comentário da coordenação");
        return planejamentos.salvar(planejamento);
    }

    public List<EventoCalendario> calendario(int ano) {
        return calendario.listarPorAno(ano).stream().sorted(Comparator.comparing(EventoCalendario::inicio)).toList();
    }

    public EventoCalendario publicarEvento(String titulo, TipoEvento tipo, LocalDate inicio, LocalDate fim,
            Ator ator) {
        EventoCalendario evento = calendario.salvar(EventoCalendario.novo(titulo, tipo, inicio, fim));
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "Calendario", evento.id(), titulo);
        return evento;
    }

    public void removerEvento(UUID id, Ator ator) {
        calendario.remover(id);
        auditor.registrar(ator, AcaoAuditada.INATIVACAO, "Calendario", id, "Evento removido");
    }

    public List<FormacaoContinuada> formacoes(UUID professorId, Ator ator) {
        UUID filtro = ator.perfil() == Perfil.PROFESSOR ? professorLogado.idDe(ator) : professorId;
        return formacoes.listar(filtro);
    }

    public FormacaoContinuada registrarFormacao(UUID professorId, String tema, LocalDate data, int cargaHoraria,
            String instituicao, Ator ator) {
        UUID professor = ator.perfil() == Perfil.PROFESSOR ? professorLogado.idDe(ator) : professorId;
        FormacaoContinuada formacao = formacoes.salvar(
                FormacaoContinuada.nova(professor, tema, data, cargaHoraria, instituicao));
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "FormacaoContinuada", formacao.id(), tema);
        return formacao;
    }
}

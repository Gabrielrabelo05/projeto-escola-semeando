package br.org.semeando.digital.presentation.rest.pedagogico;

import br.org.semeando.digital.application.pedagogico.DiarioClasseUseCase;
import br.org.semeando.digital.application.pedagogico.PlanejamentoCalendarioUseCase;
import br.org.semeando.digital.application.pedagogico.PlanosAulaUseCase;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.pedagogico.StatusPlanoAula;
import br.org.semeando.digital.presentation.rest.pedagogico.PedagogicoDtos.ComentarioRequest;
import br.org.semeando.digital.presentation.rest.pedagogico.PedagogicoDtos.DiarioRequest;
import br.org.semeando.digital.presentation.rest.pedagogico.PedagogicoDtos.DiarioResponse;
import br.org.semeando.digital.presentation.rest.pedagogico.PedagogicoDtos.EventoRequest;
import br.org.semeando.digital.presentation.rest.pedagogico.PedagogicoDtos.EventoResponse;
import br.org.semeando.digital.presentation.rest.pedagogico.PedagogicoDtos.FormacaoRequest;
import br.org.semeando.digital.presentation.rest.pedagogico.PedagogicoDtos.FormacaoResponse;
import br.org.semeando.digital.presentation.rest.pedagogico.PedagogicoDtos.FrequenciaResponse;
import br.org.semeando.digital.presentation.rest.pedagogico.PedagogicoDtos.PlanejamentoRequest;
import br.org.semeando.digital.presentation.rest.pedagogico.PedagogicoDtos.PlanejamentoResponse;
import br.org.semeando.digital.presentation.rest.pedagogico.PedagogicoDtos.PlanoAulaRequest;
import br.org.semeando.digital.presentation.rest.pedagogico.PedagogicoDtos.PlanoAulaResponse;
import br.org.semeando.digital.presentation.rest.pedagogico.PedagogicoDtos.PresencaDto;
import br.org.semeando.digital.presentation.rest.shared.Dtos.AlunoResumo;
import br.org.semeando.digital.presentation.rest.shared.Perfis;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Módulo 6 – Planejamento pedagógico (RF-39 a RF-46). */
@RestController
@RequestMapping("/api/pedagogico")
public class PedagogicoController {

    private final PlanosAulaUseCase planos;
    private final DiarioClasseUseCase diario;
    private final PlanejamentoCalendarioUseCase planejamento;

    public PedagogicoController(PlanosAulaUseCase planos, DiarioClasseUseCase diario,
            PlanejamentoCalendarioUseCase planejamento) {
        this.planos = planos;
        this.diario = diario;
        this.planejamento = planejamento;
    }

    // ----- Planos de aula (RF-39 / RF-46)

    @GetMapping("/planos-aula")
    @PreAuthorize(Perfis.PROFESSOR)
    public List<PlanoAulaResponse> planos(@RequestParam(required = false) UUID turmaId,
            @RequestParam(required = false) StatusPlanoAula status, Ator ator) {
        return planos.listar(turmaId, status, ator).stream().map(PlanoAulaResponse::de).toList();
    }

    @PostMapping("/planos-aula")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PROFESSOR')")
    public PlanoAulaResponse criarPlano(@Valid @RequestBody PlanoAulaRequest r, Ator ator) {
        return PlanoAulaResponse.de(planos.criar(r.turmaId(), r.data(), r.conteudoPlano(), ator));
    }

    @PutMapping("/planos-aula/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public PlanoAulaResponse editarPlano(@PathVariable UUID id, @Valid @RequestBody PlanoAulaRequest r, Ator ator) {
        return PlanoAulaResponse.de(planos.editar(id, r.data(), r.conteudoPlano(), ator));
    }

    @PostMapping("/planos-aula/{id}/submeter")
    @PreAuthorize("hasRole('PROFESSOR')")
    public PlanoAulaResponse submeter(@PathVariable UUID id, Ator ator) {
        return PlanoAulaResponse.de(planos.submeter(id, ator));
    }

    @PostMapping("/planos-aula/{id}/aprovar")
    @PreAuthorize(Perfis.GESTAO)
    public PlanoAulaResponse aprovar(@PathVariable UUID id, @RequestBody ComentarioRequest r, Ator ator) {
        return PlanoAulaResponse.de(planos.aprovar(id, r.comentario(), ator));
    }

    @PostMapping("/planos-aula/{id}/devolver")
    @PreAuthorize(Perfis.GESTAO)
    public PlanoAulaResponse devolver(@PathVariable UUID id, @RequestBody ComentarioRequest r, Ator ator) {
        return PlanoAulaResponse.de(planos.devolver(id, r.comentario(), ator));
    }

    // ----- Diário de classe e frequência (RF-40, RF-44, RF-45)

    @GetMapping("/turmas/{turmaId}/chamada")
    @PreAuthorize(Perfis.PROFESSOR)
    public List<AlunoResumo> chamada(@PathVariable UUID turmaId) {
        return diario.chamada(turmaId).stream().map(AlunoResumo::de).toList();
    }

    @PostMapping("/diarios")
    @PreAuthorize("hasRole('PROFESSOR')")
    public DiarioResponse registrarDiario(@Valid @RequestBody DiarioRequest r, Ator ator) {
        return DiarioResponse.de(diario.registrar(r.turmaId(), r.data(), r.conteudoMinistrado(),
                r.presencas().stream().map(PresencaDto::paraDominio).toList(), ator));
    }

    @GetMapping("/turmas/{turmaId}/diarios")
    @PreAuthorize(Perfis.PROFESSOR)
    public List<DiarioResponse> diarios(@PathVariable UUID turmaId, @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim) {
        return diario.listar(turmaId, inicio, fim).stream().map(DiarioResponse::de).toList();
    }

    @GetMapping("/turmas/{turmaId}/frequencia")
    @PreAuthorize(Perfis.PROFESSOR)
    public List<FrequenciaResponse> frequenciaTurma(@PathVariable UUID turmaId, @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim) {
        return diario.frequenciaDaTurma(turmaId, inicio, fim).stream().map(FrequenciaResponse::de).toList();
    }

    @GetMapping("/turmas/{turmaId}/frequencia/{alunoId}")
    @PreAuthorize(Perfis.PROFESSOR)
    public FrequenciaResponse frequenciaAluno(@PathVariable UUID turmaId, @PathVariable UUID alunoId,
            @RequestParam LocalDate inicio, @RequestParam LocalDate fim) {
        return FrequenciaResponse.de(null, diario.frequenciaDoAluno(alunoId, turmaId, inicio, fim));
    }

    // ----- Planejamento diário (RF-41)

    @GetMapping("/planejamentos")
    @PreAuthorize(Perfis.PROFESSOR)
    public List<PlanejamentoResponse> planejamentos(@RequestParam(required = false) UUID turmaId, Ator ator) {
        return planejamento.planejamentos(turmaId, ator).stream().map(PlanejamentoResponse::de).toList();
    }

    @PostMapping("/planejamentos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PROFESSOR')")
    public PlanejamentoResponse registrarPlanejamento(@Valid @RequestBody PlanejamentoRequest r, Ator ator) {
        return PlanejamentoResponse.de(planejamento.registrarPlanejamento(r.turmaId(), r.data(), r.roteiro(), ator));
    }

    @PostMapping("/planejamentos/{id}/comentar")
    @PreAuthorize(Perfis.GESTAO)
    public PlanejamentoResponse comentar(@PathVariable UUID id, @RequestBody ComentarioRequest r, Ator ator) {
        return PlanejamentoResponse.de(planejamento.comentarPlanejamento(id, r.comentario(), ator));
    }

    // ----- Calendário letivo (RF-42) – visível a todos os perfis

    @GetMapping("/calendario")
    @PreAuthorize(Perfis.TODOS)
    public List<EventoResponse> calendario(@RequestParam int ano) {
        return planejamento.calendario(ano).stream().map(EventoResponse::de).toList();
    }

    @PostMapping("/calendario")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.SECRETARIA)
    public EventoResponse publicarEvento(@Valid @RequestBody EventoRequest r, Ator ator) {
        return EventoResponse.de(planejamento.publicarEvento(r.titulo(), r.tipo(), r.inicio(), r.fim(), ator));
    }

    @DeleteMapping("/calendario/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Perfis.SECRETARIA)
    public void removerEvento(@PathVariable UUID id, Ator ator) {
        planejamento.removerEvento(id, ator);
    }

    // ----- Formação continuada (RF-43)

    @GetMapping("/formacoes")
    @PreAuthorize(Perfis.PROFESSOR)
    public List<FormacaoResponse> formacoes(@RequestParam(required = false) UUID professorId, Ator ator) {
        return planejamento.formacoes(professorId, ator).stream().map(FormacaoResponse::de).toList();
    }

    @PostMapping("/formacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.PROFESSOR)
    public FormacaoResponse registrarFormacao(@Valid @RequestBody FormacaoRequest r, Ator ator) {
        return FormacaoResponse.de(planejamento.registrarFormacao(r.professorId(), r.tema(), r.data(),
                r.cargaHoraria(), r.instituicao(), ator));
    }
}

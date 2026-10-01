package br.org.semeando.digital.presentation.rest.matricula;

import br.org.semeando.digital.application.matricula.AnalisarMatriculaUseCase;
import br.org.semeando.digital.application.matricula.ConsultarMatriculasUseCase;
import br.org.semeando.digital.application.matricula.GerenciarMatriculaUseCase;
import br.org.semeando.digital.application.matricula.RenovarMatriculaUseCase;
import br.org.semeando.digital.application.matricula.SolicitarPreMatriculaUseCase;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.matricula.StatusMatricula;
import br.org.semeando.digital.domain.matricula.TipoDocumento;
import br.org.semeando.digital.presentation.rest.contrato.ContratoDtos.ContratoResponse;
import br.org.semeando.digital.presentation.rest.matricula.MatriculaDtos.FardaRequest;
import br.org.semeando.digital.presentation.rest.matricula.MatriculaDtos.MatriculaResponse;
import br.org.semeando.digital.presentation.rest.matricula.MatriculaDtos.OcupacaoResponse;
import br.org.semeando.digital.presentation.rest.matricula.MatriculaDtos.PreMatriculaRequest;
import br.org.semeando.digital.presentation.rest.matricula.MatriculaDtos.RejeicaoRequest;
import br.org.semeando.digital.presentation.rest.matricula.MatriculaDtos.RelatorioResponse;
import br.org.semeando.digital.presentation.rest.matricula.MatriculaDtos.RenovacaoRequest;
import br.org.semeando.digital.presentation.rest.matricula.MatriculaDtos.TransferenciaRequest;
import br.org.semeando.digital.presentation.rest.shared.Arquivos;
import br.org.semeando.digital.presentation.rest.shared.Dtos.TurmaDto;
import br.org.semeando.digital.presentation.rest.shared.Perfis;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Módulo 1 – Matrícula (RF-01 a RF-12). */
@RestController
@RequestMapping("/api")
public class MatriculaController {

    private final SolicitarPreMatriculaUseCase preMatricula;
    private final AnalisarMatriculaUseCase analise;
    private final GerenciarMatriculaUseCase gerenciar;
    private final RenovarMatriculaUseCase renovar;
    private final ConsultarMatriculasUseCase consultar;
    private final Clock clock;

    public MatriculaController(SolicitarPreMatriculaUseCase preMatricula, AnalisarMatriculaUseCase analise,
            GerenciarMatriculaUseCase gerenciar, RenovarMatriculaUseCase renovar, ConsultarMatriculasUseCase consultar,
            Clock clock) {
        this.preMatricula = preMatricula;
        this.analise = analise;
        this.gerenciar = gerenciar;
        this.renovar = renovar;
        this.consultar = consultar;
        this.clock = clock;
    }

    /** RF-01: formulário público de pré-matrícula. */
    @PostMapping("/pre-matriculas")
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaResponse solicitar(@Valid @RequestBody PreMatriculaRequest request, Ator ator) {
        return MatriculaResponse.de(preMatricula.executar(request.paraComando(), ator), clock);
    }

    /** Turmas disponíveis para o formulário público de pré-matrícula. */
    @GetMapping("/publico/turmas")
    public List<TurmaDto> turmasAbertas(@RequestParam(required = false) Integer anoLetivo) {
        int ano = anoLetivo == null ? Year.now(clock).getValue() : anoLetivo;
        return consultar.vagasPorTurma(ano).stream().filter(o -> o.vagas() > 0).map(o -> TurmaDto.de(o.turma()))
                .toList();
    }

    @GetMapping("/matriculas")
    @PreAuthorize(Perfis.SECRETARIA)
    public List<MatriculaResponse> listar(@RequestParam(required = false) StatusMatricula status,
            @RequestParam(required = false) Integer anoLetivo) {
        return consultar.listar(status, anoLetivo).stream().map(d -> MatriculaResponse.de(d, clock)).toList();
    }

    @GetMapping("/matriculas/{id}")
    @PreAuthorize(Perfis.SECRETARIA)
    public MatriculaResponse detalhar(@PathVariable UUID id) {
        return MatriculaResponse.de(consultar.detalhar(id), clock);
    }

    @PostMapping("/matriculas/{id}/aprovar")
    @PreAuthorize(Perfis.SECRETARIA)
    public ContratoResponse aprovar(@PathVariable UUID id, Ator ator) {
        return ContratoResponse.de(analise.aprovar(id, ator));
    }

    @PostMapping("/matriculas/{id}/rejeitar")
    @PreAuthorize(Perfis.SECRETARIA)
    public MatriculaResponse rejeitar(@PathVariable UUID id, @Valid @RequestBody RejeicaoRequest request, Ator ator) {
        return MatriculaResponse.de(analise.rejeitar(id, request.justificativa(), ator), clock);
    }

    @PutMapping("/matriculas/{id}/farda")
    @PreAuthorize(Perfis.SECRETARIA)
    public MatriculaResponse farda(@PathVariable UUID id, @Valid @RequestBody FardaRequest request, Ator ator) {
        return MatriculaResponse.de(gerenciar.registrarFarda(id, request.paraDominio(), ator), clock);
    }

    @PostMapping(value = "/matriculas/{id}/documentos/{tipo}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(Perfis.SECRETARIA_OU_RESPONSAVEL)
    public MatriculaResponse enviarDocumento(@PathVariable UUID id, @PathVariable TipoDocumento tipo,
            @RequestPart("arquivo") MultipartFile arquivo, Ator ator) {
        return MatriculaResponse.de(gerenciar.enviarDocumento(id, tipo, arquivo.getOriginalFilename(),
                Arquivos.bytes(arquivo), ator), clock);
    }

    @PostMapping("/matriculas/{id}/notificar-pendencias")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Perfis.SECRETARIA)
    public void notificarPendencias(@PathVariable UUID id) {
        gerenciar.notificarPendencias(id);
    }

    @PostMapping("/matriculas/{id}/transferir")
    @PreAuthorize(Perfis.SECRETARIA)
    public MatriculaResponse transferir(@PathVariable UUID id, @Valid @RequestBody TransferenciaRequest request,
            Ator ator) {
        return MatriculaResponse.de(gerenciar.transferirTurma(id, request.turmaId(), ator), clock);
    }

    @PostMapping("/matriculas/{id}/cancelar")
    @PreAuthorize(Perfis.SECRETARIA)
    public MatriculaResponse cancelar(@PathVariable UUID id, Ator ator) {
        return MatriculaResponse.de(gerenciar.cancelar(id, ator), clock);
    }

    @PostMapping("/matriculas/renovacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.SECRETARIA)
    public MatriculaResponse renovar(@Valid @RequestBody RenovacaoRequest request, Ator ator) {
        return MatriculaResponse.de(renovar.executar(request.alunoId(), request.turmaId(), ator), clock);
    }

    @GetMapping("/alunos/{alunoId}/matriculas")
    @PreAuthorize(Perfis.SECRETARIA)
    public List<MatriculaResponse> historico(@PathVariable UUID alunoId) {
        return consultar.historicoDoAluno(alunoId).stream().map(m -> MatriculaResponse.de(m, clock)).toList();
    }

    @GetMapping("/turmas/ocupacao")
    @PreAuthorize(Perfis.SECRETARIA)
    public List<OcupacaoResponse> ocupacao(@RequestParam int anoLetivo) {
        return consultar.vagasPorTurma(anoLetivo).stream().map(OcupacaoResponse::de).toList();
    }

    @GetMapping("/relatorios/matriculas")
    @PreAuthorize(Perfis.SECRETARIA)
    public RelatorioResponse relatorio(@RequestParam int anoLetivo) {
        return RelatorioResponse.de(consultar.relatorio(anoLetivo));
    }
}

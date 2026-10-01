package br.org.semeando.digital.presentation.rest.obrigacao;

import br.org.semeando.digital.application.obrigacao.ExportacaoRegulatoriaUseCase;
import br.org.semeando.digital.application.obrigacao.ObrigacoesUseCase;
import br.org.semeando.digital.application.obrigacao.VerificarAlertasObrigacoesUseCase;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.obrigacao.ItemChecklist;
import br.org.semeando.digital.domain.obrigacao.ObrigacaoRegulatoria;
import br.org.semeando.digital.domain.obrigacao.StatusObrigacao;
import br.org.semeando.digital.domain.obrigacao.TipoObrigacao;
import br.org.semeando.digital.presentation.rest.shared.Arquivos;
import br.org.semeando.digital.presentation.rest.shared.Perfis;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

/** Módulo 7 – Portal de documentos e obrigações regulatórias (RF-47 a RF-54). */
@RestController
@RequestMapping("/api/obrigacoes")
public class ObrigacaoController {

    private static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final ObrigacoesUseCase obrigacoes;
    private final VerificarAlertasObrigacoesUseCase alertas;
    private final ExportacaoRegulatoriaUseCase exportacao;
    private final Clock clock;

    public ObrigacaoController(ObrigacoesUseCase obrigacoes, VerificarAlertasObrigacoesUseCase alertas,
            ExportacaoRegulatoriaUseCase exportacao, Clock clock) {
        this.obrigacoes = obrigacoes;
        this.alertas = alertas;
        this.exportacao = exportacao;
        this.clock = clock;
    }

    @GetMapping
    @PreAuthorize(Perfis.OBRIGACOES)
    public List<ObrigacaoResponse> listar(@RequestParam(required = false) Integer ano,
            @RequestParam(required = false) StatusObrigacao status) {
        LocalDate hoje = LocalDate.now(clock);
        return obrigacoes.listar(ano, status).stream().map(o -> ObrigacaoResponse.de(o, hoje)).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize(Perfis.OBRIGACOES)
    public ObrigacaoResponse buscar(@PathVariable UUID id) {
        return ObrigacaoResponse.de(obrigacoes.buscar(id), LocalDate.now(clock));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.SECRETARIA)
    public ObrigacaoResponse criar(@Valid @RequestBody NovaObrigacaoRequest r, Ator ator) {
        ObrigacaoRegulatoria criada = r.tipo() == TipoObrigacao.OUTRA
                ? obrigacoes.criarPersonalizada(r.nome(), r.prazo(), r.responsavel(),
                        r.checklist() == null ? List.of() : r.checklist(), ator)
                : obrigacoes.criar(r.tipo(), r.ano(), r.bimestre(), r.responsavel(), ator);
        return ObrigacaoResponse.de(criada, LocalDate.now(clock));
    }

    @PostMapping("/calendario-anual")
    @PreAuthorize(Perfis.SECRETARIA)
    public List<ObrigacaoResponse> calendarioAnual(@Valid @RequestBody CalendarioAnualRequest r, Ator ator) {
        LocalDate hoje = LocalDate.now(clock);
        return obrigacoes.gerarCalendarioAnual(r.ano(), r.responsavel(), ator).stream()
                .map(o -> ObrigacaoResponse.de(o, hoje)).toList();
    }

    @PutMapping("/{id}/checklist/{indice}")
    @PreAuthorize(Perfis.SECRETARIA)
    public ObrigacaoResponse marcarItem(@PathVariable UUID id, @PathVariable int indice,
            @RequestParam boolean concluido, Ator ator) {
        return ObrigacaoResponse.de(obrigacoes.marcarItem(id, indice, concluido, ator), LocalDate.now(clock));
    }

    @PostMapping(value = "/{id}/comprovantes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(Perfis.OBRIGACOES)
    public ObrigacaoResponse anexar(@PathVariable UUID id, @RequestPart("arquivo") MultipartFile arquivo, Ator ator) {
        return ObrigacaoResponse.de(obrigacoes.anexarComprovante(id, arquivo.getOriginalFilename(),
                Arquivos.bytes(arquivo), ator), LocalDate.now(clock));
    }

    @GetMapping("/{id}/comprovantes/{indice}")
    @PreAuthorize(Perfis.OBRIGACOES)
    public ResponseEntity<byte[]> baixar(@PathVariable UUID id, @PathVariable int indice) {
        var obrigacao = obrigacoes.buscar(id);
        String nome = indice >= 0 && indice < obrigacao.comprovantes().size()
                ? obrigacao.comprovantes().get(indice).nomeArquivo() : "comprovante";
        return Arquivos.download(obrigacoes.baixarComprovante(id, indice), nome, MediaType.APPLICATION_OCTET_STREAM);
    }

    @PostMapping("/{id}/concluir")
    @PreAuthorize(Perfis.SECRETARIA)
    public ObrigacaoResponse concluir(@PathVariable UUID id, Ator ator) {
        return ObrigacaoResponse.de(obrigacoes.concluir(id, ator), LocalDate.now(clock));
    }

    @PostMapping("/verificar-alertas")
    @PreAuthorize(Perfis.GESTAO)
    public int verificarAlertas() {
        return alertas.executar();
    }

    @GetMapping("/exportacoes/censo")
    @PreAuthorize(Perfis.SECRETARIA)
    public ResponseEntity<byte[]> censo(@RequestParam int ano, Ator ator) {
        return Arquivos.download(exportacao.censoEscolar(ano, ator).getBytes(StandardCharsets.UTF_8),
                "censo-escolar-" + ano + ".csv", CSV);
    }

    @GetMapping("/exportacoes/sistema-presenca")
    @PreAuthorize(Perfis.SECRETARIA_OU_PROFESSOR)
    public ResponseEntity<byte[]> sistemaPresenca(@RequestParam int ano, @RequestParam int bimestre, Ator ator) {
        return Arquivos.download(exportacao.sistemaPresenca(ano, bimestre, ator).getBytes(StandardCharsets.UTF_8),
                "sistema-presenca-" + ano + "-b" + bimestre + ".csv", CSV);
    }

    public record NovaObrigacaoRequest(@NotNull TipoObrigacao tipo, int ano, @Min(1) @Max(4) Integer bimestre,
            String nome, LocalDate prazo, @NotBlank String responsavel, List<String> checklist) {
    }

    public record CalendarioAnualRequest(@Min(2000) int ano, @NotBlank String responsavel) {
    }

    public record ComprovanteResponse(String nomeArquivo, Instant enviadoEm, String enviadoPor) {
    }

    public record ObrigacaoResponse(UUID id, TipoObrigacao tipo, String nome, int anoReferencia, Integer bimestre,
            LocalDate prazo, long diasRestantes, boolean atrasada, String status, String responsavel,
            List<ItemChecklist> checklist, List<ComprovanteResponse> comprovantes, LocalDate concluidaEm) {

        static ObrigacaoResponse de(ObrigacaoRegulatoria o, LocalDate hoje) {
            return new ObrigacaoResponse(o.id(), o.tipo(), o.nome(), o.anoReferencia(), o.bimestre(), o.prazo(),
                    o.diasRestantes(hoje), o.atrasada(hoje), o.status().name(), o.responsavel(), o.checklist(),
                    o.comprovantes().stream()
                            .map(c -> new ComprovanteResponse(c.nomeArquivo(), c.enviadoEm(), c.enviadoPor()))
                            .toList(),
                    o.concluidaEm());
        }
    }
}

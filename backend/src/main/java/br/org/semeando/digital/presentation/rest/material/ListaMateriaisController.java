package br.org.semeando.digital.presentation.rest.material;

import br.org.semeando.digital.application.material.ListaMateriaisUseCase;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.material.ItemMaterial;
import br.org.semeando.digital.domain.material.ListaMateriais;
import br.org.semeando.digital.domain.material.TipoItem;
import br.org.semeando.digital.domain.turma.Serie;
import br.org.semeando.digital.presentation.rest.shared.Arquivos;
import br.org.semeando.digital.presentation.rest.shared.Perfis;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Módulo 3 – Lista de materiais / portal (RF-05, RF-19 a RF-22). */
@RestController
@RequestMapping("/api/listas-materiais")
public class ListaMateriaisController {

    private final ListaMateriaisUseCase listas;

    public ListaMateriaisController(ListaMateriaisUseCase listas) {
        this.listas = listas;
    }

    @GetMapping
    @PreAuthorize(Perfis.SECRETARIA)
    public List<ListaResponse> listar(@RequestParam(required = false) Serie serie) {
        return listas.listar(serie).stream().map(ListaResponse::de).toList();
    }

    @GetMapping("/publicada/{serie}")
    @PreAuthorize(Perfis.TODOS)
    public ListaResponse publicada(@PathVariable Serie serie) {
        return ListaResponse.de(listas.publicada(serie));
    }

    @GetMapping("/publicada/{serie}/pdf")
    @PreAuthorize(Perfis.TODOS)
    public ResponseEntity<byte[]> pdf(@PathVariable Serie serie) {
        return Arquivos.download(listas.exportarPdf(serie), "lista-materiais-" + serie.name().toLowerCase() + ".pdf",
                MediaType.APPLICATION_PDF);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.SECRETARIA)
    public ListaResponse criar(@Valid @RequestBody NovaListaRequest request, Ator ator) {
        return ListaResponse.de(listas.criar(request.serie(), request.anoLetivo(), itens(request.itens()), ator));
    }

    @PutMapping("/{id}/itens")
    @PreAuthorize(Perfis.SECRETARIA)
    public ListaResponse atualizarItens(@PathVariable UUID id, @Valid @RequestBody List<@Valid ItemDto> itens,
            Ator ator) {
        return ListaResponse.de(listas.atualizarItens(id, itens(itens), ator));
    }

    @PostMapping("/{id}/publicar")
    @PreAuthorize(Perfis.SECRETARIA)
    public ListaResponse publicar(@PathVariable UUID id, @Valid @RequestBody PublicacaoRequest request, Ator ator) {
        return ListaResponse.de(listas.publicar(id, request.vigenciaInicio(), request.vigenciaFim(), ator));
    }

    private static List<ItemMaterial> itens(List<ItemDto> itens) {
        return itens.stream().map(ItemDto::paraDominio).toList();
    }

    public record ItemDto(@NotBlank String descricao, @Min(1) int quantidade, @NotNull TipoItem tipo,
            String linkCompra) {

        static ItemDto de(ItemMaterial i) {
            return new ItemDto(i.descricao(), i.quantidade(), i.tipo(), i.linkCompra());
        }

        ItemMaterial paraDominio() {
            return new ItemMaterial(descricao, quantidade, tipo, linkCompra);
        }
    }

    public record NovaListaRequest(@NotNull Serie serie, @Min(2000) int anoLetivo,
            @NotEmpty List<@Valid ItemDto> itens) {
    }

    public record PublicacaoRequest(@NotNull LocalDate vigenciaInicio, @NotNull LocalDate vigenciaFim) {
    }

    public record ListaResponse(UUID id, Serie serie, String serieDescricao, int anoLetivo, String status,
            LocalDate vigenciaInicio, LocalDate vigenciaFim, LocalDate publicadaEm, List<ItemDto> itens) {

        static ListaResponse de(ListaMateriais l) {
            return new ListaResponse(l.id(), l.serie(), l.serie().descricao(), l.anoLetivo(), l.status().name(),
                    l.vigenciaInicio(), l.vigenciaFim(), l.publicadaEm(), l.itens().stream().map(ItemDto::de).toList());
        }
    }
}

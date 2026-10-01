package br.org.semeando.digital.domain.material;

import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.Validacoes;
import br.org.semeando.digital.domain.turma.Serie;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** RF-19 / RF-20 / RF-05: lista oficial de materiais e livros por série, com vigência. */
public class ListaMateriais {

    private final UUID id;
    private final Serie serie;
    private final int anoLetivo;
    private List<ItemMaterial> itens;
    private StatusLista status;
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFim;
    private LocalDate publicadaEm;

    public ListaMateriais(UUID id, Serie serie, int anoLetivo, List<ItemMaterial> itens, StatusLista status,
            LocalDate vigenciaInicio, LocalDate vigenciaFim, LocalDate publicadaEm) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.serie = Validacoes.obrigatorio(serie, "série");
        this.anoLetivo = Validacoes.positivo(anoLetivo, "ano letivo");
        this.itens = List.copyOf(itens);
        this.status = Validacoes.obrigatorio(status, "status");
        this.vigenciaInicio = vigenciaInicio;
        this.vigenciaFim = vigenciaFim;
        this.publicadaEm = publicadaEm;
    }

    public static ListaMateriais rascunho(Serie serie, int anoLetivo, List<ItemMaterial> itens) {
        return new ListaMateriais(UUID.randomUUID(), serie, anoLetivo, Validacoes.naoVazio(itens, "itens"),
                StatusLista.RASCUNHO, null, null, null);
    }

    public void atualizarItens(List<ItemMaterial> itens) {
        if (status != StatusLista.RASCUNHO) {
            throw new DomainException("STATUS_INVALIDO", "Somente listas em rascunho podem ser alteradas");
        }
        this.itens = List.copyOf(Validacoes.naoVazio(itens, "itens"));
    }

    /** RF-20: publicação com data de vigência. */
    public void publicar(LocalDate vigenciaInicio, LocalDate vigenciaFim, LocalDate hoje) {
        if (status != StatusLista.RASCUNHO) {
            throw new DomainException("STATUS_INVALIDO", "A lista já foi publicada");
        }
        Validacoes.obrigatorio(vigenciaInicio, "início da vigência");
        Validacoes.obrigatorio(vigenciaFim, "fim da vigência");
        if (vigenciaFim.isBefore(vigenciaInicio)) {
            throw new DomainException("VIGENCIA_INVALIDA", "Fim da vigência deve ser após o início");
        }
        this.vigenciaInicio = vigenciaInicio;
        this.vigenciaFim = vigenciaFim;
        this.publicadaEm = hoje;
        this.status = StatusLista.PUBLICADA;
    }

    /** RF-20: listas anteriores ficam arquivadas quando uma nova é publicada. */
    public void arquivar() {
        this.status = StatusLista.ARQUIVADA;
    }

    public List<ItemMaterial> livros() {
        return itens.stream().filter(item -> item.tipo() == TipoItem.LIVRO).toList();
    }

    public UUID id() {
        return id;
    }

    public Serie serie() {
        return serie;
    }

    public int anoLetivo() {
        return anoLetivo;
    }

    public List<ItemMaterial> itens() {
        return itens;
    }

    public StatusLista status() {
        return status;
    }

    public LocalDate vigenciaInicio() {
        return vigenciaInicio;
    }

    public LocalDate vigenciaFim() {
        return vigenciaFim;
    }

    public LocalDate publicadaEm() {
        return publicadaEm;
    }
}

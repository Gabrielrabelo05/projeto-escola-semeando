package br.org.semeando.digital.infrastructure.persistence.material;

import br.org.semeando.digital.domain.material.ListaMateriais;
import br.org.semeando.digital.domain.material.StatusLista;
import br.org.semeando.digital.domain.turma.Serie;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "listas_materiais")
class ListaMateriaisEntity {

    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private Serie serie;
    private int anoLetivo;
    @Enumerated(EnumType.STRING)
    private StatusLista status;
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFim;
    private LocalDate publicadaEm;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "lista_itens", joinColumns = @JoinColumn(name = "lista_id"))
    @OrderColumn(name = "ordem")
    private List<ItemEmbeddable> itens = new ArrayList<>();

    protected ListaMateriaisEntity() {
    }

    static ListaMateriaisEntity de(ListaMateriais l) {
        ListaMateriaisEntity e = new ListaMateriaisEntity();
        e.id = l.id();
        e.serie = l.serie();
        e.anoLetivo = l.anoLetivo();
        e.status = l.status();
        e.vigenciaInicio = l.vigenciaInicio();
        e.vigenciaFim = l.vigenciaFim();
        e.publicadaEm = l.publicadaEm();
        e.itens = new ArrayList<>(l.itens().stream().map(ItemEmbeddable::de).toList());
        return e;
    }

    ListaMateriais paraDominio() {
        return new ListaMateriais(id, serie, anoLetivo, itens.stream().map(ItemEmbeddable::paraDominio).toList(),
                status, vigenciaInicio, vigenciaFim, publicadaEm);
    }
}

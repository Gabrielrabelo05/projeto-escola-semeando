package br.org.semeando.digital.infrastructure.persistence.obrigacao;

import br.org.semeando.digital.domain.obrigacao.NivelAlerta;
import br.org.semeando.digital.domain.obrigacao.ObrigacaoRegulatoria;
import br.org.semeando.digital.domain.obrigacao.StatusObrigacao;
import br.org.semeando.digital.domain.obrigacao.TipoObrigacao;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "obrigacoes")
class ObrigacaoEntity {

    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private TipoObrigacao tipo;
    private String nome;
    private int anoReferencia;
    private Integer bimestre;
    private LocalDate prazo;
    @Enumerated(EnumType.STRING)
    private StatusObrigacao status;
    private String responsavel;
    private LocalDate concluidaEm;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "obrigacao_checklist", joinColumns = @JoinColumn(name = "obrigacao_id"))
    @OrderColumn(name = "ordem")
    private List<ItemChecklistEmbeddable> checklist = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "obrigacao_comprovantes", joinColumns = @JoinColumn(name = "obrigacao_id"))
    @OrderColumn(name = "ordem")
    private List<ComprovanteEmbeddable> comprovantes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "obrigacao_alertas", joinColumns = @JoinColumn(name = "obrigacao_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel")
    private Set<NivelAlerta> alertas = new HashSet<>();

    protected ObrigacaoEntity() {
    }

    static ObrigacaoEntity de(ObrigacaoRegulatoria o) {
        ObrigacaoEntity e = new ObrigacaoEntity();
        e.id = o.id();
        e.tipo = o.tipo();
        e.nome = o.nome();
        e.anoReferencia = o.anoReferencia();
        e.bimestre = o.bimestre();
        e.prazo = o.prazo();
        e.status = o.status();
        e.responsavel = o.responsavel();
        e.concluidaEm = o.concluidaEm();
        e.checklist = new ArrayList<>(o.checklist().stream().map(ItemChecklistEmbeddable::de).toList());
        e.comprovantes = new ArrayList<>(o.comprovantes().stream().map(ComprovanteEmbeddable::de).toList());
        e.alertas = new HashSet<>(o.alertasEnviados());
        return e;
    }

    ObrigacaoRegulatoria paraDominio() {
        return new ObrigacaoRegulatoria(id, tipo, nome, anoReferencia, bimestre, prazo, status, responsavel,
                checklist.stream().map(ItemChecklistEmbeddable::paraDominio).toList(),
                comprovantes.stream().map(ComprovanteEmbeddable::paraDominio).toList(), alertas, concluidaEm);
    }
}

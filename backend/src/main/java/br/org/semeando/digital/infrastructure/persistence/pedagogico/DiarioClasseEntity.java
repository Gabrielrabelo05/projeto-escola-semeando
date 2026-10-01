package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import br.org.semeando.digital.domain.pedagogico.DiarioClasse;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
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
@Table(name = "diarios_classe")
class DiarioClasseEntity {

    @Id
    private UUID id;
    private UUID professorId;
    private UUID turmaId;
    private LocalDate data;
    private String conteudoMinistrado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "diario_presencas", joinColumns = @JoinColumn(name = "diario_id"))
    @OrderColumn(name = "ordem")
    private List<PresencaEmbeddable> presencas = new ArrayList<>();

    protected DiarioClasseEntity() {
    }

    static DiarioClasseEntity de(DiarioClasse d) {
        DiarioClasseEntity e = new DiarioClasseEntity();
        e.id = d.id();
        e.professorId = d.professorId();
        e.turmaId = d.turmaId();
        e.data = d.data();
        e.conteudoMinistrado = d.conteudoMinistrado();
        e.presencas = new ArrayList<>(d.presencas().stream().map(PresencaEmbeddable::de).toList());
        return e;
    }

    DiarioClasse paraDominio() {
        return new DiarioClasse(id, professorId, turmaId, data, conteudoMinistrado,
                presencas.stream().map(PresencaEmbeddable::paraDominio).toList());
    }
}

package br.org.semeando.digital.infrastructure.persistence.turma;

import br.org.semeando.digital.domain.turma.Serie;
import br.org.semeando.digital.domain.turma.Turma;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "turmas")
class TurmaEntity {

    @Id
    private UUID id;
    private String nome;
    @Enumerated(EnumType.STRING)
    private Serie serie;
    private int anoLetivo;
    private int capacidadeMaxima;
    private UUID professorTitularId;

    protected TurmaEntity() {
    }

    static TurmaEntity de(Turma t) {
        TurmaEntity e = new TurmaEntity();
        e.id = t.id();
        e.nome = t.nome();
        e.serie = t.serie();
        e.anoLetivo = t.anoLetivo();
        e.capacidadeMaxima = t.capacidadeMaxima();
        e.professorTitularId = t.professorTitularId();
        return e;
    }

    Turma paraDominio() {
        return new Turma(id, nome, serie, anoLetivo, capacidadeMaxima, professorTitularId);
    }
}

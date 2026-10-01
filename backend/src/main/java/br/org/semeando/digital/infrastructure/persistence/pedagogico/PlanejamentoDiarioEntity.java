package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import br.org.semeando.digital.domain.pedagogico.PlanejamentoDiario;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "planejamentos_diarios")
class PlanejamentoDiarioEntity {

    @Id
    private UUID id;
    private UUID professorId;
    private UUID turmaId;
    private LocalDate data;
    private String roteiro;
    private String comentarioCoordenacao;

    protected PlanejamentoDiarioEntity() {
    }

    static PlanejamentoDiarioEntity de(PlanejamentoDiario p) {
        PlanejamentoDiarioEntity e = new PlanejamentoDiarioEntity();
        e.id = p.id();
        e.professorId = p.professorId();
        e.turmaId = p.turmaId();
        e.data = p.data();
        e.roteiro = p.roteiro();
        e.comentarioCoordenacao = p.comentarioCoordenacao();
        return e;
    }

    PlanejamentoDiario paraDominio() {
        return new PlanejamentoDiario(id, professorId, turmaId, data, roteiro, comentarioCoordenacao);
    }
}

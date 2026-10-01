package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import br.org.semeando.digital.domain.pedagogico.FormacaoContinuada;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "formacoes_continuadas")
class FormacaoContinuadaEntity {

    @Id
    private UUID id;
    private UUID professorId;
    private String tema;
    private LocalDate data;
    private int cargaHoraria;
    private String instituicao;

    protected FormacaoContinuadaEntity() {
    }

    static FormacaoContinuadaEntity de(FormacaoContinuada f) {
        FormacaoContinuadaEntity e = new FormacaoContinuadaEntity();
        e.id = f.id();
        e.professorId = f.professorId();
        e.tema = f.tema();
        e.data = f.data();
        e.cargaHoraria = f.cargaHoraria();
        e.instituicao = f.instituicao();
        return e;
    }

    FormacaoContinuada paraDominio() {
        return new FormacaoContinuada(id, professorId, tema, data, cargaHoraria, instituicao);
    }
}

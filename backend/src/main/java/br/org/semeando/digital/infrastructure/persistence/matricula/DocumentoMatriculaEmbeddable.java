package br.org.semeando.digital.infrastructure.persistence.matricula;

import br.org.semeando.digital.domain.matricula.DocumentoMatricula;
import br.org.semeando.digital.domain.matricula.TipoDocumento;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;

@Embeddable
class DocumentoMatriculaEmbeddable {

    @Enumerated(EnumType.STRING)
    private TipoDocumento tipo;
    private String arquivo;
    private LocalDate entregueEm;

    protected DocumentoMatriculaEmbeddable() {
    }

    static DocumentoMatriculaEmbeddable de(DocumentoMatricula d) {
        DocumentoMatriculaEmbeddable e = new DocumentoMatriculaEmbeddable();
        e.tipo = d.tipo();
        e.arquivo = d.arquivo();
        e.entregueEm = d.entregueEm();
        return e;
    }

    DocumentoMatricula paraDominio() {
        return new DocumentoMatricula(tipo, arquivo, entregueEm);
    }
}

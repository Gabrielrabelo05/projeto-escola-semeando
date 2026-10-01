package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import br.org.semeando.digital.domain.pedagogico.Presenca;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
class PresencaEmbeddable {

    private UUID alunoId;
    private boolean presente;

    protected PresencaEmbeddable() {
    }

    static PresencaEmbeddable de(Presenca p) {
        PresencaEmbeddable e = new PresencaEmbeddable();
        e.alunoId = p.alunoId();
        e.presente = p.presente();
        return e;
    }

    Presenca paraDominio() {
        return new Presenca(alunoId, presente);
    }
}

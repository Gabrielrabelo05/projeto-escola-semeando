package br.org.semeando.digital.infrastructure.persistence.obrigacao;

import br.org.semeando.digital.domain.obrigacao.ItemChecklist;
import jakarta.persistence.Embeddable;

@Embeddable
class ItemChecklistEmbeddable {

    private String descricao;
    private boolean concluido;

    protected ItemChecklistEmbeddable() {
    }

    static ItemChecklistEmbeddable de(ItemChecklist item) {
        ItemChecklistEmbeddable e = new ItemChecklistEmbeddable();
        e.descricao = item.descricao();
        e.concluido = item.concluido();
        return e;
    }

    ItemChecklist paraDominio() {
        return new ItemChecklist(descricao, concluido);
    }
}

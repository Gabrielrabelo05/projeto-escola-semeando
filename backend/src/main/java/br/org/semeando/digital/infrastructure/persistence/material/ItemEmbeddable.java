package br.org.semeando.digital.infrastructure.persistence.material;

import br.org.semeando.digital.domain.material.ItemMaterial;
import br.org.semeando.digital.domain.material.TipoItem;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
class ItemEmbeddable {

    private String descricao;
    private int quantidade;
    @Enumerated(EnumType.STRING)
    private TipoItem tipo;
    private String linkCompra;

    protected ItemEmbeddable() {
    }

    static ItemEmbeddable de(ItemMaterial i) {
        ItemEmbeddable e = new ItemEmbeddable();
        e.descricao = i.descricao();
        e.quantidade = i.quantidade();
        e.tipo = i.tipo();
        e.linkCompra = i.linkCompra();
        return e;
    }

    ItemMaterial paraDominio() {
        return new ItemMaterial(descricao, quantidade, tipo, linkCompra);
    }
}

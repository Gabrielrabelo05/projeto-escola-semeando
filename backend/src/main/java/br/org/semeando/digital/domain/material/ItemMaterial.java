package br.org.semeando.digital.domain.material;

import br.org.semeando.digital.domain.shared.Validacoes;

/** Item da lista. Livros podem ter link para loja externa (RF-05 – somente leitura). */
public record ItemMaterial(String descricao, int quantidade, TipoItem tipo, String linkCompra) {

    public ItemMaterial {
        Validacoes.obrigatorio(descricao, "descrição do item");
        Validacoes.positivo(quantidade, "quantidade");
        Validacoes.obrigatorio(tipo, "tipo do item");
    }
}

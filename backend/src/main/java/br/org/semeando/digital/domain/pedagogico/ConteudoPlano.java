package br.org.semeando.digital.domain.pedagogico;

import br.org.semeando.digital.domain.shared.Validacoes;

public record ConteudoPlano(String objetivo, String conteudo, String metodologia, String avaliacao, String recursos) {

    public ConteudoPlano {
        Validacoes.obrigatorio(objetivo, "objetivo");
        Validacoes.obrigatorio(conteudo, "conteúdo");
        Validacoes.obrigatorio(metodologia, "metodologia");
    }
}

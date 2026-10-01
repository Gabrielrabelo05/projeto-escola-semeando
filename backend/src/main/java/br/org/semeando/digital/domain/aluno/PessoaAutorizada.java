package br.org.semeando.digital.domain.aluno;

import br.org.semeando.digital.domain.shared.Validacoes;

/** Pessoa autorizada a buscar a criança na escola (cláusula contratual obrigatória). */
public record PessoaAutorizada(String nome, String documento, String parentesco, String telefone) {

    public PessoaAutorizada {
        Validacoes.obrigatorio(nome, "nome do autorizado");
        Validacoes.obrigatorio(documento, "documento do autorizado");
        Validacoes.obrigatorio(parentesco, "parentesco do autorizado");
    }
}

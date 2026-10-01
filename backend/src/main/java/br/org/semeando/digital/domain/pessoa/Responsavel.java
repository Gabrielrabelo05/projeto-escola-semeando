package br.org.semeando.digital.domain.pessoa;

import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.util.UUID;

public class Responsavel extends Pessoa {

    private String parentesco;

    public Responsavel(UUID id, String nome, Cpf cpf, Contato contato, String parentesco, boolean ativo) {
        super(id, nome, cpf, contato, ativo);
        this.parentesco = Validacoes.obrigatorio(parentesco, "parentesco");
    }

    public static Responsavel novo(String nome, Cpf cpf, Contato contato, String parentesco) {
        return new Responsavel(UUID.randomUUID(), nome, cpf, contato, parentesco, true);
    }

    public void atualizar(String nome, Contato contato, String parentesco) {
        atualizarDadosPessoais(nome, contato);
        this.parentesco = Validacoes.obrigatorio(parentesco, "parentesco");
    }

    public String parentesco() {
        return parentesco;
    }
}

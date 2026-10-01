package br.org.semeando.digital.domain.pessoa;

import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.util.UUID;

/** Base para todas as pessoas físicas cadastradas (responsáveis, professores e funcionários). */
public abstract class Pessoa {

    private final UUID id;
    private String nome;
    private final Cpf cpf;
    private Contato contato;
    private boolean ativo;

    protected Pessoa(UUID id, String nome, Cpf cpf, Contato contato, boolean ativo) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.nome = Validacoes.obrigatorio(nome, "nome");
        this.cpf = Validacoes.obrigatorio(cpf, "CPF");
        this.contato = contato == null ? Contato.vazio() : contato;
        this.ativo = ativo;
    }

    protected void atualizarDadosPessoais(String nome, Contato contato) {
        this.nome = Validacoes.obrigatorio(nome, "nome");
        this.contato = contato == null ? Contato.vazio() : contato;
    }

    /** RF-37: registros nunca são excluídos, apenas inativados. */
    public void inativar() {
        this.ativo = false;
    }

    public UUID id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public Cpf cpf() {
        return cpf;
    }

    public Contato contato() {
        return contato;
    }

    public boolean ativo() {
        return ativo;
    }
}

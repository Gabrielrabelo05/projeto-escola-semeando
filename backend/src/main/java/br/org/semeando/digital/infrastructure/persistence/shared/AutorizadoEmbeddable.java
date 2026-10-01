package br.org.semeando.digital.infrastructure.persistence.shared;

import br.org.semeando.digital.domain.aluno.PessoaAutorizada;
import jakarta.persistence.Embeddable;

@Embeddable
public class AutorizadoEmbeddable {

    private String nome;
    private String documento;
    private String parentesco;
    private String telefone;

    protected AutorizadoEmbeddable() {
    }

    public static AutorizadoEmbeddable de(PessoaAutorizada autorizado) {
        AutorizadoEmbeddable e = new AutorizadoEmbeddable();
        e.nome = autorizado.nome();
        e.documento = autorizado.documento();
        e.parentesco = autorizado.parentesco();
        e.telefone = autorizado.telefone();
        return e;
    }

    public PessoaAutorizada paraDominio() {
        return new PessoaAutorizada(nome, documento, parentesco, telefone);
    }
}

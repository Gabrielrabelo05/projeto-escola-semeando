package br.org.semeando.digital.domain.usuario;

import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.util.UUID;

public class Usuario {

    private final UUID id;
    private String nome;
    private final String email;
    private String senhaHash;
    private Perfil perfil;
    private final Cpf cpf;
    private boolean ativo;

    public Usuario(UUID id, String nome, String email, String senhaHash, Perfil perfil, Cpf cpf, boolean ativo) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.nome = Validacoes.obrigatorio(nome, "nome");
        this.email = Validacoes.obrigatorio(email, "e-mail").toLowerCase();
        this.senhaHash = Validacoes.obrigatorio(senhaHash, "senha");
        this.perfil = Validacoes.obrigatorio(perfil, "perfil");
        this.cpf = cpf;
        this.ativo = ativo;
    }

    public static Usuario novo(String nome, String email, String senhaHash, Perfil perfil, Cpf cpf) {
        return new Usuario(UUID.randomUUID(), nome, email, senhaHash, perfil, cpf, true);
    }

    public void alterarPerfil(Perfil perfil) {
        this.perfil = Validacoes.obrigatorio(perfil, "perfil");
    }

    public void alterarSenha(String senhaHash) {
        this.senhaHash = Validacoes.obrigatorio(senhaHash, "senha");
    }

    public void inativar() {
        this.ativo = false;
    }

    public UUID id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public String email() {
        return email;
    }

    public String senhaHash() {
        return senhaHash;
    }

    public Perfil perfil() {
        return perfil;
    }

    public Cpf cpf() {
        return cpf;
    }

    public boolean ativo() {
        return ativo;
    }
}

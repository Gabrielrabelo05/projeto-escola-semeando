package br.org.semeando.digital.domain.usuario;

import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class Usuario {

    private final UUID id;
    private String nome;
    private final String email;
    private String senhaHash;
    private Perfil perfil;
    private final Cpf cpf;
    private boolean ativo;
    private Instant sessoesValidasDesde;

    public Usuario(UUID id, String nome, String email, String senhaHash, Perfil perfil, Cpf cpf, boolean ativo,
            Instant sessoesValidasDesde) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.nome = Validacoes.obrigatorio(nome, "nome");
        this.email = Validacoes.obrigatorio(email, "e-mail").toLowerCase();
        this.senhaHash = senhaHash;
        this.perfil = Validacoes.obrigatorio(perfil, "perfil");
        this.cpf = cpf;
        this.ativo = ativo;
        this.sessoesValidasDesde = sessoesValidasDesde;
    }

    public static Usuario novo(String nome, String email, String senhaHash, Perfil perfil, Cpf cpf) {
        return new Usuario(UUID.randomUUID(), nome, email, senhaHash, perfil, cpf, true, null);
    }

    /** Usuário criado por convite: só entra no sistema depois de definir a própria senha. */
    public static Usuario convidado(String nome, String email, Perfil perfil, Cpf cpf) {
        return new Usuario(UUID.randomUUID(), nome, email, null, perfil, cpf, true, null);
    }

    public boolean possuiSenha() {
        return senhaHash != null;
    }

    public void alterarPerfil(Perfil perfil) {
        this.perfil = Validacoes.obrigatorio(perfil, "perfil");
    }

    public void alterarSenha(String senhaHash) {
        this.senhaHash = Validacoes.obrigatorio(senhaHash, "senha");
    }

    /** Inativar derruba na hora todas as sessões abertas. */
    public void inativar(Instant agora) {
        this.ativo = false;
        encerrarSessoes(agora);
    }

    /** Tokens emitidos antes deste instante deixam de valer (inativação, troca de senha, novo link). */
    public void encerrarSessoes(Instant agora) {
        this.sessoesValidasDesde = agora.truncatedTo(ChronoUnit.SECONDS);
    }

    /** O token só vale se o usuário continua ativo, com senha, e foi emitido depois do último encerramento. */
    public boolean aceitaSessaoEmitidaEm(Instant emitidoEm) {
        return ativo && possuiSenha() && (sessoesValidasDesde == null || !emitidoEm.isBefore(sessoesValidasDesde));
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

    public Instant sessoesValidasDesde() {
        return sessoesValidasDesde;
    }
}

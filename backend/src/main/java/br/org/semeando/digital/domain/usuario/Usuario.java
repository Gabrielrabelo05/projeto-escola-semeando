package br.org.semeando.digital.domain.usuario;

import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
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
    private DoisFatores doisFatores;

    public Usuario(UUID id, String nome, String email, String senhaHash, Perfil perfil, Cpf cpf, boolean ativo,
            Instant sessoesValidasDesde, DoisFatores doisFatores) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.nome = Validacoes.obrigatorio(nome, "nome");
        this.email = Validacoes.obrigatorio(email, "e-mail").toLowerCase();
        this.senhaHash = senhaHash;
        this.perfil = Validacoes.obrigatorio(perfil, "perfil");
        this.cpf = cpf;
        this.ativo = ativo;
        this.sessoesValidasDesde = sessoesValidasDesde;
        this.doisFatores = doisFatores == null ? DoisFatores.DESLIGADO : doisFatores;
    }

    public static Usuario novo(String nome, String email, String senhaHash, Perfil perfil, Cpf cpf) {
        return new Usuario(UUID.randomUUID(), nome, email, senhaHash, perfil, cpf, true, null, null);
    }

    /** Usuário criado por convite: só entra no sistema depois de definir a própria senha. */
    public static Usuario convidado(String nome, String email, Perfil perfil, Cpf cpf) {
        return new Usuario(UUID.randomUUID(), nome, email, null, perfil, cpf, true, null, null);
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

    /** Novo segredo, ainda inativo: só passa a valer depois que o primeiro código do aplicativo confere. */
    public void prepararDoisFatores(String segredo) {
        if (doisFatores.ativo()) {
            throw new DomainException("DOIS_FATORES_ATIVO",
                    "A verificação em duas etapas já está ativa. Desative-a antes de trocar de aparelho.");
        }
        doisFatores = new DoisFatores(Validacoes.obrigatorio(segredo, "segredo"), false, null, List.of());
    }

    /** Ativa com o passo do primeiro código conferido e encerra as outras sessões. */
    public void ativarDoisFatores(long passo, List<String> codigosRecuperacao, Instant agora) {
        if (doisFatores.ativo() || doisFatores.segredo() == null) {
            throw new DomainException("DOIS_FATORES_NAO_PREPARADO",
                    "Gere o QR Code da verificação em duas etapas antes de ativá-la.");
        }
        doisFatores = new DoisFatores(doisFatores.segredo(), true, passo, codigosRecuperacao);
        encerrarSessoes(agora);
    }

    /** Desativar (ou o link de acesso, que recupera quem perdeu o celular) também encerra as sessões. */
    public void desativarDoisFatores(Instant agora) {
        doisFatores = DoisFatores.DESLIGADO;
        encerrarSessoes(agora);
    }

    /**
     * Confere o código do aplicativo contra o segredo (ativo ou em configuração). Um código já usado é recusado,
     * para que alguém que o tenha visto não consiga reutilizá-lo nos mesmos 30 segundos.
     */
    public OptionalLong conferirCodigoTotp(String codigo, Instant agora) {
        if (doisFatores.segredo() == null) {
            return OptionalLong.empty();
        }
        OptionalLong passo = Totp.passoQueConfere(doisFatores.segredo(), codigo, agora);
        Long ultimo = doisFatores.ultimoPasso();
        if (passo.isEmpty() || (ultimo != null && passo.getAsLong() <= ultimo)) {
            return OptionalLong.empty();
        }
        if (doisFatores.ativo()) {
            doisFatores = new DoisFatores(doisFatores.segredo(), true, passo.getAsLong(),
                    doisFatores.codigosRecuperacao());
        }
        return passo;
    }

    /** Cada código de recuperação vale uma única vez. */
    public boolean usarCodigoRecuperacao(String hash) {
        if (!doisFatores.ativo() || !doisFatores.codigosRecuperacao().contains(hash)) {
            return false;
        }
        List<String> restantes = new ArrayList<>(doisFatores.codigosRecuperacao());
        restantes.remove(hash);
        doisFatores = new DoisFatores(doisFatores.segredo(), true, doisFatores.ultimoPasso(), restantes);
        return true;
    }

    public boolean doisFatoresAtivo() {
        return doisFatores.ativo();
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

    public DoisFatores doisFatores() {
        return doisFatores;
    }
}

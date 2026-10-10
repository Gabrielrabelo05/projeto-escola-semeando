package br.org.semeando.digital.domain.usuario;

import java.util.Set;

/**
 * RNF-01: perfis que só entram com a verificação em duas etapas (por padrão, Administrador). Os demais podem
 * ativá-la por conta própria; uma vez ativa, ela passa a ser exigida em todo login.
 */
public record PoliticaDoisFatores(Set<Perfil> obrigatorioPara) {

    public PoliticaDoisFatores {
        obrigatorioPara = obrigatorioPara == null ? Set.of() : Set.copyOf(obrigatorioPara);
    }

    public boolean obrigatorio(Perfil perfil) {
        return obrigatorioPara.contains(perfil);
    }

    /** A sessão desse usuário só vale se o segundo fator foi conferido no login. */
    public boolean exige(Usuario usuario) {
        return usuario.doisFatoresAtivo() || obrigatorio(usuario.perfil());
    }
}

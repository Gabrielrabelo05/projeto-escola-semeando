package br.org.semeando.digital.application.shared;

import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.usuario.Perfil;
import java.util.UUID;

/** Usuário autenticado que executa o caso de uso (usado em auditoria e regras de propriedade). */
public record Ator(UUID usuarioId, String nome, Perfil perfil, Cpf cpf) {

    public static Ator sistema() {
        return new Ator(null, "sistema", Perfil.ADMIN, null);
    }

    public static Ator anonimo() {
        return new Ator(null, "anonimo", Perfil.RESPONSAVEL, null);
    }

    public boolean possuiPerfil(Perfil... perfis) {
        for (Perfil p : perfis) {
            if (perfil == p) {
                return true;
            }
        }
        return false;
    }

    public boolean responsavel() {
        return perfil == Perfil.RESPONSAVEL;
    }
}

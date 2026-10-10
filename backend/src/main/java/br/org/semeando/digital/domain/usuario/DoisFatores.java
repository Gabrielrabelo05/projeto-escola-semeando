package br.org.semeando.digital.domain.usuario;

import java.util.List;

/**
 * Estado do segundo fator (RNF-01) de um usuário.
 *
 * @param segredo segredo TOTP em Base32 (cifrado em repouso); existe também durante a configuração
 * @param ativo o login exige o código do aplicativo
 * @param ultimoPasso último passo TOTP aceito: o mesmo código não pode ser usado duas vezes
 * @param codigosRecuperacao hashes SHA-256 dos códigos de recuperação ainda não usados
 */
public record DoisFatores(String segredo, boolean ativo, Long ultimoPasso, List<String> codigosRecuperacao) {

    public static final DoisFatores DESLIGADO = new DoisFatores(null, false, null, List.of());

    public DoisFatores {
        codigosRecuperacao = codigosRecuperacao == null ? List.of() : List.copyOf(codigosRecuperacao);
    }
}

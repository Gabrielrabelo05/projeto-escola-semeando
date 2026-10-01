package br.org.semeando.digital.domain.shared;

/**
 * RN-09: a identidade legal da instituição é única em todo o sistema.
 * Qualquer configuração ou documento que divirja destes dados é bloqueado.
 */
public final class IdentidadeInstitucional {

    public static final String NOME = "Centro Educacional Semeando / Instituto Educacional Semeando";
    public static final String CNPJ = "31.486.754/0001-27";

    private IdentidadeInstitucional() {
    }

    public static void validar(String nome, String cnpj) {
        if (!NOME.equals(nome) || !CNPJ.equals(cnpj)) {
            throw new RegraNegocioVioladaException("RN-09",
                    "Identidade institucional divergente. Esperado: " + NOME + " - CNPJ " + CNPJ);
        }
    }

    public static String cabecalho() {
        return NOME + " - CNPJ " + CNPJ;
    }
}

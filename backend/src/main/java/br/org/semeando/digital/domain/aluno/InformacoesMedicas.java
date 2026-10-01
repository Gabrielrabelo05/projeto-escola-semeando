package br.org.semeando.digital.domain.aluno;

/** Dados de saúde sensíveis (LGPD Art. 14) – persistidos criptografados. */
public record InformacoesMedicas(String alergias, String tipoSanguineo, String necessidadesEspeciais) {

    public static InformacoesMedicas naoInformadas() {
        return new InformacoesMedicas(null, null, null);
    }
}

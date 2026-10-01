package br.org.semeando.digital.infrastructure.seed;

/** Gera CPFs sintéticos válidos (dígitos verificadores corretos) a partir de uma base de 9 dígitos – RN-08. */
final class CpfSintetico {

    private CpfSintetico() {
    }

    static String de(String base9) {
        String cpf = base9 + digito(base9, 10);
        return cpf + digito(cpf, 11);
    }

    private static int digito(String parcial, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < parcial.length(); i++) {
            soma += (parcial.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}

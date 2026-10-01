package br.org.semeando.digital.domain.shared;

/** CPF validado pelos dígitos verificadores, armazenado apenas com números. */
public record Cpf(String numero) {

    public Cpf {
        Validacoes.obrigatorio(numero, "CPF");
        numero = numero.replaceAll("\\D", "");
        if (!valido(numero)) {
            throw new DomainException("CPF_INVALIDO", "CPF inválido");
        }
    }

    public static Cpf de(String numero) {
        return new Cpf(numero);
    }

    public String formatado() {
        return numero.substring(0, 3) + "." + numero.substring(3, 6) + "." + numero.substring(6, 9) + "-"
                + numero.substring(9);
    }

    private static boolean valido(String cpf) {
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }
        return digito(cpf, 9) == cpf.charAt(9) - '0' && digito(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int digito(String cpf, int posicao) {
        int soma = 0;
        for (int i = 0; i < posicao; i++) {
            soma += (cpf.charAt(i) - '0') * (posicao + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    @Override
    public String toString() {
        return formatado();
    }
}

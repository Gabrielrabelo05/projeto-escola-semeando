package br.org.semeando.digital.domain.pessoa;

public record Contato(String email, String telefone) {

    public static Contato vazio() {
        return new Contato(null, null);
    }

    public boolean possuiEmail() {
        return email != null && !email.isBlank();
    }
}

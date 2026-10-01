package br.org.semeando.digital.infrastructure.persistence.shared;

/** Normaliza termos de busca textual. */
public final class Termos {

    private Termos() {
    }

    public static String apenasDigitos(String termo) {
        String digitos = termo.replaceAll("\\D", "");
        return digitos.isEmpty() ? "__sem_digitos__" : digitos;
    }
}

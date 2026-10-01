package br.org.semeando.digital.domain.shared;

/** Violação de invariante ou regra de negócio do domínio. */
public class DomainException extends RuntimeException {

    private final String codigo;

    public DomainException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}

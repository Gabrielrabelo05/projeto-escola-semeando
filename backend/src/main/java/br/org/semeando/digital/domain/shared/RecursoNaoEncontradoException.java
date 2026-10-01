package br.org.semeando.digital.domain.shared;

public class RecursoNaoEncontradoException extends DomainException {

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super("NAO_ENCONTRADO", recurso + " não encontrado(a): " + id);
    }
}

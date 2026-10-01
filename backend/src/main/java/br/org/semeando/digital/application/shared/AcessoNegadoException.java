package br.org.semeando.digital.application.shared;

import br.org.semeando.digital.domain.shared.DomainException;

public class AcessoNegadoException extends DomainException {

    public AcessoNegadoException(String mensagem) {
        super("ACESSO_NEGADO", mensagem);
    }
}

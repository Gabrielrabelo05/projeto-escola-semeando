package br.org.semeando.digital.domain.shared;

/** Violação de uma regra de negócio numerada (RN-xx) do levantamento de requisitos. */
public class RegraNegocioVioladaException extends DomainException {

    public RegraNegocioVioladaException(String regra, String mensagem) {
        super(regra, mensagem);
    }
}

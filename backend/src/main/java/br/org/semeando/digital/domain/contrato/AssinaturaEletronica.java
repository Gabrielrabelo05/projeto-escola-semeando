package br.org.semeando.digital.domain.contrato;

import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.Instant;

/** RF-15: evidências da assinatura eletrônica – data/hora, IP do dispositivo e hash do documento assinado. */
public record AssinaturaEletronica(String assinante, Instant assinadoEm, String enderecoIp, String hashDocumento) {

    public AssinaturaEletronica {
        Validacoes.obrigatorio(assinante, "assinante");
        Validacoes.obrigatorio(assinadoEm, "data da assinatura");
        Validacoes.obrigatorio(enderecoIp, "endereço IP");
        Validacoes.obrigatorio(hashDocumento, "hash do documento");
    }
}

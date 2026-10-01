package br.org.semeando.digital.presentation.rest.shared;

import java.time.Instant;
import java.util.Map;

public record ErroResponse(String codigo, String mensagem, Map<String, String> campos, Instant timestamp) {

    public static ErroResponse de(String codigo, String mensagem) {
        return new ErroResponse(codigo, mensagem, Map.of(), Instant.now());
    }
}

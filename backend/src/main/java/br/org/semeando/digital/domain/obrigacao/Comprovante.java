package br.org.semeando.digital.domain.obrigacao;

import java.time.Instant;

/** RF-53: evidência digital do cumprimento da obrigação. */
public record Comprovante(String nomeArquivo, String caminho, Instant enviadoEm, String enviadoPor) {
}

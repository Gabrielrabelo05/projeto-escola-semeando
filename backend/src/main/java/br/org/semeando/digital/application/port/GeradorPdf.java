package br.org.semeando.digital.application.port;

import java.util.List;

/** Porta de saída para geração de PDFs simples (RF-22). */
public interface GeradorPdf {

    byte[] gerar(String titulo, List<String> paragrafos);
}

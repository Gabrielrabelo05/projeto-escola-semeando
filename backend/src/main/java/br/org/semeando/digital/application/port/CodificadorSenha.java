package br.org.semeando.digital.application.port;

public interface CodificadorSenha {

    String codificar(String senha);

    boolean confere(String senha, String hash);
}

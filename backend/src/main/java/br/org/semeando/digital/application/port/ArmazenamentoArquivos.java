package br.org.semeando.digital.application.port;

/** Porta de saída para armazenamento de documentos digitalizados e comprovantes. */
public interface ArmazenamentoArquivos {

    String salvar(String pasta, String nomeOriginal, byte[] conteudo);

    byte[] ler(String caminho);
}

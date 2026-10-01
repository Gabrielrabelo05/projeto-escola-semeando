package br.org.semeando.digital.infrastructure.persistence.obrigacao;

import br.org.semeando.digital.domain.obrigacao.Comprovante;
import jakarta.persistence.Embeddable;
import java.time.Instant;

@Embeddable
class ComprovanteEmbeddable {

    private String nomeArquivo;
    private String caminho;
    private Instant enviadoEm;
    private String enviadoPor;

    protected ComprovanteEmbeddable() {
    }

    static ComprovanteEmbeddable de(Comprovante c) {
        ComprovanteEmbeddable e = new ComprovanteEmbeddable();
        e.nomeArquivo = c.nomeArquivo();
        e.caminho = c.caminho();
        e.enviadoEm = c.enviadoEm();
        e.enviadoPor = c.enviadoPor();
        return e;
    }

    Comprovante paraDominio() {
        return new Comprovante(nomeArquivo, caminho, enviadoEm, enviadoPor);
    }
}

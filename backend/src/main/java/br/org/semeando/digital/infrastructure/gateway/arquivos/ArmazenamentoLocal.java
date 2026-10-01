package br.org.semeando.digital.infrastructure.gateway.arquivos;

import br.org.semeando.digital.application.port.ArmazenamentoArquivos;
import br.org.semeando.digital.domain.shared.DomainException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Armazena arquivos no disco local, sob um diretório raiz configurável. */
@Component
class ArmazenamentoLocal implements ArmazenamentoArquivos {

    private final Path raiz;

    ArmazenamentoLocal(@Value("${semeando.arquivos.diretorio}") String diretorio) {
        this.raiz = Path.of(diretorio).toAbsolutePath().normalize();
    }

    @Override
    public String salvar(String pasta, String nomeOriginal, byte[] conteudo) {
        String nomeSeguro = UUID.randomUUID() + "-" + nomeOriginal.replaceAll("[^A-Za-z0-9._-]", "_");
        Path destino = resolver(pasta + "/" + nomeSeguro);
        try {
            Files.createDirectories(destino.getParent());
            Files.write(destino, conteudo);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar arquivo", e);
        }
        return raiz.relativize(destino).toString();
    }

    @Override
    public byte[] ler(String caminho) {
        try {
            return Files.readAllBytes(resolver(caminho));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler arquivo", e);
        }
    }

    /** Impede path traversal ("../") para fora do diretório raiz. */
    private Path resolver(String relativo) {
        Path caminho = raiz.resolve(relativo).normalize();
        if (!caminho.startsWith(raiz)) {
            throw new DomainException("CAMINHO_INVALIDO", "Caminho de arquivo inválido");
        }
        return caminho;
    }
}

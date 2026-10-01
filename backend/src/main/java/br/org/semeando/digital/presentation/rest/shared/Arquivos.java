package br.org.semeando.digital.presentation.rest.shared;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public final class Arquivos {

    private Arquivos() {
    }

    public static byte[] bytes(MultipartFile arquivo) {
        try {
            return arquivo.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ResponseEntity<byte[]> download(byte[] conteudo, String nome, MediaType tipo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nome).build()
                        .toString())
                .contentType(tipo)
                .body(conteudo);
    }
}

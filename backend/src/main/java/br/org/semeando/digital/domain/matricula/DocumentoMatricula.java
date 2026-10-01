package br.org.semeando.digital.domain.matricula;

import java.time.LocalDate;

/** Item do checklist de documentos de uma matrícula. */
public record DocumentoMatricula(TipoDocumento tipo, String arquivo, LocalDate entregueEm) {

    public static DocumentoMatricula pendente(TipoDocumento tipo) {
        return new DocumentoMatricula(tipo, null, null);
    }

    public boolean entregue() {
        return entregueEm != null;
    }

    /** RF-08: documento entregue, mas cuja validade expirou, volta a ser pendência. */
    public boolean vigenteEm(LocalDate data) {
        if (!entregue()) {
            return false;
        }
        return tipo.validadeDias().map(dias -> !entregueEm.plusDays(dias).isBefore(data)).orElse(true);
    }
}

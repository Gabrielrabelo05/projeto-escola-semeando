package br.org.semeando.digital.infrastructure.gateway.pdf;

import br.org.semeando.digital.application.port.GeradorPdf;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class OpenPdfGerador implements GeradorPdf {

    @Override
    public byte[] gerar(String titulo, List<String> paragrafos) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 48, 48, 48, 48);
        PdfWriter.getInstance(documento, saida);
        documento.open();
        documento.add(new Paragraph(titulo, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.BOLD)));
        Font corpo = FontFactory.getFont(FontFactory.HELVETICA, 11);
        paragrafos.forEach(texto -> documento.add(new Paragraph(texto, corpo)));
        documento.close();
        return saida.toByteArray();
    }
}

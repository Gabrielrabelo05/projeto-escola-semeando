package br.org.semeando.digital.infrastructure.persistence.shared;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Aplica {@link CifradorDados} de forma transparente às colunas marcadas com @Convert. */
@Converter
public class DadoSensivelConverter implements AttributeConverter<String, String> {

    private final CifradorDados cifrador;

    public DadoSensivelConverter(CifradorDados cifrador) {
        this.cifrador = cifrador;
    }

    @Override
    public String convertToDatabaseColumn(String atributo) {
        return cifrador.cifrar(atributo);
    }

    @Override
    public String convertToEntityAttribute(String coluna) {
        return cifrador.decifrar(coluna);
    }
}

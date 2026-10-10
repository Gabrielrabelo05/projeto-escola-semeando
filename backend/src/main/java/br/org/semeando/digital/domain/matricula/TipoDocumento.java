package br.org.semeando.digital.domain.matricula;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * RN-05 / RF-06: documentos exigidos para matrícula, conforme o Anexo didático da escola (mesma lista para a
 * Educação Infantil e o Ensino Fundamental). A declaração de adimplência só é exigida de alunos vindos de outra
 * instituição particular. A validade (em dias) indica documentos a reapresentar na renovação (RF-08).
 */
public enum TipoDocumento {
    DOCUMENTO_RESPONSAVEL("Cópia da identidade e CPF dos pais ou responsáveis", null, false),
    DOCUMENTO_ALUNO("Cópia da certidão de nascimento ou RG e CPF da criança", null, false),
    CARTEIRA_VACINACAO("Cópia da carteira de vacinação", 365, false),
    COMPROVANTE_RESIDENCIA("Comprovante de residência", 90, false),
    DECLARACAO_ADIMPLENCIA("Declaração de adimplência (alunos de outras instituições particulares)", null, true),
    FOTOS_3X4("2 fotos 3x4", 365, false),
    DOCUMENTO_AUTORIZADOS("Cópia do documento de identificação das pessoas autorizadas a buscar a criança", null,
            false);

    private final String descricao;
    private final Integer validadeDias;
    private final boolean somenteVindoDeEscolaParticular;

    TipoDocumento(String descricao, Integer validadeDias, boolean somenteVindoDeEscolaParticular) {
        this.descricao = descricao;
        this.validadeDias = validadeDias;
        this.somenteVindoDeEscolaParticular = somenteVindoDeEscolaParticular;
    }

    public static List<TipoDocumento> exigidos(boolean vindoDeEscolaParticular) {
        return Arrays.stream(values())
                .filter(tipo -> vindoDeEscolaParticular || !tipo.somenteVindoDeEscolaParticular)
                .toList();
    }

    public String descricao() {
        return descricao;
    }

    public Optional<Integer> validadeDias() {
        return Optional.ofNullable(validadeDias);
    }
}

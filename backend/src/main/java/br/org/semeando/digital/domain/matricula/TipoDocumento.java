package br.org.semeando.digital.domain.matricula;

import static br.org.semeando.digital.domain.turma.NivelEnsino.EDUCACAO_INFANTIL;
import static br.org.semeando.digital.domain.turma.NivelEnsino.ENSINO_FUNDAMENTAL;

import br.org.semeando.digital.domain.turma.NivelEnsino;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * RN-05 / RF-06: documentos exigidos para matrícula. Cada nível de ensino possui um checklist de 7 documentos.
 * A validade (em dias) indica documentos que precisam ser reapresentados na renovação (RF-08).
 */
public enum TipoDocumento {
    CERTIDAO_NASCIMENTO("Certidão de nascimento", null, EDUCACAO_INFANTIL, ENSINO_FUNDAMENTAL),
    DOCUMENTO_RESPONSAVEL("RG e CPF do responsável", null, EDUCACAO_INFANTIL, ENSINO_FUNDAMENTAL),
    COMPROVANTE_RESIDENCIA("Comprovante de residência", 90, EDUCACAO_INFANTIL, ENSINO_FUNDAMENTAL),
    CARTAO_VACINACAO("Cartão de vacinação atualizado", 365, EDUCACAO_INFANTIL, ENSINO_FUNDAMENTAL),
    FOTO_3X4("Foto 3x4 do aluno", 365, EDUCACAO_INFANTIL, ENSINO_FUNDAMENTAL),
    CARTAO_SUS("Cartão do SUS", null, EDUCACAO_INFANTIL),
    LAUDO_MEDICO("Declaração de saúde / laudo médico", 365, EDUCACAO_INFANTIL),
    DECLARACAO_TRANSFERENCIA("Declaração de transferência", null, ENSINO_FUNDAMENTAL),
    HISTORICO_ESCOLAR("Histórico escolar", null, ENSINO_FUNDAMENTAL);

    private final String descricao;
    private final Integer validadeDias;
    private final Set<NivelEnsino> niveis;

    TipoDocumento(String descricao, Integer validadeDias, NivelEnsino... niveis) {
        this.descricao = descricao;
        this.validadeDias = validadeDias;
        this.niveis = EnumSet.copyOf(Arrays.asList(niveis));
    }

    public static List<TipoDocumento> exigidosPara(NivelEnsino nivel) {
        return Arrays.stream(values()).filter(tipo -> tipo.niveis.contains(nivel)).toList();
    }

    public boolean exigidoPara(NivelEnsino nivel) {
        return niveis.contains(nivel);
    }

    public String descricao() {
        return descricao;
    }

    public Optional<Integer> validadeDias() {
        return Optional.ofNullable(validadeDias);
    }
}

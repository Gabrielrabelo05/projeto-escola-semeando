package br.org.semeando.digital.infrastructure.config;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("semeando")
public record SemeandoProperties(Instituicao instituicao, Valores valores, Seguranca seguranca, Cora cora,
        Inpc inpc, Seed seed) {

    public record Instituicao(String nome, String cnpj) {
    }

    /** Tabela oficial (Anexo didático): valor integral e com o desconto de pontualidade. */
    public record Valores(Parcela primeiroAno, Parcela segundoAoQuintoAno, int parcelasFundamental,
            BigDecimal taxaAssociadoInfantil, int parcelasAssociado, BigDecimal atividadeExtracurricular,
            int diaVencimento) {
    }

    public record Parcela(BigDecimal integral, BigDecimal comDesconto) {
    }

    public record Seguranca(String jwtSegredo, long jwtValidadeMinutos, List<String> corsOrigens) {
    }

    public record Cora(String modo, String url, String token, String webhookSegredo) {
    }

    /** {@code modo}: "ibge" consulta a série do INPC no SIDRA; "nenhum" desliga a correção monetária. */
    public record Inpc(String modo, String url) {
    }

    public record Seed(boolean habilitado) {
    }
}

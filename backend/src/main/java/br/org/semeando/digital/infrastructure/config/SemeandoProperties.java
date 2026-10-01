package br.org.semeando.digital.infrastructure.config;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("semeando")
public record SemeandoProperties(Instituicao instituicao, Valores valores, Seguranca seguranca, Cora cora,
        Seed seed) {

    public record Instituicao(String nome, String cnpj) {
    }

    public record Valores(BigDecimal mensalidadeFundamental, int parcelasFundamental,
            BigDecimal taxaAssociadoInfantil, int parcelasAssociado, BigDecimal taxaMatricula, int diaVencimento) {
    }

    public record Seguranca(String jwtSegredo, long jwtValidadeMinutos, List<String> corsOrigens) {
    }

    public record Cora(String modo, String url, String token, String webhookSegredo) {
    }

    public record Seed(boolean habilitado) {
    }
}

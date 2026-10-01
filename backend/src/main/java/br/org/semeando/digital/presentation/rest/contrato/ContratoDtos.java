package br.org.semeando.digital.presentation.rest.contrato;

import br.org.semeando.digital.domain.contrato.ContratoPrestacaoServicos;
import br.org.semeando.digital.domain.contrato.DocumentoVinculo;
import br.org.semeando.digital.domain.contrato.FichaSocio;
import br.org.semeando.digital.presentation.rest.shared.Dtos.AutorizadoDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ContratoDtos {

    private ContratoDtos() {
    }

    public record GerarContratoRequest(@NotNull UUID matriculaId, @NotNull @DecimalMin("0.01") BigDecimal valorParcela,
            @Min(1) int numeroParcelas) {
    }

    public record AssinaturaResponse(String assinante, Instant assinadoEm, String enderecoIp, String hashDocumento) {
    }

    public record ContratoResponse(UUID id, UUID numero, int versao, String tipo, UUID alunoId, UUID matriculaId,
            int anoLetivo, Instant geradoEm, String geradoPor, String conteudo, String hashDocumento, String status,
            AssinaturaResponse assinatura, BigDecimal valorParcela, Integer numeroParcelas, BigDecimal valorTotal,
            String hospitalEmergencia, List<AutorizadoDto> autorizados, String nomeSocio,
            BigDecimal valorTaxaAssociado) {

        public static ContratoResponse de(DocumentoVinculo d) {
            var a = d.assinatura();
            AssinaturaResponse assinatura = a == null ? null
                    : new AssinaturaResponse(a.assinante(), a.assinadoEm(), a.enderecoIp(), a.hashDocumento());
            BigDecimal parcela = null;
            Integer parcelas = null;
            BigDecimal total = null;
            String hospital = null;
            List<AutorizadoDto> autorizados = List.of();
            String socio = null;
            BigDecimal taxa = null;
            switch (d) {
                case ContratoPrestacaoServicos c -> {
                    parcela = c.valorParcela().valor();
                    parcelas = c.numeroParcelas();
                    total = c.valorTotal().valor();
                    hospital = c.hospitalEmergencia();
                    autorizados = c.autorizados().stream().map(AutorizadoDto::de).toList();
                }
                case FichaSocio f -> {
                    socio = f.nomeSocio();
                    taxa = f.valorTaxaAssociado().valor();
                }
            }
            return new ContratoResponse(d.id(), d.numero(), d.versao(), d.tipo().name(), d.alunoId(), d.matriculaId(),
                    d.anoLetivo(), d.geradoEm(), d.geradoPor(), d.conteudo(), d.hashDocumento(), d.status().name(),
                    assinatura, parcela, parcelas, total, hospital, autorizados, socio, taxa);
        }
    }
}

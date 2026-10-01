package br.org.semeando.digital.infrastructure.persistence.contrato;

import br.org.semeando.digital.domain.contrato.AssinaturaEletronica;
import br.org.semeando.digital.domain.contrato.ContratoPrestacaoServicos;
import br.org.semeando.digital.domain.contrato.DadosVinculo;
import br.org.semeando.digital.domain.contrato.DocumentoVinculo;
import br.org.semeando.digital.domain.contrato.FichaSocio;
import br.org.semeando.digital.domain.contrato.StatusDocumentoVinculo;
import br.org.semeando.digital.domain.contrato.TipoVinculo;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.infrastructure.persistence.shared.AutorizadoEmbeddable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Tabela única para a hierarquia selada de documentos de vínculo, discriminada por {@code tipo}. */
@Entity
@Table(name = "documentos_vinculo")
class DocumentoVinculoEntity {

    @Id
    private UUID id;
    private UUID numero;
    private int versao;
    @Enumerated(EnumType.STRING)
    private TipoVinculo tipo;
    private UUID alunoId;
    private UUID matriculaId;
    private int anoLetivo;
    private Instant geradoEm;
    private String geradoPor;
    private String conteudo;
    private String hashDocumento;
    @Enumerated(EnumType.STRING)
    private StatusDocumentoVinculo status;
    private String assinante;
    private Instant assinadoEm;
    private String assinaturaIp;
    private String assinaturaHash;
    private BigDecimal valorParcela;
    private Integer numeroParcelas;
    private String hospitalEmergencia;
    private String nomeSocio;
    private String cpfSocio;
    private Boolean aceiteEstatuto;
    private BigDecimal valorTaxaAssociado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "vinculo_autorizados", joinColumns = @JoinColumn(name = "vinculo_id"))
    @OrderColumn(name = "ordem")
    private List<AutorizadoEmbeddable> autorizados = new ArrayList<>();

    protected DocumentoVinculoEntity() {
    }

    static DocumentoVinculoEntity de(DocumentoVinculo d) {
        DocumentoVinculoEntity e = new DocumentoVinculoEntity();
        e.id = d.id();
        e.numero = d.numero();
        e.versao = d.versao();
        e.tipo = d.tipo();
        e.alunoId = d.alunoId();
        e.matriculaId = d.matriculaId();
        e.anoLetivo = d.anoLetivo();
        e.geradoEm = d.geradoEm();
        e.geradoPor = d.geradoPor();
        e.conteudo = d.conteudo();
        e.hashDocumento = d.hashDocumento();
        e.status = d.status();
        if (d.assinatura() != null) {
            e.assinante = d.assinatura().assinante();
            e.assinadoEm = d.assinatura().assinadoEm();
            e.assinaturaIp = d.assinatura().enderecoIp();
            e.assinaturaHash = d.assinatura().hashDocumento();
        }
        switch (d) {
            case ContratoPrestacaoServicos c -> {
                e.valorParcela = c.valorParcela().valor();
                e.numeroParcelas = c.numeroParcelas();
                e.hospitalEmergencia = c.hospitalEmergencia();
                e.autorizados = new ArrayList<>(c.autorizados().stream().map(AutorizadoEmbeddable::de).toList());
            }
            case FichaSocio f -> {
                e.nomeSocio = f.nomeSocio();
                e.cpfSocio = f.cpfSocio().numero();
                e.aceiteEstatuto = f.aceiteEstatuto();
                e.valorTaxaAssociado = f.valorTaxaAssociado().valor();
            }
        }
        return e;
    }

    DocumentoVinculo paraDominio() {
        AssinaturaEletronica assinatura = assinadoEm == null ? null
                : new AssinaturaEletronica(assinante, assinadoEm, assinaturaIp, assinaturaHash);
        DadosVinculo dados = new DadosVinculo(id, numero, versao, alunoId, matriculaId, anoLetivo, geradoEm,
                geradoPor, conteudo, hashDocumento, status, assinatura);
        return switch (tipo) {
            case CONTRATO_PRESTACAO_SERVICOS -> new ContratoPrestacaoServicos(dados, Dinheiro.de(valorParcela),
                    numeroParcelas, hospitalEmergencia,
                    autorizados.stream().map(AutorizadoEmbeddable::paraDominio).toList());
            case FICHA_SOCIO -> new FichaSocio(dados, nomeSocio, Cpf.de(cpfSocio), Boolean.TRUE.equals(aceiteEstatuto),
                    Dinheiro.de(valorTaxaAssociado));
        };
    }
}

package br.org.semeando.digital.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.contrato.ContratoPrestacaoServicos;
import br.org.semeando.digital.domain.contrato.DocumentoVinculo;
import br.org.semeando.digital.domain.contrato.FichaSocio;
import br.org.semeando.digital.domain.contrato.GeradorDocumentoVinculo;
import br.org.semeando.digital.domain.contrato.StatusDocumentoVinculo;
import br.org.semeando.digital.domain.contrato.TabelaValores;
import br.org.semeando.digital.domain.contrato.TipoVinculo;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.RegraNegocioVioladaException;
import br.org.semeando.digital.domain.turma.NivelEnsino;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ContratoTest {

    private final GeradorDocumentoVinculo gerador = new GeradorDocumentoVinculo(Fixtures.tabela());
    private final Responsavel responsavel = Fixtures.responsavel();
    private final Instant agora = Instant.parse("2026-03-10T12:00:00Z");

    @Test
    void rn01EducacaoInfantilGeraFichaDeSocio() {
        Aluno aluno = Fixtures.aluno(responsavel, true);
        DocumentoVinculo doc = gerador.gerarComTabelaOficial(aluno, responsavel,
                Fixtures.matricula(aluno, NivelEnsino.EDUCACAO_INFANTIL), "secretaria", agora);

        assertThat(doc).isInstanceOf(FichaSocio.class);
        assertThat(doc.tipo()).isEqualTo(TipoVinculo.FICHA_SOCIO);
        assertThatThrownBy(() -> doc.validarCompatibilidade(NivelEnsino.ENSINO_FUNDAMENTAL))
                .hasFieldOrPropertyWithValue("codigo", "RN-01");
    }

    @Test
    void rn01EnsinoFundamentalGeraContratoComValoresOficiais() {
        Aluno aluno = Fixtures.aluno(responsavel, true);
        DocumentoVinculo doc = gerador.gerarComTabelaOficial(aluno, responsavel,
                Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL), "secretaria", agora);

        assertThat(doc).isInstanceOfSatisfying(ContratoPrestacaoServicos.class, contrato -> {
            assertThat(contrato.valorParcela()).isEqualTo(Dinheiro.de("375.00"));
            assertThat(contrato.numeroParcelas()).isEqualTo(11);
            assertThat(contrato.valorTotal()).isEqualTo(Dinheiro.de("4125.00"));
        });
        assertThat(doc.conteudo()).contains("31.486.754/0001-27", "Hospital Sintético", "Avó Sintética");
        assertThat(doc.hashDocumento()).hasSize(64);
    }

    @Test
    void rn02ValoresDivergentesBloqueiamGeracao() {
        Aluno aluno = Fixtures.aluno(responsavel, true);
        var matricula = Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL);

        assertThatThrownBy(() -> gerador.gerar(aluno, responsavel, matricula, Dinheiro.de("350.00"), 11, "sec", agora))
                .isInstanceOf(RegraNegocioVioladaException.class)
                .hasFieldOrPropertyWithValue("codigo", "RN-02");
        assertThatThrownBy(() -> gerador.gerar(aluno, responsavel, matricula, Dinheiro.de("375.00"), 12, "sec", agora))
                .hasFieldOrPropertyWithValue("codigo", "RN-02");
    }

    @Test
    void rn02TabelaDeValoresDiferenteDaOficialNaoPodeSerConfigurada() {
        assertThatThrownBy(() -> new TabelaValores(Dinheiro.de("400.00"), 11, Dinheiro.de("280"), 11,
                Dinheiro.de("150"), 10)).hasFieldOrPropertyWithValue("codigo", "RN-02");
    }

    @Test
    void rn06ContratoSemHospitalOuAutorizadosNaoEhGerado() {
        Aluno aluno = Fixtures.aluno(responsavel, false);
        var matricula = Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL);

        assertThatThrownBy(() -> gerador.gerarComTabelaOficial(aluno, responsavel, matricula, "sec", agora))
                .hasFieldOrPropertyWithValue("codigo", "RN-06");
    }

    @Test
    void rf15AssinaturaRegistraIpHashEData() {
        Aluno aluno = Fixtures.aluno(responsavel, true);
        DocumentoVinculo doc = gerador.gerarComTabelaOficial(aluno, responsavel,
                Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL), "sec", agora);

        doc.assinar("Maria", "10.0.0.1", agora);

        assertThat(doc.assinado()).isTrue();
        assertThat(doc.assinatura().enderecoIp()).isEqualTo("10.0.0.1");
        assertThat(doc.assinatura().hashDocumento()).isEqualTo(doc.hashDocumento());
        assertThatThrownBy(() -> doc.assinar("Maria", "10.0.0.1", agora)).isInstanceOf(DomainException.class);
    }

    @Test
    void rf16NovaVersaoIncrementaNumeroDeVersaoEExigeNovaAssinatura() {
        Aluno aluno = Fixtures.aluno(responsavel, true);
        var contrato = (ContratoPrestacaoServicos) gerador.gerarComTabelaOficial(aluno, responsavel,
                Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL), "sec", agora);
        contrato.assinar("Maria", "10.0.0.1", agora);

        var nova = contrato.novaVersao(contrato.conteudo() + "\nADENDO", "sec", agora);
        contrato.marcarComoSubstituido();

        assertThat(nova.versao()).isEqualTo(2);
        assertThat(nova.numero()).isEqualTo(contrato.numero());
        assertThat(nova.status()).isEqualTo(StatusDocumentoVinculo.AGUARDANDO_ASSINATURA);
        assertThat(nova.hashDocumento()).isNotEqualTo(contrato.hashDocumento());
        assertThat(contrato.status()).isEqualTo(StatusDocumentoVinculo.SUBSTITUIDO);
    }
}

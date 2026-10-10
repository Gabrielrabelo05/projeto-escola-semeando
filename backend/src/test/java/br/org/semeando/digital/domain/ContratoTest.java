package br.org.semeando.digital.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.contrato.ContratoPrestacaoServicos;
import br.org.semeando.digital.domain.contrato.DocumentoVinculo;
import br.org.semeando.digital.domain.contrato.FichaSocio;
import br.org.semeando.digital.domain.contrato.GeradorDocumentoVinculo;
import br.org.semeando.digital.domain.contrato.Mensalidade;
import br.org.semeando.digital.domain.contrato.StatusDocumentoVinculo;
import br.org.semeando.digital.domain.contrato.TabelaValores;
import br.org.semeando.digital.domain.contrato.TipoVinculo;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.RegraNegocioVioladaException;
import br.org.semeando.digital.domain.turma.NivelEnsino;
import br.org.semeando.digital.domain.turma.Serie;
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
                Fixtures.matricula(aluno, NivelEnsino.EDUCACAO_INFANTIL), Serie.MATERNAL_II, "secretaria", agora);

        assertThat(doc).isInstanceOfSatisfying(FichaSocio.class,
                ficha -> assertThat(ficha.valorTaxaAssociado()).isEqualTo(Dinheiro.de("125.00")));
        assertThat(doc.tipo()).isEqualTo(TipoVinculo.FICHA_SOCIO);
        assertThatThrownBy(() -> doc.validarCompatibilidade(NivelEnsino.ENSINO_FUNDAMENTAL))
                .hasFieldOrPropertyWithValue("codigo", "RN-01");
    }

    @Test
    void rn02PrimeiroAnoTemContratoDeTrezentosEDezReais() {
        Aluno aluno = Fixtures.aluno(responsavel, true);
        DocumentoVinculo doc = gerador.gerarComTabelaOficial(aluno, responsavel,
                Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL), Serie.PRIMEIRO_ANO, "secretaria", agora);

        assertThat(doc).isInstanceOfSatisfying(ContratoPrestacaoServicos.class, contrato -> {
            assertThat(contrato.valorParcela()).isEqualTo(Dinheiro.de("310.00"));
            assertThat(contrato.numeroParcelas()).isEqualTo(11);
            assertThat(contrato.valorTotal()).isEqualTo(Dinheiro.de("3410.00"));
        });
        assertThat(doc.conteudo()).contains("31.486.754/0001-27", "Hospital Sintético", "Avó Sintética",
                "1º ano", "R$ 279,00", "quinto dia útil", "INPC");
        assertThat(doc.hashDocumento()).hasSize(64);
    }

    @Test
    void rn02SegundoAoQuintoAnoTemContratoDeTrezentosESetentaECincoReais() {
        Aluno aluno = Fixtures.aluno(responsavel, true);
        var contrato = (ContratoPrestacaoServicos) gerador.gerarComTabelaOficial(aluno, responsavel,
                Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL), Serie.TERCEIRO_ANO, "secretaria", agora);

        assertThat(contrato.valorParcela()).isEqualTo(Dinheiro.de("375.00"));
        assertThat(contrato.valorTotal()).isEqualTo(Dinheiro.de("4125.00"));
        assertThat(contrato.conteudo()).contains("R$ 337,00");
    }

    @Test
    void rn02ValoresDivergentesDaSerieBloqueiamGeracao() {
        Aluno aluno = Fixtures.aluno(responsavel, true);
        var matricula = Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL);

        // Caso real do levantamento: contrato do 3º ano emitido com a parcela do 1º ano.
        assertThatThrownBy(() -> gerador.gerar(aluno, responsavel, matricula, Serie.TERCEIRO_ANO,
                Dinheiro.de("310.00"), 11, "sec", agora))
                .isInstanceOf(RegraNegocioVioladaException.class)
                .hasFieldOrPropertyWithValue("codigo", "RN-02");
        assertThatThrownBy(() -> gerador.gerar(aluno, responsavel, matricula, Serie.PRIMEIRO_ANO,
                Dinheiro.de("375.00"), 11, "sec", agora)).hasFieldOrPropertyWithValue("codigo", "RN-02");
        assertThatThrownBy(() -> gerador.gerar(aluno, responsavel, matricula, Serie.PRIMEIRO_ANO,
                Dinheiro.de("310.00"), 12, "sec", agora)).hasFieldOrPropertyWithValue("codigo", "RN-02");
    }

    @Test
    void rn02TabelaDeValoresDiferenteDaOficialNaoPodeSerConfigurada() {
        assertThatThrownBy(() -> new TabelaValores(Mensalidade.de("375.00", "337.00"),
                TabelaValores.SEGUNDO_AO_QUINTO_ANO_OFICIAL, 11, Dinheiro.de("125"), 11, Dinheiro.de("50"), 10))
                .hasFieldOrPropertyWithValue("codigo", "RN-02");
        assertThatThrownBy(() -> new TabelaValores(TabelaValores.PRIMEIRO_ANO_OFICIAL,
                Mensalidade.de("375.00", "375.00"), 11, Dinheiro.de("125"), 11, Dinheiro.de("50"), 10))
                .hasFieldOrPropertyWithValue("codigo", "RN-02");
    }

    @Test
    void rn06ContratoSemHospitalOuAutorizadosNaoEhGerado() {
        Aluno aluno = Fixtures.aluno(responsavel, false);
        var matricula = Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL);

        assertThatThrownBy(() -> gerador.gerarComTabelaOficial(aluno, responsavel, matricula, Serie.SEGUNDO_ANO,
                "sec", agora))
                .hasFieldOrPropertyWithValue("codigo", "RN-06");
    }

    @Test
    void rf15AssinaturaRegistraIpHashEData() {
        Aluno aluno = Fixtures.aluno(responsavel, true);
        DocumentoVinculo doc = gerador.gerarComTabelaOficial(aluno, responsavel,
                Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL), Serie.SEGUNDO_ANO, "sec", agora);

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
                Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL), Serie.SEGUNDO_ANO, "sec", agora);
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

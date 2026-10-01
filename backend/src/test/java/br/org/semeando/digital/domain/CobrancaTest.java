package br.org.semeando.digital.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.cobranca.Boleto;
import br.org.semeando.digital.domain.cobranca.CalculadoraEncargos;
import br.org.semeando.digital.domain.cobranca.Cobranca;
import br.org.semeando.digital.domain.cobranca.Encargos;
import br.org.semeando.digital.domain.cobranca.FormaPagamento;
import br.org.semeando.digital.domain.cobranca.Pagamento;
import br.org.semeando.digital.domain.cobranca.PoliticaEmissaoBoleto;
import br.org.semeando.digital.domain.cobranca.StatusCobranca;
import br.org.semeando.digital.domain.cobranca.TipoCobranca;
import br.org.semeando.digital.domain.contrato.DocumentoVinculo;
import br.org.semeando.digital.domain.contrato.GeradorDocumentoVinculo;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.MesReferencia;
import br.org.semeando.digital.domain.turma.NivelEnsino;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CobrancaTest {

    private static final LocalDate VENCIMENTO = LocalDate.of(2026, 3, 10);

    @Test
    void rn03SemAtrasoNaoHaEncargos() {
        assertThat(CalculadoraEncargos.calcular(Dinheiro.de("375"), VENCIMENTO, VENCIMENTO)).isEqualTo(Encargos.nenhum());
    }

    @Test
    void rn03MultaDeDoisPorCentoEJurosDiariosDeZeroVirgulaZeroTrintaETres() {
        Encargos encargos = CalculadoraEncargos.calcular(Dinheiro.de("375.00"), VENCIMENTO, VENCIMENTO.plusDays(10));

        assertThat(encargos.diasAtraso()).isEqualTo(10);
        assertThat(encargos.multa()).isEqualTo(Dinheiro.de("7.50"));
        assertThat(encargos.juros()).isEqualTo(Dinheiro.de("1.24")); // 375 * 0,00033 * 10 = 1,2375
        assertThat(encargos.total()).isEqualTo(Dinheiro.de("8.74"));
    }

    @Test
    void rf26CobrancaVencidaTemValorAtualizado() {
        Cobranca cobranca = Cobranca.emitir(UUID.randomUUID(), TipoCobranca.MENSALIDADE, MesReferencia.de("2026-03"),
                Dinheiro.de("375.00"), VENCIMENTO, new Boleto("cora-1", null, null));

        assertThat(cobranca.vencidaEm(VENCIMENTO)).isFalse();
        assertThat(cobranca.vencidaEm(VENCIMENTO.plusDays(1))).isTrue();
        assertThat(cobranca.valorAtualizadoEm(VENCIMENTO.plusDays(10))).isEqualTo(Dinheiro.de("383.74"));
    }

    @Test
    void rf28ConfirmacaoDePagamentoEhIdempotente() {
        Cobranca cobranca = Cobranca.emitir(UUID.randomUUID(), TipoCobranca.MENSALIDADE, MesReferencia.de("2026-03"),
                Dinheiro.de("375.00"), VENCIMENTO, new Boleto("cora-1", null, null));
        Pagamento pagamento = new Pagamento(VENCIMENTO, Dinheiro.de("375.00"), FormaPagamento.BOLETO, "webhook");

        assertThat(cobranca.confirmarPagamento(pagamento)).isTrue();
        assertThat(cobranca.confirmarPagamento(pagamento)).isFalse();
        assertThat(cobranca.status()).isEqualTo(StatusCobranca.PAGA);
        assertThat(cobranca.vencidaEm(VENCIMENTO.plusDays(30))).isFalse();
        assertThatThrownBy(cobranca::cancelar).hasMessageContaining("paga");
    }

    @Test
    void rn07BoletoExigeAlunoAtivoEContratoAssinado() {
        var responsavel = Fixtures.responsavel();
        Aluno aluno = Fixtures.aluno(responsavel, true);
        DocumentoVinculo contrato = new GeradorDocumentoVinculo(Fixtures.tabela()).gerarComTabelaOficial(aluno,
                responsavel, Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL), "sec", Instant.now());

        assertThat(PoliticaEmissaoBoleto.permite(aluno, Optional.of(contrato))).isFalse();
        aluno.ativar();
        assertThat(PoliticaEmissaoBoleto.permite(aluno, Optional.of(contrato))).isFalse();
        assertThat(PoliticaEmissaoBoleto.permite(aluno, Optional.empty())).isFalse();
        contrato.assinar("Maria", "127.0.0.1", Instant.now());
        assertThat(PoliticaEmissaoBoleto.permite(aluno, Optional.of(contrato))).isTrue();

        aluno.inativar();
        assertThatThrownBy(() -> PoliticaEmissaoBoleto.verificar(aluno, Optional.of(contrato)))
                .hasFieldOrPropertyWithValue("codigo", "RN-07");
    }
}

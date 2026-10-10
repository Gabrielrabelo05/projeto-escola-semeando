package br.org.semeando.digital.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.cobranca.Boleto;
import br.org.semeando.digital.domain.cobranca.CalculadoraEncargos;
import br.org.semeando.digital.domain.cobranca.Cobranca;
import br.org.semeando.digital.domain.cobranca.Encargos;
import br.org.semeando.digital.domain.cobranca.FormaPagamento;
import br.org.semeando.digital.domain.cobranca.IndiceCorrecaoMonetaria;
import br.org.semeando.digital.domain.cobranca.Pagamento;
import br.org.semeando.digital.domain.cobranca.PoliticaEmissaoBoleto;
import br.org.semeando.digital.domain.cobranca.StatusCobranca;
import br.org.semeando.digital.domain.cobranca.TipoCobranca;
import br.org.semeando.digital.domain.contrato.DocumentoVinculo;
import br.org.semeando.digital.domain.contrato.GeradorDocumentoVinculo;
import br.org.semeando.digital.domain.contrato.Mensalidade;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.MesReferencia;
import br.org.semeando.digital.domain.turma.NivelEnsino;
import br.org.semeando.digital.domain.turma.Serie;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CobrancaTest {

    private static final LocalDate VENCIMENTO = LocalDate.of(2026, 3, 10);
    private static final IndiceCorrecaoMonetaria SEM_INPC = IndiceCorrecaoMonetaria.NENHUM;

    @Test
    void rn03SemAtrasoNaoHaEncargos() {
        assertThat(CalculadoraEncargos.calcular(Dinheiro.de("375"), VENCIMENTO, VENCIMENTO, SEM_INPC))
                .isEqualTo(Encargos.nenhum());
    }

    @Test
    void rn03MultaDeDoisPorCentoEJurosDiariosDeZeroVirgulaZeroTrintaETres() {
        Encargos encargos = CalculadoraEncargos.calcular(Dinheiro.de("375.00"), VENCIMENTO, VENCIMENTO.plusDays(10),
                SEM_INPC);

        assertThat(encargos.diasAtraso()).isEqualTo(10);
        assertThat(encargos.multa()).isEqualTo(Dinheiro.de("7.50"));
        assertThat(encargos.juros()).isEqualTo(Dinheiro.de("1.24")); // 375 * 0,00033 * 10 = 1,2375
        assertThat(encargos.correcao()).isEqualTo(Dinheiro.ZERO);
        assertThat(encargos.total()).isEqualTo(Dinheiro.de("8.74"));
    }

    @Test
    void contratoNaoHaAcrescimosAteOQuintoDiaUtilDoMesDoVencimento() {
        LocalDate vencimentoNoInicioDoMes = LocalDate.of(2026, 3, 2); // segunda-feira; 5º dia útil = 06/03

        assertThat(CalculadoraEncargos.limiteSemAcrescimos(vencimentoNoInicioDoMes))
                .isEqualTo(LocalDate.of(2026, 3, 6));
        assertThat(CalculadoraEncargos.calcular(Dinheiro.de("375.00"), vencimentoNoInicioDoMes,
                LocalDate.of(2026, 3, 6), SEM_INPC)).isEqualTo(Encargos.nenhum());
        assertThat(CalculadoraEncargos.calcular(Dinheiro.de("375.00"), vencimentoNoInicioDoMes,
                LocalDate.of(2026, 3, 9), SEM_INPC).diasAtraso()).isEqualTo(7);
    }

    @Test
    void contratoAtrasoDeTrintaDiasOuMaisCorrigeOPrincipalPeloInpc() {
        IndiceCorrecaoMonetaria inpc = (inicio, fim) -> {
            assertThat(inicio).isEqualTo(YearMonth.of(2026, 4));
            assertThat(fim).isEqualTo(YearMonth.of(2026, 4));
            return new BigDecimal("0.01");
        };

        Encargos encargos = CalculadoraEncargos.calcular(Dinheiro.de("375.00"), VENCIMENTO, LocalDate.of(2026, 5, 15),
                inpc);

        assertThat(encargos.diasAtraso()).isEqualTo(66);
        assertThat(encargos.correcao()).isEqualTo(Dinheiro.de("3.75"));
        assertThat(CalculadoraEncargos.calcular(Dinheiro.de("375.00"), VENCIMENTO, VENCIMENTO.plusDays(29), inpc)
                .correcao()).isEqualTo(Dinheiro.ZERO);
    }

    @Test
    void rf26DescontoDePontualidadeAteOVencimentoEValorIntegralComEncargosDepois() {
        Cobranca cobranca = Cobranca.emitir(UUID.randomUUID(), TipoCobranca.MENSALIDADE, MesReferencia.de("2026-03"),
                Mensalidade.de("375.00", "337.00"), VENCIMENTO, new Boleto("cora-1", null, null));

        assertThat(cobranca.vencidaEm(VENCIMENTO)).isFalse();
        assertThat(cobranca.valorDevidoEm(VENCIMENTO, SEM_INPC)).isEqualTo(Dinheiro.de("337.00"));
        assertThat(cobranca.vencidaEm(VENCIMENTO.plusDays(1))).isTrue();
        assertThat(cobranca.valorDevidoEm(VENCIMENTO.plusDays(10), SEM_INPC)).isEqualTo(Dinheiro.de("383.74"));
    }

    @Test
    void taxaDeAssociadoNaoTemDescontoNemEncargosDoContrato() {
        Cobranca taxa = Cobranca.emitir(UUID.randomUUID(), TipoCobranca.TAXA_ASSOCIADO, MesReferencia.de("2026-03"),
                Mensalidade.semDesconto(Dinheiro.de("125.00")), VENCIMENTO, null);

        assertThat(taxa.boleto()).isNull();
        assertThat(taxa.valorDevidoEm(VENCIMENTO, SEM_INPC)).isEqualTo(Dinheiro.de("125.00"));
        assertThat(taxa.vencidaEm(VENCIMENTO.plusDays(10))).isTrue();
        assertThat(taxa.valorDevidoEm(VENCIMENTO.plusDays(10), SEM_INPC)).isEqualTo(Dinheiro.de("125.00"));
    }

    @Test
    void rf28ConfirmacaoDePagamentoEhIdempotente() {
        Cobranca cobranca = Cobranca.emitir(UUID.randomUUID(), TipoCobranca.MENSALIDADE, MesReferencia.de("2026-03"),
                Mensalidade.de("375.00", "337.00"), VENCIMENTO, new Boleto("cora-1", null, null));
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
                responsavel, Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL), Serie.PRIMEIRO_ANO, "sec",
                Instant.now());

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

package br.org.semeando.digital.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.StatusMatricula;
import br.org.semeando.digital.domain.matricula.TipoDocumento;
import br.org.semeando.digital.domain.matricula.TipoMatricula;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.turma.NivelEnsino;
import br.org.semeando.digital.domain.turma.Serie;
import br.org.semeando.digital.domain.turma.Turma;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MatriculaTest {

    private final Matricula matricula = Matricula.solicitar(UUID.randomUUID(), UUID.randomUUID(),
            NivelEnsino.ENSINO_FUNDAMENTAL, 2026, Fixtures.HOJE);

    @Test
    void rn05ChecklistTemSeteDocumentosDiferentesPorNivel() {
        assertThat(TipoDocumento.exigidosPara(NivelEnsino.EDUCACAO_INFANTIL)).hasSize(7)
                .contains(TipoDocumento.CARTAO_SUS).doesNotContain(TipoDocumento.HISTORICO_ESCOLAR);
        assertThat(TipoDocumento.exigidosPara(NivelEnsino.ENSINO_FUNDAMENTAL)).hasSize(7)
                .contains(TipoDocumento.HISTORICO_ESCOLAR).doesNotContain(TipoDocumento.CARTAO_SUS);
        assertThat(matricula.pendencias(Fixtures.HOJE)).hasSize(7);
    }

    @Test
    void rn05DocumentoForaDoChecklistDoNivelEhRejeitado() {
        assertThatThrownBy(() -> matricula.registrarEntregaDocumento(TipoDocumento.CARTAO_SUS, "a.pdf", Fixtures.HOJE))
                .hasFieldOrPropertyWithValue("codigo", "RN-05");
    }

    @Test
    void rf06EntregaRemovePendencia() {
        matricula.registrarEntregaDocumento(TipoDocumento.HISTORICO_ESCOLAR, "historico.pdf", Fixtures.HOJE);
        assertThat(matricula.pendencias(Fixtures.HOJE)).hasSize(6).doesNotContain(TipoDocumento.HISTORICO_ESCOLAR);
    }

    @Test
    void rf02FluxoDeAprovacaoEAtivacao() {
        matricula.aprovar();
        assertThat(matricula.ocupaVaga()).isTrue();
        matricula.ativar();
        assertThat(matricula.status()).isEqualTo(StatusMatricula.ATIVA);
        assertThatThrownBy(matricula::aprovar).isInstanceOf(DomainException.class);
    }

    @Test
    void rf02RejeicaoExigeJustificativa() {
        assertThatThrownBy(() -> matricula.rejeitar(" ")).isInstanceOf(DomainException.class);
        matricula.rejeitar("Documentação incompleta");
        assertThat(matricula.status()).isEqualTo(StatusMatricula.REJEITADA);
        assertThat(matricula.justificativaRejeicao()).isEqualTo("Documentação incompleta");
    }

    @Test
    void rf08RenovacaoReaproveitaApenasDocumentosVigentes() {
        matricula.registrarEntregaDocumento(TipoDocumento.CERTIDAO_NASCIMENTO, "certidao.pdf", Fixtures.HOJE);
        matricula.registrarEntregaDocumento(TipoDocumento.COMPROVANTE_RESIDENCIA, "conta.pdf", Fixtures.HOJE);

        Matricula renovacao = matricula.renovar(UUID.randomUUID(), NivelEnsino.ENSINO_FUNDAMENTAL, 2027,
                Fixtures.HOJE.plusMonths(10));

        assertThat(renovacao.tipo()).isEqualTo(TipoMatricula.RENOVACAO);
        assertThat(renovacao.pendencias(Fixtures.HOJE.plusMonths(10)))
                .doesNotContain(TipoDocumento.CERTIDAO_NASCIMENTO)
                .contains(TipoDocumento.COMPROVANTE_RESIDENCIA);
        assertThatThrownBy(() -> matricula.renovar(UUID.randomUUID(), NivelEnsino.ENSINO_FUNDAMENTAL, 2026,
                Fixtures.HOJE)).isInstanceOf(DomainException.class);
    }

    @Test
    void rf09TurmaLotadaBloqueiaNovasMatriculas() {
        Turma turma = Turma.nova("1º ano A", Serie.PRIMEIRO_ANO, 2026, 2, null);
        turma.garantirVaga(1);
        assertThat(turma.vagasDisponiveis(2)).isZero();
        assertThatThrownBy(() -> turma.garantirVaga(2)).hasFieldOrPropertyWithValue("codigo", "RF-09");
    }
}

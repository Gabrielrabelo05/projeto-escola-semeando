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
            NivelEnsino.ENSINO_FUNDAMENTAL, 2026, false, Fixtures.HOJE);

    @Test
    void rn05ChecklistDoAnexoDidaticoComDeclaracaoDeAdimplenciaSoParaQuemVemDeEscolaParticular() {
        assertThat(TipoDocumento.exigidos(true)).hasSize(7).contains(TipoDocumento.DECLARACAO_ADIMPLENCIA,
                TipoDocumento.DOCUMENTO_AUTORIZADOS, TipoDocumento.FOTOS_3X4);
        assertThat(TipoDocumento.exigidos(false)).hasSize(6).doesNotContain(TipoDocumento.DECLARACAO_ADIMPLENCIA);
        assertThat(matricula.pendencias(Fixtures.HOJE)).hasSize(6);
        assertThat(Matricula.solicitar(UUID.randomUUID(), UUID.randomUUID(), NivelEnsino.EDUCACAO_INFANTIL, 2026, true,
                Fixtures.HOJE).pendencias(Fixtures.HOJE)).contains(TipoDocumento.DECLARACAO_ADIMPLENCIA);
    }

    @Test
    void rn05DocumentoForaDoChecklistDaMatriculaEhRejeitado() {
        assertThatThrownBy(() -> matricula.registrarEntregaDocumento(TipoDocumento.DECLARACAO_ADIMPLENCIA, "a.pdf",
                Fixtures.HOJE)).hasFieldOrPropertyWithValue("codigo", "RN-05");
    }

    @Test
    void rf06EntregaRemovePendencia() {
        matricula.registrarEntregaDocumento(TipoDocumento.DOCUMENTO_ALUNO, "certidao.pdf", Fixtures.HOJE);
        assertThat(matricula.pendencias(Fixtures.HOJE)).hasSize(5).doesNotContain(TipoDocumento.DOCUMENTO_ALUNO);
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
        matricula.registrarEntregaDocumento(TipoDocumento.DOCUMENTO_ALUNO, "certidao.pdf", Fixtures.HOJE);
        matricula.registrarEntregaDocumento(TipoDocumento.COMPROVANTE_RESIDENCIA, "conta.pdf", Fixtures.HOJE);

        Matricula renovacao = matricula.renovar(UUID.randomUUID(), NivelEnsino.ENSINO_FUNDAMENTAL, 2027,
                Fixtures.HOJE.plusMonths(10));

        assertThat(renovacao.tipo()).isEqualTo(TipoMatricula.RENOVACAO);
        assertThat(renovacao.pendencias(Fixtures.HOJE.plusMonths(10)))
                .doesNotContain(TipoDocumento.DOCUMENTO_ALUNO)
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

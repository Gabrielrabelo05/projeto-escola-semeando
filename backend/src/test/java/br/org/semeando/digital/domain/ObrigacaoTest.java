package br.org.semeando.digital.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.org.semeando.digital.domain.obrigacao.Comprovante;
import br.org.semeando.digital.domain.obrigacao.NivelAlerta;
import br.org.semeando.digital.domain.obrigacao.ObrigacaoRegulatoria;
import br.org.semeando.digital.domain.obrigacao.StatusObrigacao;
import br.org.semeando.digital.domain.obrigacao.TipoObrigacao;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ObrigacaoTest {

    @Test
    void rn10PrazosLegaisSaoCalculadosPeloSistema() {
        assertThat(TipoObrigacao.SMTT.prazo(2026, null)).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(TipoObrigacao.CENSO_ESCOLAR.prazo(2026, null)).isEqualTo(LocalDate.of(2026, 6, 30));
        assertThat(TipoObrigacao.SISTEMA_PRESENCA.prazo(2026, 1)).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(TipoObrigacao.SISTEMA_PRESENCA.prazo(2026, 4)).isEqualTo(LocalDate.of(2026, 11, 30));
    }

    @Test
    void rn10AlertasCom30E15DiasSemRepeticao() {
        ObrigacaoRegulatoria smtt = ObrigacaoRegulatoria.criar(TipoObrigacao.SMTT, 2026, null, "Secretaria");

        assertThat(smtt.alertaDevido(LocalDate.of(2026, 2, 20))).isEmpty();
        assertThat(smtt.alertaDevido(LocalDate.of(2026, 3, 1))).contains(NivelAlerta.TRINTA_DIAS);
        smtt.registrarAlertaEnviado(NivelAlerta.TRINTA_DIAS);
        assertThat(smtt.alertaDevido(LocalDate.of(2026, 3, 5))).isEmpty();
        assertThat(smtt.alertaDevido(LocalDate.of(2026, 3, 16))).contains(NivelAlerta.QUINZE_DIAS);
        smtt.registrarAlertaEnviado(NivelAlerta.QUINZE_DIAS);
        assertThat(smtt.alertaDevido(LocalDate.of(2026, 4, 1))).contains(NivelAlerta.VENCIDA);
    }

    @Test
    void rf48SmttPossuiChecklistDeDocumentos() {
        ObrigacaoRegulatoria smtt = ObrigacaoRegulatoria.criar(TipoObrigacao.SMTT, 2026, null, "Secretaria");
        assertThat(smtt.checklist()).isNotEmpty().allMatch(item -> !item.concluido());
        smtt.marcarItem(0, true);
        assertThat(smtt.checklist().get(0).concluido()).isTrue();
        assertThat(smtt.status()).isEqualTo(StatusObrigacao.EM_ANDAMENTO);
    }

    @Test
    void rf53ConclusaoExigeComprovante() {
        ObrigacaoRegulatoria censo = ObrigacaoRegulatoria.criar(TipoObrigacao.CENSO_ESCOLAR, 2026, null, "Secretaria");
        assertThatThrownBy(() -> censo.concluir(LocalDate.of(2026, 6, 1)))
                .hasFieldOrPropertyWithValue("codigo", "COMPROVANTE_OBRIGATORIO");

        censo.anexarComprovante(new Comprovante("recibo.pdf", "x/recibo.pdf", Instant.now(), "secretaria"));
        censo.concluir(LocalDate.of(2026, 6, 1));

        assertThat(censo.status()).isEqualTo(StatusObrigacao.CONCLUIDA);
        assertThat(censo.alertaDevido(LocalDate.of(2026, 6, 25))).isEmpty();
    }
}

package br.org.semeando.digital.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.org.semeando.digital.domain.pedagogico.CalculadoraFrequencia;
import br.org.semeando.digital.domain.pedagogico.ConteudoPlano;
import br.org.semeando.digital.domain.pedagogico.DiarioClasse;
import br.org.semeando.digital.domain.pedagogico.Frequencia;
import br.org.semeando.digital.domain.pedagogico.PlanoAula;
import br.org.semeando.digital.domain.pedagogico.Presenca;
import br.org.semeando.digital.domain.pedagogico.StatusPlanoAula;
import br.org.semeando.digital.domain.shared.DomainException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PedagogicoTest {

    private final UUID professor = UUID.randomUUID();
    private final UUID turma = UUID.randomUUID();

    @Test
    void rf44AlertaQuandoFrequenciaFicaAbaixoDe75PorCento() {
        UUID aluno = UUID.randomUUID();
        List<DiarioClasse> diarios = List.of(
                diario(1, aluno, true), diario(2, aluno, true), diario(3, aluno, true), diario(4, aluno, false));
        Frequencia frequencia = CalculadoraFrequencia.doAluno(aluno, diarios);
        assertThat(frequencia.percentual()).isEqualByComparingTo("75.00");
        assertThat(frequencia.emAlerta()).isFalse();

        Frequencia baixa = CalculadoraFrequencia.doAluno(aluno, List.of(diario(1, aluno, true), diario(2, aluno, false)));
        assertThat(baixa.emAlerta()).isTrue();
        assertThat(baixa.faltas()).isEqualTo(1);
    }

    @Test
    void rf45FrequenciaConsolidadaDaTurma() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        var diarios = List.of(
                DiarioClasse.registrar(professor, turma, LocalDate.of(2026, 3, 2), "Aula",
                        List.of(new Presenca(a, true), new Presenca(b, false))),
                DiarioClasse.registrar(professor, turma, LocalDate.of(2026, 3, 3), "Aula",
                        List.of(new Presenca(a, true), new Presenca(b, true))));
        assertThat(CalculadoraFrequencia.daTurma(diarios)).extracting(Frequencia::percentual)
                .containsExactly(new java.math.BigDecimal("100.00"), new java.math.BigDecimal("50.00"));
    }

    @Test
    void rf46FluxoDeAprovacaoDoPlanoDeAula() {
        PlanoAula plano = PlanoAula.criar(professor, turma, LocalDate.of(2026, 3, 2),
                new ConteudoPlano("Objetivo", "Conteúdo", "Metodologia", null, null));
        assertThatThrownBy(() -> plano.aprovar("ok")).isInstanceOf(DomainException.class);

        plano.submeter();
        plano.devolver("Detalhar a avaliação");
        assertThat(plano.status()).isEqualTo(StatusPlanoAula.DEVOLVIDO);

        plano.editar(LocalDate.of(2026, 3, 3), new ConteudoPlano("Objetivo", "Conteúdo", "Metodologia", "Prova", null));
        plano.submeter();
        plano.aprovar("Aprovado");
        assertThat(plano.status()).isEqualTo(StatusPlanoAula.APROVADO);
        assertThatThrownBy(() -> plano.editar(LocalDate.now(), plano.conteudo())).isInstanceOf(DomainException.class);
    }

    private DiarioClasse diario(int dia, UUID aluno, boolean presente) {
        return DiarioClasse.registrar(professor, turma, LocalDate.of(2026, 3, dia), "Aula",
                List.of(new Presenca(aluno, presente)));
    }
}

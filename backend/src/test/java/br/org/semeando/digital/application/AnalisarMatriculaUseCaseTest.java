package br.org.semeando.digital.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.cobranca.EmissorCobranca;
import br.org.semeando.digital.application.matricula.AnalisarMatriculaUseCase;
import br.org.semeando.digital.application.matricula.MatriculaContexto;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.Notificador;
import br.org.semeando.digital.domain.Fixtures;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.cobranca.Cobranca;
import br.org.semeando.digital.domain.cobranca.TipoCobranca;
import br.org.semeando.digital.domain.contrato.ContratoPrestacaoServicos;
import br.org.semeando.digital.domain.contrato.DocumentoVinculoRepository;
import br.org.semeando.digital.domain.contrato.GeradorDocumentoVinculo;
import br.org.semeando.digital.domain.contrato.Mensalidade;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.matricula.StatusMatricula;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.pessoa.ResponsavelRepository;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.MesReferencia;
import br.org.semeando.digital.domain.turma.Serie;
import br.org.semeando.digital.domain.turma.Turma;
import br.org.semeando.digital.domain.turma.TurmaRepository;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalisarMatriculaUseCaseTest {

    @Mock
    MatriculaRepository matriculas;
    @Mock
    AlunoRepository alunos;
    @Mock
    TurmaRepository turmas;
    @Mock
    ResponsavelRepository responsaveis;
    @Mock
    DocumentoVinculoRepository vinculos;
    @Mock
    EmissorCobranca emissor;
    @Mock
    Notificador notificador;
    @Mock
    Auditor auditor;

    private final Clock clock = Clock.fixed(Fixtures.HOJE.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private final Ator secretaria = new Ator(UUID.randomUUID(), "Secretária", null, null);
    private AnalisarMatriculaUseCase useCase;
    private Responsavel responsavel;
    private Aluno aluno;
    private Turma turma;
    private Matricula matricula;

    @BeforeEach
    void preparar() {
        var contexto = new MatriculaContexto(matriculas, alunos, turmas, responsaveis);
        useCase = new AnalisarMatriculaUseCase(contexto, matriculas, vinculos,
                new GeradorDocumentoVinculo(Fixtures.tabela()), Fixtures.tabela(), emissor, notificador, auditor, clock);
        responsavel = Fixtures.responsavel();
        aluno = Fixtures.aluno(responsavel, true);
        turma = Turma.nova("1º ano A", Serie.PRIMEIRO_ANO, 2026, 20, null);
        matricula = Matricula.solicitar(aluno.id(), turma.id(), turma.nivel(), 2026, false, Fixtures.HOJE);

        when(matriculas.buscarPorId(matricula.id())).thenReturn(Optional.of(matricula));
        lenient().when(turmas.buscarPorId(turma.id())).thenReturn(Optional.of(turma));
        when(alunos.buscarPorId(aluno.id())).thenReturn(Optional.of(aluno));
        when(responsaveis.buscarPorId(responsavel.id())).thenReturn(Optional.of(responsavel));
    }

    @Test
    void aprovarGeraContratoDaSerieEPrimeiraParcelaComDesconto() {
        when(matriculas.listarPorAluno(aluno.id())).thenReturn(List.of(matricula));
        Mensalidade primeiroAno = Mensalidade.de("310.00", "279.00");
        Cobranca primeiraParcela = Cobranca.emitir(aluno.id(), TipoCobranca.MENSALIDADE, MesReferencia.de("2026-03"),
                primeiroAno, Fixtures.HOJE.plusDays(5), null);
        when(emissor.emitir(aluno, responsavel, TipoCobranca.MENSALIDADE, MesReferencia.de("2026-03"), primeiroAno,
                Fixtures.HOJE.plusDays(5))).thenReturn(primeiraParcela);

        var vinculo = useCase.aprovar(matricula.id(), secretaria);

        assertThat(vinculo).isInstanceOfSatisfying(ContratoPrestacaoServicos.class,
                contrato -> assertThat(contrato.valorParcela()).isEqualTo(Dinheiro.de("310.00")));
        assertThat(matricula.status()).isEqualTo(StatusMatricula.APROVADA);
        assertThat(matricula.primeiraParcelaId()).isEqualTo(primeiraParcela.id());
        verify(vinculos).salvar(vinculo);
        verify(notificador).responsavel(eq(responsavel), eq("Matrícula aprovada"), anyString());
    }

    @Test
    void rn04NaoAprovaQuandoAlunoJaPossuiMatriculaEmVigor() {
        Matricula outra = Matricula.solicitar(aluno.id(), UUID.randomUUID(), turma.nivel(), 2026, false,
                Fixtures.HOJE);
        outra.aprovar();
        when(matriculas.listarPorAluno(aluno.id())).thenReturn(List.of(matricula, outra));

        assertThatThrownBy(() -> useCase.aprovar(matricula.id(), secretaria))
                .hasFieldOrPropertyWithValue("codigo", "RN-04");
        verify(vinculos, never()).salvar(any());
    }

    @Test
    void rf09NaoAprovaQuandoTurmaEstaLotada() {
        when(matriculas.listarPorAluno(aluno.id())).thenReturn(List.of(matricula));
        when(matriculas.contarOcupandoVaga(turma.id())).thenReturn(20L);

        assertThatThrownBy(() -> useCase.aprovar(matricula.id(), secretaria))
                .hasFieldOrPropertyWithValue("codigo", "RF-09");
        assertThat(matricula.status()).isEqualTo(StatusMatricula.AGUARDANDO_ANALISE);
    }

    @Test
    void rejeitarRegistraJustificativaENotificaResponsavel() {
        useCase.rejeitar(matricula.id(), "Sem vagas no turno", secretaria);

        assertThat(matricula.status()).isEqualTo(StatusMatricula.REJEITADA);
        verify(notificador).responsavel(eq(responsavel), eq("Matrícula não aprovada"), anyString());
    }
}

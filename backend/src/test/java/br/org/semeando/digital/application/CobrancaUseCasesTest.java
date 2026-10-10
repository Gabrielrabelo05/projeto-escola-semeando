package br.org.semeando.digital.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.cobranca.ConfirmarPagamentoUseCase;
import br.org.semeando.digital.application.cobranca.EmissorCobranca;
import br.org.semeando.digital.application.cobranca.GerarCobrancasMensaisUseCase;
import br.org.semeando.digital.application.port.CoraGateway;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.Notificador;
import br.org.semeando.digital.domain.Fixtures;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.aluno.AtividadeExtracurricular;
import br.org.semeando.digital.domain.aluno.StatusAluno;
import br.org.semeando.digital.domain.cobranca.Boleto;
import br.org.semeando.digital.domain.cobranca.Cobranca;
import br.org.semeando.digital.domain.cobranca.CobrancaRepository;
import br.org.semeando.digital.domain.cobranca.StatusCobranca;
import br.org.semeando.digital.domain.cobranca.TipoCobranca;
import br.org.semeando.digital.domain.contrato.DocumentoVinculo;
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
import br.org.semeando.digital.domain.turma.NivelEnsino;
import br.org.semeando.digital.domain.turma.Serie;
import br.org.semeando.digital.domain.turma.Turma;
import br.org.semeando.digital.domain.turma.TurmaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CobrancaUseCasesTest {

    private static final MesReferencia MARCO = MesReferencia.de("2026-03");

    @Mock
    AlunoRepository alunos;
    @Mock
    ResponsavelRepository responsaveis;
    @Mock
    DocumentoVinculoRepository vinculos;
    @Mock
    CobrancaRepository cobrancas;
    @Mock
    MatriculaRepository matriculas;
    @Mock
    TurmaRepository turmas;
    @Mock
    CoraGateway cora;
    @Mock
    EmissorCobranca emissor;
    @Mock
    Notificador notificador;
    @Mock
    Auditor auditor;

    private final Responsavel responsavel = Fixtures.responsavel();

    @Test
    void rf24GeraMensalidadeDaSerieApenasParaAlunosComContratoAssinado() {
        Aluno comContrato = alunoAtivo();
        Aluno semContrato = alunoAtivo();
        DocumentoVinculo contrato = contratoDe(comContrato, matriculaAtiva(comContrato, Serie.PRIMEIRO_ANO));
        contrato.assinar("Maria", "127.0.0.1", Instant.now());
        when(alunos.listar(StatusAluno.ATIVO)).thenReturn(List.of(comContrato, semContrato));
        when(vinculos.buscarVigentePorAlunoEAno(comContrato.id(), 2026)).thenReturn(Optional.of(contrato));
        when(vinculos.buscarVigentePorAlunoEAno(semContrato.id(), 2026)).thenReturn(Optional.empty());
        when(responsaveis.buscarPorId(responsavel.id())).thenReturn(Optional.of(responsavel));

        var resultado = useCaseGeracao().executar(MARCO, Ator.sistema());

        assertThat(resultado.geradas()).containsExactly(comContrato.nome());
        assertThat(resultado.ignoradas()).singleElement().asString().contains("RN-07");
        verify(emissor).emitir(comContrato, responsavel, TipoCobranca.MENSALIDADE, MARCO,
                Mensalidade.de("310.00", "279.00"), MARCO.dia(10));
    }

    @Test
    void rf24AtividadesExtracurricularesSaoCobradasAParte() {
        Aluno aluno = alunoAtivo();
        aluno.definirAtividades(Set.of(AtividadeExtracurricular.JIU_JITSU, AtividadeExtracurricular.BALLET));
        DocumentoVinculo contrato = contratoDe(aluno, matriculaAtiva(aluno, Serie.TERCEIRO_ANO));
        contrato.assinar("Maria", "127.0.0.1", Instant.now());
        when(alunos.listar(StatusAluno.ATIVO)).thenReturn(List.of(aluno));
        when(vinculos.buscarVigentePorAlunoEAno(aluno.id(), 2026)).thenReturn(Optional.of(contrato));
        when(responsaveis.buscarPorId(responsavel.id())).thenReturn(Optional.of(responsavel));

        var resultado = useCaseGeracao().executar(MARCO, Ator.sistema());

        assertThat(resultado.geradas()).hasSize(2);
        verify(emissor).emitir(aluno, responsavel, TipoCobranca.MENSALIDADE, MARCO, Mensalidade.de("375.00", "337.00"),
                MARCO.dia(10));
        verify(emissor).emitir(aluno, responsavel, TipoCobranca.EXTRACURRICULAR, MARCO,
                Mensalidade.semDesconto(Dinheiro.de("100.00")), MARCO.dia(10));
    }

    @Test
    void taxaDeAssociadoNaoEmiteBoletoEMensalidadeEmiteComDesconto() {
        when(cobrancas.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));
        when(cora.emitirBoleto(any())).thenReturn(new Boleto("cora-1", null, null));
        var emissorReal = new EmissorCobranca(cora, cobrancas);
        Aluno aluno = alunoAtivo();

        Cobranca taxa = emissorReal.emitir(aluno, responsavel, TipoCobranca.TAXA_ASSOCIADO, MARCO,
                Mensalidade.semDesconto(Dinheiro.de("125.00")), MARCO.dia(10));
        Cobranca mensalidade = emissorReal.emitir(aluno, responsavel, TipoCobranca.MENSALIDADE, MARCO,
                Mensalidade.de("310.00", "279.00"), MARCO.dia(10));

        assertThat(taxa.boleto()).isNull();
        assertThat(mensalidade.boleto()).isNotNull();
        assertThat(mensalidade.valorComDesconto()).isEqualTo(Dinheiro.de("279.00"));
        verify(cora).emitirBoleto(argThat(s -> s.desconto().equals(Dinheiro.de("31.00"))));
    }

    @Test
    void rf24NaoDuplicaCobrancaDoMesmoMes() {
        Aluno aluno = alunoAtivo();
        DocumentoVinculo contrato = contratoDe(aluno, Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL));
        contrato.assinar("Maria", "127.0.0.1", Instant.now());
        when(alunos.listar(StatusAluno.ATIVO)).thenReturn(List.of(aluno));
        when(vinculos.buscarVigentePorAlunoEAno(aluno.id(), 2026)).thenReturn(Optional.of(contrato));
        when(cobrancas.existe(aluno.id(), TipoCobranca.MENSALIDADE, MARCO)).thenReturn(true);

        var resultado = useCaseGeracao().executar(MARCO, Ator.sistema());

        assertThat(resultado.geradas()).isEmpty();
        verify(emissor, never()).emitir(any(), any(), any(), any(), any(), any());
    }

    @Test
    void rf28PagamentoDaPrimeiraParcelaAtivaMatriculaEAlunoEEnviaRecibo() {
        Aluno aluno = Fixtures.aluno(responsavel, true);
        Matricula matricula = Fixtures.matricula(aluno, NivelEnsino.ENSINO_FUNDAMENTAL);
        matricula.aprovar();
        Cobranca primeiraParcela = Cobranca.emitir(aluno.id(), TipoCobranca.MENSALIDADE, MARCO,
                Mensalidade.de("310.00", "279.00"), Fixtures.HOJE, new Boleto("cora-123", null, null));
        when(cobrancas.buscarPorIdCora("cora-123")).thenReturn(Optional.of(primeiraParcela));
        when(alunos.buscarPorId(aluno.id())).thenReturn(Optional.of(aluno));
        when(matriculas.buscarPorPrimeiraParcela(primeiraParcela.id())).thenReturn(Optional.of(matricula));
        when(responsaveis.buscarPorId(responsavel.id())).thenReturn(Optional.of(responsavel));

        var useCase = new ConfirmarPagamentoUseCase(cobrancas, matriculas, alunos, responsaveis, notificador, auditor);
        useCase.processarWebhook("cora-123", Fixtures.HOJE, Dinheiro.de("279.00"));

        assertThat(primeiraParcela.status()).isEqualTo(StatusCobranca.PAGA);
        assertThat(matricula.status()).isEqualTo(StatusMatricula.ATIVA);
        assertThat(aluno.status()).isEqualTo(StatusAluno.ATIVO);
        verify(notificador).responsavel(eq(responsavel), eq("Recibo de pagamento"), any());
        verify(notificador).responsavel(eq(responsavel), eq("Matrícula confirmada"), any());
    }

    private GerarCobrancasMensaisUseCase useCaseGeracao() {
        return new GerarCobrancasMensaisUseCase(alunos, responsaveis, vinculos, matriculas, turmas, cobrancas, emissor,
                Fixtures.tabela(), auditor);
    }

    private Aluno alunoAtivo() {
        Aluno aluno = Fixtures.aluno(responsavel, true);
        aluno.ativar();
        return aluno;
    }

    /** Matrícula ATIVA na turma da série, já registrada nos repositórios simulados. */
    private Matricula matriculaAtiva(Aluno aluno, Serie serie) {
        Turma turma = Turma.nova(serie.descricao() + " A", serie, 2026, 20, null);
        Matricula matricula = Matricula.solicitar(aluno.id(), turma.id(), serie.nivel(), 2026, false, Fixtures.HOJE);
        matricula.aprovar();
        matricula.ativar();
        when(matriculas.listarPorAluno(aluno.id())).thenReturn(List.of(matricula));
        when(turmas.buscarPorId(turma.id())).thenReturn(Optional.of(turma));
        return matricula;
    }

    private DocumentoVinculo contratoDe(Aluno aluno, Matricula matricula) {
        return new GeradorDocumentoVinculo(Fixtures.tabela()).gerarComTabelaOficial(aluno, responsavel, matricula,
                Serie.PRIMEIRO_ANO, "sec", Instant.now());
    }
}

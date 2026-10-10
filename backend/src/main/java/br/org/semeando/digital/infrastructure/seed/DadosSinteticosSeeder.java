package br.org.semeando.digital.infrastructure.seed;

import br.org.semeando.digital.application.auth.AutenticacaoUseCase;
import br.org.semeando.digital.application.cadastro.AlunosUseCase;
import br.org.semeando.digital.application.cadastro.FuncionariosUseCase;
import br.org.semeando.digital.application.cadastro.FuncionariosUseCase.DadosFuncionario;
import br.org.semeando.digital.application.cadastro.PrestadoresUseCase;
import br.org.semeando.digital.application.cadastro.PrestadoresUseCase.DadosPrestador;
import br.org.semeando.digital.application.cadastro.ProfessoresUseCase;
import br.org.semeando.digital.application.cadastro.ProfessoresUseCase.DadosProfessor;
import br.org.semeando.digital.application.cadastro.TurmasUseCase;
import br.org.semeando.digital.application.cobranca.ConfirmarPagamentoUseCase;
import br.org.semeando.digital.application.cobranca.GerarCobrancasMensaisUseCase;
import br.org.semeando.digital.application.contrato.ContratosUseCase;
import br.org.semeando.digital.application.material.ListaMateriaisUseCase;
import br.org.semeando.digital.application.matricula.AnalisarMatriculaUseCase;
import br.org.semeando.digital.application.matricula.GerenciarMatriculaUseCase;
import br.org.semeando.digital.application.matricula.PreMatriculaCommand;
import br.org.semeando.digital.application.matricula.PreMatriculaCommand.DadosAluno;
import br.org.semeando.digital.application.matricula.PreMatriculaCommand.DadosResponsavel;
import br.org.semeando.digital.application.matricula.SolicitarPreMatriculaUseCase;
import br.org.semeando.digital.application.obrigacao.ObrigacoesUseCase;
import br.org.semeando.digital.application.pedagogico.DiarioClasseUseCase;
import br.org.semeando.digital.application.pedagogico.PlanejamentoCalendarioUseCase;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.aluno.AtividadeExtracurricular;
import br.org.semeando.digital.domain.aluno.InformacoesMedicas;
import br.org.semeando.digital.domain.aluno.PessoaAutorizada;
import br.org.semeando.digital.domain.cobranca.FormaPagamento;
import br.org.semeando.digital.domain.contrato.DocumentoVinculo;
import br.org.semeando.digital.domain.contrato.TabelaValores;
import br.org.semeando.digital.domain.material.ItemMaterial;
import br.org.semeando.digital.domain.material.TipoItem;
import br.org.semeando.digital.domain.matricula.Farda;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.TipoDocumento;
import br.org.semeando.digital.domain.pedagogico.Presenca;
import br.org.semeando.digital.domain.pedagogico.TipoEvento;
import br.org.semeando.digital.domain.pessoa.Contato;
import br.org.semeando.digital.domain.pessoa.Professor;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.MesReferencia;
import br.org.semeando.digital.domain.turma.Serie;
import br.org.semeando.digital.domain.turma.Turma;
import br.org.semeando.digital.domain.usuario.Perfil;
import br.org.semeando.digital.domain.usuario.Usuario;
import br.org.semeando.digital.domain.usuario.UsuarioRepository;
import br.org.semeando.digital.infrastructure.config.SemeandoProperties;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * RN-08: popula o ambiente de desenvolvimento apenas com dados sintéticos, exercitando os
 * próprios casos de uso (fluxo completo de matrícula, contrato, pagamento e cobrança).
 */
@Component
class DadosSinteticosSeeder implements ApplicationRunner {

    static final String SENHA_PADRAO = "Semeando@2026";
    private static final Logger LOG = LoggerFactory.getLogger(DadosSinteticosSeeder.class);

    private final SemeandoProperties props;
    private final TabelaValores tabela;
    private final UsuarioRepository usuarios;
    private final AutenticacaoUseCase autenticacao;
    private final AlunosUseCase alunos;
    private final ProfessoresUseCase professores;
    private final FuncionariosUseCase funcionarios;
    private final PrestadoresUseCase prestadores;
    private final TurmasUseCase turmas;
    private final SolicitarPreMatriculaUseCase preMatricula;
    private final AnalisarMatriculaUseCase analise;
    private final GerenciarMatriculaUseCase gerenciar;
    private final ContratosUseCase contratos;
    private final ConfirmarPagamentoUseCase pagamentos;
    private final GerarCobrancasMensaisUseCase cobrancas;
    private final ObrigacoesUseCase obrigacoes;
    private final PlanejamentoCalendarioUseCase calendario;
    private final ListaMateriaisUseCase listas;
    private final DiarioClasseUseCase diario;
    private final Clock clock;
    private final Ator sistema = Ator.sistema();

    DadosSinteticosSeeder(SemeandoProperties props, TabelaValores tabela, UsuarioRepository usuarios,
            AutenticacaoUseCase autenticacao, AlunosUseCase alunos, ProfessoresUseCase professores,
            FuncionariosUseCase funcionarios, PrestadoresUseCase prestadores,
            TurmasUseCase turmas, SolicitarPreMatriculaUseCase preMatricula, AnalisarMatriculaUseCase analise,
            GerenciarMatriculaUseCase gerenciar, ContratosUseCase contratos, ConfirmarPagamentoUseCase pagamentos,
            GerarCobrancasMensaisUseCase cobrancas, ObrigacoesUseCase obrigacoes,
            PlanejamentoCalendarioUseCase calendario, ListaMateriaisUseCase listas, DiarioClasseUseCase diario,
            Clock clock) {
        this.props = props;
        this.tabela = tabela;
        this.usuarios = usuarios;
        this.autenticacao = autenticacao;
        this.alunos = alunos;
        this.professores = professores;
        this.funcionarios = funcionarios;
        this.prestadores = prestadores;
        this.turmas = turmas;
        this.preMatricula = preMatricula;
        this.analise = analise;
        this.gerenciar = gerenciar;
        this.contratos = contratos;
        this.pagamentos = pagamentos;
        this.cobrancas = cobrancas;
        this.obrigacoes = obrigacoes;
        this.calendario = calendario;
        this.listas = listas;
        this.diario = diario;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!props.seed().habilitado() || !usuarios.listar().isEmpty()) {
            return;
        }
        LOG.info("Carregando dados sintéticos de desenvolvimento (RN-08)...");
        int ano = LocalDate.now(clock).getYear();
        String cpfProfessora = CpfSintetico.de("100000001");
        String cpfResponsavel = CpfSintetico.de("200000002");

        Professor professora = professores.cadastrar(new DadosProfessor("Ana Beatriz Sintética", cpfProfessora,
                new Contato("professora@semeando.dev", "98900000001"), "0000001", "Pedagogia",
                List.of("Alfabetização", "Matemática"), 40), sistema);
        cadastrarEquipe();
        criarUsuarios(cpfProfessora, cpfResponsavel);
        Map<Serie, Turma> turmasDoAno = criarTurmas(ano, professora);

        Matricula fundamental = matricularAteAtivar("Lucas Sintético", LocalDate.of(ano - 6, 4, 12),
                cpfResponsavel, "Maria Responsável Sintética", turmasDoAno.get(Serie.PRIMEIRO_ANO));
        matricularSemPagar("Clara Sintética", LocalDate.of(ano - 3, 8, 2), cpfResponsavel,
                "Maria Responsável Sintética", turmasDoAno.get(Serie.MATERNAL_II));
        preMatricula.executar(comando("Pedro Aguardando", LocalDate.of(ano - 8, 1, 20), CpfSintetico.de("300000003"),
                "João Responsável Sintético", turmasDoAno.get(Serie.TERCEIRO_ANO)), Ator.anonimo());
        alunos.definirAtividades(fundamental.alunoId(), Set.of(AtividadeExtracurricular.JIU_JITSU), sistema);

        cobrancas.executar(new MesReferencia(YearMonth.now(clock)), sistema);
        registrarDiario(turmasDoAno.get(Serie.PRIMEIRO_ANO), fundamental, cpfProfessora);
        obrigacoes.gerarCalendarioAnual(ano, "Secretaria Escolar", sistema);
        criarCalendario(ano);
        publicarListaMateriais(ano);
        LOG.info("Dados sintéticos carregados. Senha padrão de todos os usuários: {}", SENHA_PADRAO);
    }

    private void criarUsuarios(String cpfProfessora, String cpfResponsavel) {
        autenticacao.criar("Administrador do Sistema", "admin@semeando.dev", SENHA_PADRAO, Perfil.ADMIN, null, sistema);
        autenticacao.criar("Diretora Sintética", "diretora@semeando.dev", SENHA_PADRAO, Perfil.DIRETOR, null, sistema);
        autenticacao.criar("Secretária Sintética", "secretaria@semeando.dev", SENHA_PADRAO, Perfil.SECRETARIA, null,
                sistema);
        autenticacao.criar("Financeiro Sintético", "financeiro@semeando.dev", SENHA_PADRAO, Perfil.FINANCEIRO, null,
                sistema);
        autenticacao.criar("Ana Beatriz Sintética", "professora@semeando.dev", SENHA_PADRAO, Perfil.PROFESSOR,
                cpfProfessora, sistema);
        autenticacao.criar("Maria Responsável Sintética", "responsavel@semeando.dev", SENHA_PADRAO,
                Perfil.RESPONSAVEL, cpfResponsavel, sistema);
        autenticacao.criar("Transportes Sintéticos LTDA", "prestador@semeando.dev", SENHA_PADRAO, Perfil.PRESTADOR,
                null, sistema);
    }

    private void cadastrarEquipe() {
        funcionarios.cadastrar(new DadosFuncionario("Secretária Sintética", CpfSintetico.de("400000004"),
                new Contato("secretaria@semeando.dev", null), "Secretária Escolar", "Secretaria", Perfil.SECRETARIA),
                sistema);
        funcionarios.cadastrar(new DadosFuncionario("Financeiro Sintético", CpfSintetico.de("500000005"),
                new Contato("financeiro@semeando.dev", null), "Assistente Financeiro", "Financeiro",
                Perfil.FINANCEIRO), sistema);
        LocalDate hoje = LocalDate.now(clock);
        prestadores.cadastrar(new DadosPrestador("Transportes Sintéticos LTDA", "11222333000181", "Transporte escolar",
                hoje.withDayOfYear(1), hoje.withDayOfYear(1).plusYears(1).minusDays(1),
                List.of("CNPJ", "Alvará SMTT", "Apólice de seguro"), new Contato("prestador@semeando.dev", null)),
                sistema);
    }

    private Map<Serie, Turma> criarTurmas(int ano, Professor titular) {
        Map<Serie, Turma> mapa = new EnumMap<>(Serie.class);
        for (Serie serie : Serie.values()) {
            mapa.put(serie, turmas.criar(serie.descricao() + " - A", serie, ano, 20,
                    serie == Serie.PRIMEIRO_ANO ? titular.id() : null, sistema));
        }
        return mapa;
    }

    private Matricula matricularAteAtivar(String aluno, LocalDate nascimento, String cpf, String responsavel,
            Turma turma) {
        Matricula matricula = matricularSemPagar(aluno, nascimento, cpf, responsavel, turma);
        pagamentos.registrarManual(matricula.primeiraParcelaId(), LocalDate.now(clock),
                tabela.valorMensalPara(turma.serie()).comDesconto(), FormaPagamento.PIX, sistema);
        return matricula;
    }

    private Matricula matricularSemPagar(String aluno, LocalDate nascimento, String cpf, String responsavel,
            Turma turma) {
        Matricula matricula = preMatricula.executar(comando(aluno, nascimento, cpf, responsavel, turma),
                Ator.anonimo());
        DocumentoVinculo vinculo = analise.aprovar(matricula.id(), sistema);
        contratos.assinar(vinculo.id(), "127.0.0.1", sistema);
        gerenciar.registrarFarda(matricula.id(), new Farda("6", "6", "30"), sistema);
        return gerenciar.enviarDocumento(matricula.id(), TipoDocumento.DOCUMENTO_ALUNO, "certidao.txt",
                "documento sintético".getBytes(StandardCharsets.UTF_8), sistema);
    }

    private PreMatriculaCommand comando(String aluno, LocalDate nascimento, String cpf, String responsavel,
            Turma turma) {
        return new PreMatriculaCommand(
                new DadosAluno(aluno, nascimento, new InformacoesMedicas("Nenhuma conhecida", "O+", null),
                        "Hospital Sintético da Ilha",
                        List.of(new PessoaAutorizada("Avó Sintética", "RG 0000000", "Avó", "98900000009"))),
                new DadosResponsavel(responsavel, cpf, slug(responsavel) + "@exemplo.dev", "98900000002", "Mãe"),
                turma.id(), false);
    }

    private void registrarDiario(Turma turma, Matricula matricula, String cpfProfessora) {
        Usuario professora = usuarios.buscarPorEmail("professora@semeando.dev").orElseThrow();
        Ator ator = new Ator(professora.id(), professora.nome(), Perfil.PROFESSOR, Cpf.de(cpfProfessora));
        LocalDate hoje = LocalDate.now(clock);
        for (int dias = 4; dias >= 1; dias--) {
            diario.registrar(turma.id(), hoje.minusDays(dias), "Aula sintética " + dias,
                    List.of(new Presenca(matricula.alunoId(), dias != 2)), ator);
        }
    }

    private void criarCalendario(int ano) {
        calendario.publicarEvento("Início do ano letivo", TipoEvento.INICIO_ANO_LETIVO, LocalDate.of(ano, 2, 2), null,
                sistema);
        calendario.publicarEvento("Carnaval", TipoEvento.FERIADO, LocalDate.of(ano, 2, 16), LocalDate.of(ano, 2, 18),
                sistema);
        calendario.publicarEvento("Reunião de pais – 2º semestre", TipoEvento.REUNIAO_PAIS, LocalDate.of(ano, 10, 17),
                null, sistema);
        calendario.publicarEvento("Encerramento do ano letivo", TipoEvento.FIM_ANO_LETIVO, LocalDate.of(ano, 12, 11),
                null, sistema);
    }

    private void publicarListaMateriais(int ano) {
        var lista = listas.criar(Serie.PRIMEIRO_ANO, ano, List.of(
                new ItemMaterial("Caderno brochura 96 folhas", 4, TipoItem.MATERIAL, null),
                new ItemMaterial("Lápis grafite nº 2", 6, TipoItem.MATERIAL, null),
                new ItemMaterial("Caixa de lápis de cor (12 cores)", 1, TipoItem.MATERIAL, null),
                new ItemMaterial("Livro: Aprender Juntos – Língua Portuguesa 1º ano", 1, TipoItem.LIVRO,
                        "https://www.exemplo.dev/livros/aprender-juntos-1")), sistema);
        listas.publicar(lista.id(), LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31), sistema);
    }

    private static String slug(String texto) {
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase().replaceAll("[^a-z]+", ".").replaceAll("^\\.|\\.$", "");
    }
}

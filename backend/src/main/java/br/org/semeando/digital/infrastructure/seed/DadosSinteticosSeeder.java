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
import br.org.semeando.digital.domain.cobranca.CobrancaRepository;
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
 * RN-08: popula o ambiente com dados de demonstração fictícios (nomes realistas, CPFs gerados e e-mails em
 * example.com), exercitando os próprios casos de uso (matrícula, contrato, pagamento, cobrança e inadimplência).
 */
@Component
class DadosSinteticosSeeder implements ApplicationRunner {

    static final String SENHA_PADRAO = "Semeando@2026";
    private static final Logger LOG = LoggerFactory.getLogger(DadosSinteticosSeeder.class);

    /** Responsável com login de demonstração (responsavel@semeando.dev). */
    private static final Responsavel MARIA = new Responsavel("Maria Fernanda Oliveira Santos",
            CpfSintetico.de("209347516"), "responsavel@semeando.dev", "98987112034", "Mãe");

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
    private final CobrancaRepository cobrancasRepo;
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
            GerarCobrancasMensaisUseCase cobrancas, CobrancaRepository cobrancasRepo, ObrigacoesUseCase obrigacoes,
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
        this.cobrancasRepo = cobrancasRepo;
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
        LOG.info("Carregando dados de demonstração (fictícios, RN-08)...");
        LocalDate hoje = LocalDate.now(clock);
        int ano = hoje.getYear();
        String cpfProfessora = CpfSintetico.de("381245906");

        Professor professora = professores.cadastrar(new DadosProfessor("Ana Beatriz Costa Lima", cpfProfessora,
                new Contato("professora@semeando.dev", "98988127745"), "045218796", "Pedagogia (UFMA)",
                List.of("Alfabetização", "Matemática"), 40), sistema);
        professores.cadastrar(new DadosProfessor("Carlos Eduardo Nunes", CpfSintetico.de("527814360"),
                new Contato("carlos.nunes@example.com", "98991453302"), "038114527", "Licenciatura em Educação Física",
                List.of("Educação Física", "Jiu Jitsu"), 20), sistema);
        cadastrarEquipe();
        criarUsuarios(cpfProfessora);
        Map<Serie, Turma> turmasDoAno = criarTurmas(ano, professora);

        Matricula lucas = matricularAteAtivar(new Familia("Lucas Oliveira Santos", LocalDate.of(ano - 6, 4, 12),
                MARIA, "Hospital da Criança Dr. Odorico Amaral de Matos",
                new PessoaAutorizada("Raimunda Oliveira", "RG 018745236 SSP/MA", "Avó", "98987451230")),
                turmasDoAno.get(Serie.PRIMEIRO_ANO));
        matricularAteAtivar(new Familia("Helena Oliveira Santos", LocalDate.of(ano - 3, 8, 2), MARIA,
                "Hospital da Criança Dr. Odorico Amaral de Matos",
                new PessoaAutorizada("Raimunda Oliveira", "RG 018745236 SSP/MA", "Avó", "98987451230")),
                turmasDoAno.get(Serie.MATERNAL_II));
        Matricula isabela = matricularAteAtivar(new Familia("Isabela Carvalho Rocha", LocalDate.of(ano - 8, 1, 20),
                new Responsavel("Fernanda Carvalho Rocha", CpfSintetico.de("614378205"),
                        "fernanda.rocha@example.com", "98984126673", "Mãe"),
                "Hospital São Domingos",
                new PessoaAutorizada("Paulo Henrique Rocha", "RG 027415896 SSP/MA", "Pai", "98981204478")),
                turmasDoAno.get(Serie.TERCEIRO_ANO));
        Matricula gabriel = matricularAteAtivar(new Familia("Gabriel Henrique Alves", LocalDate.of(ano - 10, 6, 3),
                new Responsavel("José Ricardo Alves", CpfSintetico.de("702563418"), "jose.alves@example.com",
                        "98985567012", "Pai"),
                "Hospital UDI",
                new PessoaAutorizada("Sandra Alves", "RG 031254789 SSP/MA", "Tia", "98988341196")),
                turmasDoAno.get(Serie.QUINTO_ANO));
        matricularSemPagar(new Familia("Miguel Souza Barros", LocalDate.of(ano - 5, 11, 15),
                new Responsavel("Juliana Souza Barros", CpfSintetico.de("835127640"), "juliana.barros@example.com",
                        "98982236645", "Mãe"),
                "Hospital São Domingos",
                new PessoaAutorizada("Antônio Barros", "RG 014785236 SSP/MA", "Avô", "98987002314")),
                turmasDoAno.get(Serie.INFANTIL_2));
        preMatricula.executar(comando(new Familia("Laura Mendes Pires", LocalDate.of(ano - 7, 3, 28),
                new Responsavel("Camila Mendes Pires", CpfSintetico.de("946281573"), "camila.pires@example.com",
                        "98983318820", "Mãe"),
                "Hospital UDI",
                new PessoaAutorizada("Roberto Pires", "RG 025874136 SSP/MA", "Pai", "98986657731")),
                turmasDoAno.get(Serie.SEGUNDO_ANO), true), Ator.anonimo());
        alunos.definirAtividades(lucas.alunoId(), Set.of(AtividadeExtracurricular.JIU_JITSU), sistema);
        alunos.definirAtividades(isabela.alunoId(), Set.of(AtividadeExtracurricular.BALLET), sistema);

        gerarMesAnteriorComInadimplencia(hoje, gabriel);
        cobrancas.executar(new MesReferencia(YearMonth.from(hoje)), sistema);
        registrarDiario(turmasDoAno.get(Serie.PRIMEIRO_ANO), lucas, cpfProfessora);
        obrigacoes.gerarCalendarioAnual(ano, "Secretaria Escolar", sistema);
        criarCalendario(ano);
        publicarListaMateriais(ano);
        LOG.info("Dados de demonstração carregados. Senha padrão dos usuários de teste: {}", SENHA_PADRAO);
    }

    /** Cobranças do mês anterior: todas pagas no prazo, menos a do aluno informado (aparece na inadimplência). */
    private void gerarMesAnteriorComInadimplencia(LocalDate hoje, Matricula inadimplente) {
        MesReferencia anterior = new MesReferencia(YearMonth.from(hoje).minusMonths(1));
        cobrancas.executar(anterior, sistema);
        cobrancasRepo.listarPorMes(anterior).stream()
                .filter(c -> !c.alunoId().equals(inadimplente.alunoId()))
                .forEach(c -> pagamentos.registrarManual(c.id(), c.vencimento().minusDays(2), c.valorComDesconto(),
                        c.boleto() == null ? FormaPagamento.DINHEIRO : FormaPagamento.BOLETO, sistema));
    }

    private void criarUsuarios(String cpfProfessora) {
        autenticacao.criar("Rafael Mendes Araújo", "admin@semeando.dev", SENHA_PADRAO, Perfil.ADMIN, null, sistema);
        autenticacao.criar("Luciana Ribeiro Matos", "diretora@semeando.dev", SENHA_PADRAO, Perfil.DIRETOR, null,
                sistema);
        autenticacao.criar("Patrícia Gomes da Silva", "secretaria@semeando.dev", SENHA_PADRAO, Perfil.SECRETARIA,
                null, sistema);
        autenticacao.criar("Marcos Antônio Pereira", "financeiro@semeando.dev", SENHA_PADRAO, Perfil.FINANCEIRO,
                null, sistema);
        autenticacao.criar("Ana Beatriz Costa Lima", "professora@semeando.dev", SENHA_PADRAO, Perfil.PROFESSOR,
                cpfProfessora, sistema);
        autenticacao.criar(MARIA.nome(), MARIA.email(), SENHA_PADRAO, Perfil.RESPONSAVEL, MARIA.cpf(), sistema);
        autenticacao.criar("Rota Kids Transporte Escolar LTDA", "prestador@semeando.dev", SENHA_PADRAO,
                Perfil.PRESTADOR, null, sistema);
    }

    private void cadastrarEquipe() {
        funcionarios.cadastrar(new DadosFuncionario("Patrícia Gomes da Silva", CpfSintetico.de("458120397"),
                new Contato("secretaria@semeando.dev", "98981447230"), "Secretária Escolar", "Secretaria",
                Perfil.SECRETARIA), sistema);
        funcionarios.cadastrar(new DadosFuncionario("Marcos Antônio Pereira", CpfSintetico.de("563092184"),
                new Contato("financeiro@semeando.dev", "98984702215"), "Assistente Financeiro", "Financeiro",
                Perfil.FINANCEIRO), sistema);
        funcionarios.cadastrar(new DadosFuncionario("Rosângela Ferreira Lopes", CpfSintetico.de("672318045"),
                new Contato("rosangela.lopes@example.com", "98983019946"), "Auxiliar de secretaria", "Secretaria",
                Perfil.SECRETARIA), sistema);
        LocalDate hoje = LocalDate.now(clock);
        prestadores.cadastrar(new DadosPrestador("Rota Kids Transporte Escolar LTDA", "11222333000181",
                "Transporte escolar", hoje.withDayOfYear(1), hoje.withDayOfYear(1).plusYears(1).minusDays(1),
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

    private Matricula matricularAteAtivar(Familia familia, Turma turma) {
        Matricula matricula = matricularSemPagar(familia, turma);
        pagamentos.registrarManual(matricula.primeiraParcelaId(), LocalDate.now(clock),
                tabela.valorMensalPara(turma.serie()).comDesconto(), FormaPagamento.PIX, sistema);
        return matricula;
    }

    private Matricula matricularSemPagar(Familia familia, Turma turma) {
        Matricula matricula = preMatricula.executar(comando(familia, turma, false), Ator.anonimo());
        DocumentoVinculo vinculo = analise.aprovar(matricula.id(), sistema);
        contratos.assinar(vinculo.id(), "127.0.0.1", sistema);
        gerenciar.registrarFarda(matricula.id(), new Farda("6", "6", "30"), sistema);
        gerenciar.enviarDocumento(matricula.id(), TipoDocumento.DOCUMENTO_RESPONSAVEL, "rg-cpf-responsavel.txt",
                "cópia do RG e CPF (demonstração)".getBytes(StandardCharsets.UTF_8), sistema);
        return gerenciar.enviarDocumento(matricula.id(), TipoDocumento.DOCUMENTO_ALUNO, "certidao.txt",
                "certidão de nascimento (demonstração)".getBytes(StandardCharsets.UTF_8), sistema);
    }

    private PreMatriculaCommand comando(Familia familia, Turma turma, boolean vindoDeEscolaParticular) {
        Responsavel r = familia.responsavel();
        return new PreMatriculaCommand(
                new DadosAluno(familia.aluno(), familia.nascimento(),
                        new InformacoesMedicas("Nenhuma conhecida", "O+", null), familia.hospital(),
                        List.of(familia.autorizado())),
                new DadosResponsavel(r.nome(), r.cpf(), r.email(), r.telefone(), r.parentesco()),
                turma.id(), vindoDeEscolaParticular);
    }

    private record Responsavel(String nome, String cpf, String email, String telefone, String parentesco) {
    }

    private record Familia(String aluno, LocalDate nascimento, Responsavel responsavel, String hospital,
            PessoaAutorizada autorizado) {
    }

    private static final List<String> CONTEUDOS = List.of("Leitura compartilhada: Chapeuzinho Vermelho",
            "Adição com material dourado", "Vogais e encontros vocálicos", "Números até 100 e sequência numérica");

    private void registrarDiario(Turma turma, Matricula matricula, String cpfProfessora) {
        Usuario professora = usuarios.buscarPorEmail("professora@semeando.dev").orElseThrow();
        Ator ator = new Ator(professora.id(), professora.nome(), Perfil.PROFESSOR, Cpf.de(cpfProfessora));
        LocalDate hoje = LocalDate.now(clock);
        for (int dias = 4; dias >= 1; dias--) {
            diario.registrar(turma.id(), hoje.minusDays(dias), CONTEUDOS.get(dias - 1),
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
}

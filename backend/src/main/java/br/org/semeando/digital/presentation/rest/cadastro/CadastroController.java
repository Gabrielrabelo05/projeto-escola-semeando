package br.org.semeando.digital.presentation.rest.cadastro;

import br.org.semeando.digital.application.auditoria.ConsultarAuditoriaUseCase;
import br.org.semeando.digital.application.cadastro.AlunosUseCase;
import br.org.semeando.digital.application.cadastro.BuscaUnificadaUseCase;
import br.org.semeando.digital.application.cadastro.FuncionariosUseCase;
import br.org.semeando.digital.application.cadastro.PrestadoresUseCase;
import br.org.semeando.digital.application.cadastro.ProfessoresUseCase;
import br.org.semeando.digital.application.cadastro.TurmasUseCase;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.aluno.StatusAluno;
import br.org.semeando.digital.presentation.rest.cadastro.CadastroDtos.AtividadesRequest;
import br.org.semeando.digital.presentation.rest.cadastro.CadastroDtos.AtualizarAlunoRequest;
import br.org.semeando.digital.presentation.rest.cadastro.CadastroDtos.AuditoriaResponse;
import br.org.semeando.digital.presentation.rest.cadastro.CadastroDtos.FuncionarioRequest;
import br.org.semeando.digital.presentation.rest.cadastro.CadastroDtos.FuncionarioResponse;
import br.org.semeando.digital.presentation.rest.cadastro.CadastroDtos.PrestadorRequest;
import br.org.semeando.digital.presentation.rest.cadastro.CadastroDtos.PrestadorResponse;
import br.org.semeando.digital.presentation.rest.cadastro.CadastroDtos.ProfessorRequest;
import br.org.semeando.digital.presentation.rest.cadastro.CadastroDtos.ProfessorResponse;
import br.org.semeando.digital.presentation.rest.cadastro.CadastroDtos.TurmaRequest;
import br.org.semeando.digital.presentation.rest.shared.Dtos.AlunoDetalhe;
import br.org.semeando.digital.presentation.rest.shared.Dtos.AlunoResumo;
import br.org.semeando.digital.presentation.rest.shared.Dtos.TurmaDto;
import br.org.semeando.digital.presentation.rest.shared.Perfis;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Módulo 5 – Cadastros gerais (RF-31 a RF-38). Registros são inativados, nunca excluídos (RF-37). */
@RestController
@RequestMapping("/api")
public class CadastroController {

    private final AlunosUseCase alunos;
    private final ProfessoresUseCase professores;
    private final FuncionariosUseCase funcionarios;
    private final PrestadoresUseCase prestadores;
    private final TurmasUseCase turmas;
    private final BuscaUnificadaUseCase busca;
    private final ConsultarAuditoriaUseCase auditoria;
    private final Clock clock;

    public CadastroController(AlunosUseCase alunos, ProfessoresUseCase professores, FuncionariosUseCase funcionarios,
            PrestadoresUseCase prestadores, TurmasUseCase turmas, BuscaUnificadaUseCase busca,
            ConsultarAuditoriaUseCase auditoria, Clock clock) {
        this.alunos = alunos;
        this.professores = professores;
        this.funcionarios = funcionarios;
        this.prestadores = prestadores;
        this.turmas = turmas;
        this.busca = busca;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    // ----- Alunos (RF-31)

    @GetMapping("/alunos")
    @PreAuthorize(Perfis.SECRETARIA_OU_PROFESSOR)
    public List<AlunoResumo> alunos(@RequestParam(required = false) StatusAluno status) {
        return alunos.listar(status).stream().map(AlunoResumo::de).toList();
    }

    @GetMapping("/alunos/{id}")
    @PreAuthorize(Perfis.SECRETARIA)
    public AlunoDetalhe aluno(@PathVariable UUID id) {
        var ficha = alunos.ficha(id);
        return AlunoDetalhe.de(ficha.aluno(), ficha.responsavel());
    }

    @PutMapping("/alunos/{id}")
    @PreAuthorize(Perfis.SECRETARIA)
    public AlunoDetalhe atualizarAluno(@PathVariable UUID id, @Valid @RequestBody AtualizarAlunoRequest request,
            Ator ator) {
        alunos.atualizar(id, request.paraComando(), ator);
        return aluno(id);
    }

    /** Jiu Jitsu / Ballet: R$ 50,00 por atividade, cobrados na geração mensal. */
    @PutMapping("/alunos/{id}/atividades")
    @PreAuthorize(Perfis.SECRETARIA)
    public AlunoDetalhe definirAtividades(@PathVariable UUID id, @Valid @RequestBody AtividadesRequest request,
            Ator ator) {
        alunos.definirAtividades(id, request.atividades(), ator);
        return aluno(id);
    }

    @PatchMapping("/alunos/{id}/inativar")
    @PreAuthorize(Perfis.SECRETARIA)
    public AlunoResumo inativarAluno(@PathVariable UUID id, Ator ator) {
        return AlunoResumo.de(alunos.inativar(id, ator));
    }

    // ----- Professores (RF-32)

    @GetMapping("/professores")
    @PreAuthorize(Perfis.SECRETARIA)
    public List<ProfessorResponse> professores(@RequestParam(defaultValue = "false") boolean incluirInativos) {
        return professores.listar(incluirInativos).stream().map(ProfessorResponse::de).toList();
    }

    @PostMapping("/professores")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.SECRETARIA)
    public ProfessorResponse criarProfessor(@Valid @RequestBody ProfessorRequest request, Ator ator) {
        return ProfessorResponse.de(professores.cadastrar(request.paraDados(), ator));
    }

    @PutMapping("/professores/{id}")
    @PreAuthorize(Perfis.SECRETARIA)
    public ProfessorResponse atualizarProfessor(@PathVariable UUID id, @Valid @RequestBody ProfessorRequest request,
            Ator ator) {
        return ProfessorResponse.de(professores.atualizar(id, request.paraDados(), ator));
    }

    @PatchMapping("/professores/{id}/inativar")
    @PreAuthorize(Perfis.SECRETARIA)
    public ProfessorResponse inativarProfessor(@PathVariable UUID id, Ator ator) {
        return ProfessorResponse.de(professores.inativar(id, ator));
    }

    // ----- Funcionários (RF-33)

    @GetMapping("/funcionarios")
    @PreAuthorize(Perfis.GESTAO)
    public List<FuncionarioResponse> funcionarios(@RequestParam(defaultValue = "false") boolean incluirInativos) {
        return funcionarios.listar(incluirInativos).stream().map(FuncionarioResponse::de).toList();
    }

    @PostMapping("/funcionarios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.GESTAO)
    public FuncionarioResponse criarFuncionario(@Valid @RequestBody FuncionarioRequest request, Ator ator) {
        return FuncionarioResponse.de(funcionarios.cadastrar(request.paraDados(), ator));
    }

    @PutMapping("/funcionarios/{id}")
    @PreAuthorize(Perfis.GESTAO)
    public FuncionarioResponse atualizarFuncionario(@PathVariable UUID id,
            @Valid @RequestBody FuncionarioRequest request, Ator ator) {
        return FuncionarioResponse.de(funcionarios.atualizar(id, request.paraDados(), ator));
    }

    @PatchMapping("/funcionarios/{id}/inativar")
    @PreAuthorize(Perfis.GESTAO)
    public FuncionarioResponse inativarFuncionario(@PathVariable UUID id, Ator ator) {
        return FuncionarioResponse.de(funcionarios.inativar(id, ator));
    }

    // ----- Prestadores (RF-34)

    @GetMapping("/prestadores")
    @PreAuthorize(Perfis.SECRETARIA)
    public List<PrestadorResponse> prestadores(@RequestParam(defaultValue = "false") boolean incluirInativos) {
        LocalDate hoje = LocalDate.now(clock);
        return prestadores.listar(incluirInativos).stream().map(p -> PrestadorResponse.de(p, hoje)).toList();
    }

    @PostMapping("/prestadores")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.SECRETARIA)
    public PrestadorResponse criarPrestador(@Valid @RequestBody PrestadorRequest request, Ator ator) {
        return PrestadorResponse.de(prestadores.cadastrar(request.paraDados(), ator), LocalDate.now(clock));
    }

    @PutMapping("/prestadores/{id}")
    @PreAuthorize(Perfis.SECRETARIA)
    public PrestadorResponse atualizarPrestador(@PathVariable UUID id, @Valid @RequestBody PrestadorRequest request,
            Ator ator) {
        return PrestadorResponse.de(prestadores.atualizar(id, request.paraDados(), ator), LocalDate.now(clock));
    }

    @PatchMapping("/prestadores/{id}/inativar")
    @PreAuthorize(Perfis.SECRETARIA)
    public PrestadorResponse inativarPrestador(@PathVariable UUID id, Ator ator) {
        return PrestadorResponse.de(prestadores.inativar(id, ator), LocalDate.now(clock));
    }

    // ----- Turmas

    @GetMapping("/turmas")
    @PreAuthorize(Perfis.TODOS)
    public List<TurmaDto> turmas(@RequestParam(required = false) Integer anoLetivo) {
        return turmas.listar(anoLetivo).stream().map(TurmaDto::de).toList();
    }

    @PostMapping("/turmas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.SECRETARIA)
    public TurmaDto criarTurma(@Valid @RequestBody TurmaRequest r, Ator ator) {
        return TurmaDto.de(turmas.criar(r.nome(), r.serie(), r.anoLetivo(), r.capacidadeMaxima(),
                r.professorTitularId(), ator));
    }

    @PutMapping("/turmas/{id}")
    @PreAuthorize(Perfis.SECRETARIA)
    public TurmaDto atualizarTurma(@PathVariable UUID id, @Valid @RequestBody TurmaRequest r, Ator ator) {
        return TurmaDto.de(turmas.atualizar(id, r.nome(), r.capacidadeMaxima(), r.professorTitularId(), ator));
    }

    // ----- Busca unificada (RF-38) e auditoria (RF-36)

    @GetMapping("/busca")
    @PreAuthorize(Perfis.EQUIPE)
    public List<BuscaUnificadaUseCase.Resultado> buscar(@RequestParam String termo) {
        return busca.buscar(termo);
    }

    @GetMapping("/auditoria")
    @PreAuthorize(Perfis.GESTAO)
    public List<AuditoriaResponse> auditoria(@RequestParam(required = false) String entidade,
            @RequestParam(required = false) String entidadeId) {
        return auditoria.executar(entidade, entidadeId).stream().map(AuditoriaResponse::de).toList();
    }
}

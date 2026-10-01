package br.org.semeando.digital.application.cadastro;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.pessoa.Contato;
import br.org.semeando.digital.domain.pessoa.Professor;
import br.org.semeando.digital.domain.pessoa.ProfessorRepository;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import java.util.List;
import java.util.UUID;

/** RF-32 */
@CasoDeUso
public class ProfessoresUseCase {

    private final ProfessorRepository professores;
    private final Auditor auditor;

    public ProfessoresUseCase(ProfessorRepository professores, Auditor auditor) {
        this.professores = professores;
        this.auditor = auditor;
    }

    public List<Professor> listar(boolean incluirInativos) {
        return professores.listar(incluirInativos);
    }

    public Professor buscar(UUID id) {
        return professores.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Professor", id));
    }

    public Professor cadastrar(DadosProfessor dados, Ator ator) {
        Cpf cpf = Cpf.de(dados.cpf());
        if (professores.buscarPorCpf(cpf).isPresent()) {
            throw new DomainException("DUPLICADO", "Já existe professor com este CPF");
        }
        Professor professor = professores.salvar(Professor.novo(dados.nome(), cpf, dados.contato(), dados.rg(),
                dados.formacaoAcademica(), dados.disciplinas(), dados.cargaHorariaSemanal()));
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "Professor", professor.id(), professor.nome());
        return professor;
    }

    public Professor atualizar(UUID id, DadosProfessor dados, Ator ator) {
        Professor professor = buscar(id);
        professor.atualizar(dados.nome(), dados.contato(), dados.rg(), dados.formacaoAcademica(), dados.disciplinas(),
                dados.cargaHorariaSemanal());
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Professor", id, professor.nome());
        return professores.salvar(professor);
    }

    public Professor inativar(UUID id, Ator ator) {
        Professor professor = buscar(id);
        professor.inativar();
        auditor.registrar(ator, AcaoAuditada.INATIVACAO, "Professor", id, professor.nome());
        return professores.salvar(professor);
    }

    public record DadosProfessor(String nome, String cpf, Contato contato, String rg, String formacaoAcademica,
            List<String> disciplinas, int cargaHorariaSemanal) {
    }
}

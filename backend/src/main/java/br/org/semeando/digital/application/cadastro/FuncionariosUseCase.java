package br.org.semeando.digital.application.cadastro;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.pessoa.Contato;
import br.org.semeando.digital.domain.pessoa.Funcionario;
import br.org.semeando.digital.domain.pessoa.FuncionarioRepository;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import br.org.semeando.digital.domain.usuario.Perfil;
import java.util.List;
import java.util.UUID;

/** RF-33 */
@CasoDeUso
public class FuncionariosUseCase {

    private final FuncionarioRepository funcionarios;
    private final Auditor auditor;

    public FuncionariosUseCase(FuncionarioRepository funcionarios, Auditor auditor) {
        this.funcionarios = funcionarios;
        this.auditor = auditor;
    }

    public List<Funcionario> listar(boolean incluirInativos) {
        return funcionarios.listar(incluirInativos);
    }

    public Funcionario buscar(UUID id) {
        return funcionarios.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário", id));
    }

    public Funcionario cadastrar(DadosFuncionario dados, Ator ator) {
        Cpf cpf = Cpf.de(dados.cpf());
        if (funcionarios.buscarPorCpf(cpf).isPresent()) {
            throw new DomainException("DUPLICADO", "Já existe funcionário com este CPF");
        }
        Funcionario funcionario = funcionarios.salvar(Funcionario.novo(dados.nome(), cpf, dados.contato(),
                dados.cargo(), dados.setor(), dados.perfilAcesso()));
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "Funcionario", funcionario.id(), funcionario.nome());
        return funcionario;
    }

    public Funcionario atualizar(UUID id, DadosFuncionario dados, Ator ator) {
        Funcionario funcionario = buscar(id);
        funcionario.atualizar(dados.nome(), dados.contato(), dados.cargo(), dados.setor(), dados.perfilAcesso());
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Funcionario", id, funcionario.nome());
        return funcionarios.salvar(funcionario);
    }

    public Funcionario inativar(UUID id, Ator ator) {
        Funcionario funcionario = buscar(id);
        funcionario.inativar();
        auditor.registrar(ator, AcaoAuditada.INATIVACAO, "Funcionario", id, funcionario.nome());
        return funcionarios.salvar(funcionario);
    }

    public record DadosFuncionario(String nome, String cpf, Contato contato, String cargo, String setor,
            Perfil perfilAcesso) {
    }
}

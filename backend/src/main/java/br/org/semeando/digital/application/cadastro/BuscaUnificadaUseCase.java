package br.org.semeando.digital.application.cadastro;

import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.pessoa.FuncionarioRepository;
import br.org.semeando.digital.domain.pessoa.ProfessorRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** RF-38: localiza alunos, professores ou funcionários por nome, CPF ou matrícula. */
@CasoDeUso
public class BuscaUnificadaUseCase {

    private static final int TAMANHO_MINIMO = 2;

    private final AlunoRepository alunos;
    private final ProfessorRepository professores;
    private final FuncionarioRepository funcionarios;

    public BuscaUnificadaUseCase(AlunoRepository alunos, ProfessorRepository professores,
            FuncionarioRepository funcionarios) {
        this.alunos = alunos;
        this.professores = professores;
        this.funcionarios = funcionarios;
    }

    public List<Resultado> buscar(String termo) {
        if (termo == null || termo.isBlank() || termo.trim().length() < TAMANHO_MINIMO) {
            return List.of();
        }
        String t = termo.trim();
        List<Resultado> resultados = new ArrayList<>();
        alunos.buscarPorTermo(t).forEach(a -> resultados.add(
                new Resultado("ALUNO", a.id(), a.nome(), "Status " + a.status())));
        professores.buscarPorTermo(t).forEach(p -> resultados.add(
                new Resultado("PROFESSOR", p.id(), p.nome(), "CPF " + p.cpf())));
        funcionarios.buscarPorTermo(t).forEach(f -> resultados.add(
                new Resultado("FUNCIONARIO", f.id(), f.nome(), f.cargo())));
        return resultados;
    }

    public record Resultado(String tipo, UUID id, String nome, String detalhe) {
    }
}

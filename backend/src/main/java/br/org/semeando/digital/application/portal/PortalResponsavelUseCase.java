package br.org.semeando.digital.application.portal;

import br.org.semeando.digital.application.cobranca.ConsultarFinanceiroUseCase;
import br.org.semeando.digital.application.cobranca.ConsultarFinanceiroUseCase.CobrancaComAluno;
import br.org.semeando.digital.application.shared.AcessoNegadoException;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.contrato.DocumentoVinculo;
import br.org.semeando.digital.domain.contrato.DocumentoVinculoRepository;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.pedagogico.CalculadoraFrequencia;
import br.org.semeando.digital.domain.pedagogico.DiarioClasseRepository;
import br.org.semeando.digital.domain.pedagogico.Frequencia;
import br.org.semeando.digital.domain.turma.Turma;
import br.org.semeando.digital.domain.turma.TurmaRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Portal do responsável (somente leitura): filhos, boletos, presença e documentos. */
@CasoDeUso
public class PortalResponsavelUseCase {

    private final AlunoRepository alunos;
    private final MatriculaRepository matriculas;
    private final TurmaRepository turmas;
    private final DocumentoVinculoRepository vinculos;
    private final DiarioClasseRepository diarios;
    private final ConsultarFinanceiroUseCase financeiro;
    private final Clock clock;

    public PortalResponsavelUseCase(AlunoRepository alunos, MatriculaRepository matriculas, TurmaRepository turmas,
            DocumentoVinculoRepository vinculos, DiarioClasseRepository diarios, ConsultarFinanceiroUseCase financeiro,
            Clock clock) {
        this.alunos = alunos;
        this.matriculas = matriculas;
        this.turmas = turmas;
        this.vinculos = vinculos;
        this.diarios = diarios;
        this.financeiro = financeiro;
        this.clock = clock;
    }

    public List<ResumoFilho> meusFilhos(Ator ator) {
        if (ator.cpf() == null) {
            throw new AcessoNegadoException("Usuário sem CPF de responsável vinculado");
        }
        return alunos.listarPorCpfResponsavel(ator.cpf()).stream().map(this::resumo).toList();
    }

    public List<CobrancaComAluno> boletos(UUID alunoId, Ator ator) {
        garantirFilho(alunoId, ator);
        return financeiro.doAluno(alunoId);
    }

    public List<DocumentoVinculo> documentos(UUID alunoId, Ator ator) {
        garantirFilho(alunoId, ator);
        return vinculos.pesquisar(alunoId, null, null);
    }

    private ResumoFilho resumo(Aluno aluno) {
        Optional<Matricula> atual = matriculas.listarPorAluno(aluno.id()).stream()
                .max(Comparator.comparing(Matricula::dataSolicitacao));
        Optional<Turma> turma = atual.flatMap(m -> turmas.buscarPorId(m.turmaId()));
        LocalDate hoje = LocalDate.now(clock);
        Frequencia frequencia = turma.map(t -> CalculadoraFrequencia.doAluno(aluno.id(),
                diarios.listarPorTurma(t.id(), LocalDate.of(hoje.getYear(), 1, 1), hoje)))
                .orElse(new Frequencia(aluno.id(), 0, 0));
        return new ResumoFilho(aluno, atual.orElse(null), turma.orElse(null), frequencia);
    }

    private void garantirFilho(UUID alunoId, Ator ator) {
        if (!ator.responsavel()) {
            return;
        }
        boolean filho = alunos.buscarPorId(alunoId)
                .map(a -> a.cpfResponsavel().equals(ator.cpf()))
                .orElse(false);
        if (!filho) {
            throw new AcessoNegadoException("Aluno não está sob sua responsabilidade");
        }
    }

    public record ResumoFilho(Aluno aluno, Matricula matricula, Turma turma, Frequencia frequencia) {
    }
}

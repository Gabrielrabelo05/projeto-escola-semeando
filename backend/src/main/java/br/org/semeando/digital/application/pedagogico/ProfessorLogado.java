package br.org.semeando.digital.application.pedagogico;

import br.org.semeando.digital.application.shared.AcessoNegadoException;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.pessoa.Professor;
import br.org.semeando.digital.domain.pessoa.ProfessorRepository;
import java.util.UUID;

/** Resolve o cadastro de professor do usuário autenticado (vínculo pelo CPF). */
public class ProfessorLogado {

    private final ProfessorRepository professores;

    public ProfessorLogado(ProfessorRepository professores) {
        this.professores = professores;
    }

    public UUID idDe(Ator ator) {
        if (ator.cpf() == null) {
            throw new AcessoNegadoException("Usuário sem CPF vinculado a um cadastro de professor");
        }
        return professores.buscarPorCpf(ator.cpf()).map(Professor::id)
                .orElseThrow(() -> new AcessoNegadoException("Usuário não possui cadastro de professor"));
    }
}

package br.org.semeando.digital.application.matricula;

import br.org.semeando.digital.domain.aluno.InformacoesMedicas;
import br.org.semeando.digital.domain.aluno.PessoaAutorizada;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** {@code vindoDeEscolaParticular}: exige a declaração de adimplência no checklist (Anexo didático). */
public record PreMatriculaCommand(DadosAluno aluno, DadosResponsavel responsavel, UUID turmaId,
        boolean vindoDeEscolaParticular) {

    public record DadosAluno(String nome, LocalDate dataNascimento, InformacoesMedicas informacoesMedicas,
            String hospitalEmergencia, List<PessoaAutorizada> autorizados) {
    }

    public record DadosResponsavel(String nome, String cpf, String email, String telefone, String parentesco) {
    }
}

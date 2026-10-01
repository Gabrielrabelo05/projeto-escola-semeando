package br.org.semeando.digital.domain;

import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.InformacoesMedicas;
import br.org.semeando.digital.domain.aluno.PessoaAutorizada;
import br.org.semeando.digital.domain.aluno.StatusAluno;
import br.org.semeando.digital.domain.contrato.TabelaValores;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.pessoa.Contato;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.turma.NivelEnsino;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Dados sintéticos para testes (RN-08). */
public final class Fixtures {

    public static final String CPF_VALIDO = "52998224725";
    public static final LocalDate HOJE = LocalDate.of(2026, 3, 10);

    private Fixtures() {
    }

    public static TabelaValores tabela() {
        return new TabelaValores(Dinheiro.de("375.00"), 11, Dinheiro.de("280.00"), 11, Dinheiro.de("150.00"), 10);
    }

    public static Responsavel responsavel() {
        return Responsavel.novo("Maria Sintética", Cpf.de(CPF_VALIDO), new Contato("maria@exemplo.dev", "98900000000"),
                "Mãe");
    }

    public static Aluno aluno(Responsavel responsavel, boolean comDadosSeguranca) {
        List<PessoaAutorizada> autorizados = comDadosSeguranca
                ? List.of(new PessoaAutorizada("Avó Sintética", "RG 123", "Avó", null)) : List.of();
        return new Aluno(UUID.randomUUID(), "Lucas Sintético", LocalDate.of(2019, 5, 1), responsavel.id(),
                responsavel.cpf(), InformacoesMedicas.naoInformadas(), comDadosSeguranca ? "Hospital Sintético" : null,
                autorizados, null, StatusAluno.PRE_MATRICULA);
    }

    public static Matricula matricula(Aluno aluno, NivelEnsino nivel) {
        return Matricula.solicitar(aluno.id(), UUID.randomUUID(), nivel, 2026, HOJE);
    }
}

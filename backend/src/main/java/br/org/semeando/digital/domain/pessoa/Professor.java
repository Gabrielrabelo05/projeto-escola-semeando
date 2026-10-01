package br.org.semeando.digital.domain.pessoa;

import br.org.semeando.digital.domain.shared.Cpf;
import java.util.List;
import java.util.UUID;

/** RF-32: cadastro de professores. */
public class Professor extends Pessoa {

    private String rg;
    private String formacaoAcademica;
    private List<String> disciplinas;
    private int cargaHorariaSemanal;

    public Professor(UUID id, String nome, Cpf cpf, Contato contato, boolean ativo, String rg,
            String formacaoAcademica, List<String> disciplinas, int cargaHorariaSemanal) {
        super(id, nome, cpf, contato, ativo);
        this.rg = rg;
        this.formacaoAcademica = formacaoAcademica;
        this.disciplinas = List.copyOf(disciplinas == null ? List.of() : disciplinas);
        this.cargaHorariaSemanal = cargaHorariaSemanal;
    }

    public static Professor novo(String nome, Cpf cpf, Contato contato, String rg, String formacaoAcademica,
            List<String> disciplinas, int cargaHorariaSemanal) {
        return new Professor(UUID.randomUUID(), nome, cpf, contato, true, rg, formacaoAcademica, disciplinas,
                cargaHorariaSemanal);
    }

    public void atualizar(String nome, Contato contato, String rg, String formacaoAcademica,
            List<String> disciplinas, int cargaHorariaSemanal) {
        atualizarDadosPessoais(nome, contato);
        this.rg = rg;
        this.formacaoAcademica = formacaoAcademica;
        this.disciplinas = List.copyOf(disciplinas == null ? List.of() : disciplinas);
        this.cargaHorariaSemanal = cargaHorariaSemanal;
    }

    public String rg() {
        return rg;
    }

    public String formacaoAcademica() {
        return formacaoAcademica;
    }

    public List<String> disciplinas() {
        return disciplinas;
    }

    public int cargaHorariaSemanal() {
        return cargaHorariaSemanal;
    }
}

package br.org.semeando.digital.infrastructure.persistence.pessoa;

import br.org.semeando.digital.domain.pessoa.Contato;
import br.org.semeando.digital.domain.pessoa.Professor;
import br.org.semeando.digital.domain.shared.Cpf;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "professores")
class ProfessorEntity {

    @Id
    private UUID id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private boolean ativo;
    private String rg;
    private String formacaoAcademica;
    private int cargaHorariaSemanal;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "professor_disciplinas", joinColumns = @JoinColumn(name = "professor_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "disciplina")
    private List<String> disciplinas = new ArrayList<>();

    protected ProfessorEntity() {
    }

    static ProfessorEntity de(Professor p) {
        ProfessorEntity e = new ProfessorEntity();
        e.id = p.id();
        e.nome = p.nome();
        e.cpf = p.cpf().numero();
        e.email = p.contato().email();
        e.telefone = p.contato().telefone();
        e.ativo = p.ativo();
        e.rg = p.rg();
        e.formacaoAcademica = p.formacaoAcademica();
        e.cargaHorariaSemanal = p.cargaHorariaSemanal();
        e.disciplinas = new ArrayList<>(p.disciplinas());
        return e;
    }

    Professor paraDominio() {
        return new Professor(id, nome, Cpf.de(cpf), new Contato(email, telefone), ativo, rg, formacaoAcademica,
                disciplinas, cargaHorariaSemanal);
    }
}

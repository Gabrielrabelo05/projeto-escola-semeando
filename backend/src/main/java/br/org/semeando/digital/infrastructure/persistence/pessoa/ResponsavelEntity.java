package br.org.semeando.digital.infrastructure.persistence.pessoa;

import br.org.semeando.digital.domain.pessoa.Contato;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.shared.Cpf;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "responsaveis")
class ResponsavelEntity {

    @Id
    private UUID id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String parentesco;
    private boolean ativo;

    protected ResponsavelEntity() {
    }

    static ResponsavelEntity de(Responsavel r) {
        ResponsavelEntity e = new ResponsavelEntity();
        e.id = r.id();
        e.nome = r.nome();
        e.cpf = r.cpf().numero();
        e.email = r.contato().email();
        e.telefone = r.contato().telefone();
        e.parentesco = r.parentesco();
        e.ativo = r.ativo();
        return e;
    }

    Responsavel paraDominio() {
        return new Responsavel(id, nome, Cpf.de(cpf), new Contato(email, telefone), parentesco, ativo);
    }
}

package br.org.semeando.digital.infrastructure.persistence.pessoa;

import br.org.semeando.digital.domain.pessoa.Contato;
import br.org.semeando.digital.domain.pessoa.Funcionario;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.usuario.Perfil;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "funcionarios")
class FuncionarioEntity {

    @Id
    private UUID id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private boolean ativo;
    private String cargo;
    private String setor;
    @Enumerated(EnumType.STRING)
    private Perfil perfilAcesso;

    protected FuncionarioEntity() {
    }

    static FuncionarioEntity de(Funcionario f) {
        FuncionarioEntity e = new FuncionarioEntity();
        e.id = f.id();
        e.nome = f.nome();
        e.cpf = f.cpf().numero();
        e.email = f.contato().email();
        e.telefone = f.contato().telefone();
        e.ativo = f.ativo();
        e.cargo = f.cargo();
        e.setor = f.setor();
        e.perfilAcesso = f.perfilAcesso();
        return e;
    }

    Funcionario paraDominio() {
        return new Funcionario(id, nome, Cpf.de(cpf), new Contato(email, telefone), ativo, cargo, setor,
                perfilAcesso);
    }
}

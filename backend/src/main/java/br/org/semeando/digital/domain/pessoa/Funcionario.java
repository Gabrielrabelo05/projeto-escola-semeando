package br.org.semeando.digital.domain.pessoa;

import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.Validacoes;
import br.org.semeando.digital.domain.usuario.Perfil;
import java.util.UUID;

/** RF-33: cadastro de funcionários administrativos com nível de acesso ao sistema. */
public class Funcionario extends Pessoa {

    private String cargo;
    private String setor;
    private Perfil perfilAcesso;

    public Funcionario(UUID id, String nome, Cpf cpf, Contato contato, boolean ativo, String cargo, String setor,
            Perfil perfilAcesso) {
        super(id, nome, cpf, contato, ativo);
        this.cargo = Validacoes.obrigatorio(cargo, "cargo");
        this.setor = Validacoes.obrigatorio(setor, "setor");
        this.perfilAcesso = Validacoes.obrigatorio(perfilAcesso, "perfil de acesso");
    }

    public static Funcionario novo(String nome, Cpf cpf, Contato contato, String cargo, String setor,
            Perfil perfilAcesso) {
        return new Funcionario(UUID.randomUUID(), nome, cpf, contato, true, cargo, setor, perfilAcesso);
    }

    public void atualizar(String nome, Contato contato, String cargo, String setor, Perfil perfilAcesso) {
        atualizarDadosPessoais(nome, contato);
        this.cargo = Validacoes.obrigatorio(cargo, "cargo");
        this.setor = Validacoes.obrigatorio(setor, "setor");
        this.perfilAcesso = Validacoes.obrigatorio(perfilAcesso, "perfil de acesso");
    }

    public String cargo() {
        return cargo;
    }

    public String setor() {
        return setor;
    }

    public Perfil perfilAcesso() {
        return perfilAcesso;
    }
}
